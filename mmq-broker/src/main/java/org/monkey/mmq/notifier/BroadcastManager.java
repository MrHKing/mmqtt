/*
 * Copyright 2021-2021 Monkey Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.monkey.mmq.notifier;

import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RemotingException;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.alipay.sofa.jraft.util.RpcFactoryHelper;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.config.driver.MysqlDriver;
import org.monkey.mmq.config.service.ResourcesService;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.consistency.notifier.ValueChangeEvent;
import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.entity.RejectClient;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.listener.Subscriber;
import org.monkey.mmq.core.utils.InetUtils;
import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.notifier.processor.PublishRequestProcessor;
import org.monkey.mmq.notifier.processor.RejectClientProcessor;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 消息广播管理
 *
 * @author solley
 */
@Component
public final class BroadcastManager extends Subscriber<PublishEvent> {

    private final int queueMaxSize = 10;

    private final RpcServer rpcServer;

    private final ServerMemberManager memberManager;

//    private final RaftGroupService raftGroupService;

    private final RpcClient client;

    public BroadcastManager(ServerMemberManager memberManager,
                            SubscribeStoreService subscribeStoreService,
                            SessionStoreService sessionStoreService) {
        this.memberManager = memberManager;

        // init rpc
//        String localAddress = InetUtils.getSelfIP() + ":" + (this.memberManager.getSelf().getPort() + 10);

//        GrpcRaftRpcFactory raftRpcFactory = (GrpcRaftRpcFactory) RpcFactoryHelper.rpcFactory();
//        raftRpcFactory.registerProtobufSerializer(InternalMessage.class.getName(), InternalMessage.getDefaultInstance());
//        raftRpcFactory.registerProtobufSerializer(RejectClient.class.getName(), RejectClient.getDefaultInstance());
//
//        MarshallerRegistry registry = raftRpcFactory.getMarshallerRegistry();
//        registry.registerResponseInstance(InternalMessage.class.getName(), Response.getDefaultInstance());
//        registry.registerResponseInstance(RejectClient.class.getName(), Response.getDefaultInstance());

        rpcServer = new RpcServer(this.memberManager.getSelf().getPort() + 10);
        rpcServer.registerUserProcessor(new PublishRequestProcessor(
                this.memberManager.getSelf(),
                subscribeStoreService,
                sessionStoreService));
        rpcServer.registerUserProcessor(new RejectClientProcessor(sessionStoreService));
        rpcServer.startup();

        //raftGroupService = new RaftGroupService(Constants.MQTT_PERSISTENT_BROKER_GROUP, localPeerId, copy, rpcServer, true);

        client = new RpcClient();
        client.startup();

        NotifyCenter.registerToPublisher(PublishEvent.class, queueMaxSize);
        NotifyCenter.registerSubscriber(this);
    }

    @Override
    public void onEvent(PublishEvent event) {
        if (event == null) return;
        if (StringUtils.isEmpty(event.getNodeIp())) return;

        try {
                switch (event.getPublicEventType()) {
                    case REJECT_CLIENT:
                        client.oneway(event.getNodeIp() + ":" + (event.getNodePort() + 10),
                                        event.getRejectClient());
                        break;
                    case PUBLISH_MESSAGE:
                    default:
                        client.oneway(event.getNodeIp() + ":" + (event.getNodePort() + 10),
                                        event.getInternalMessage());
                        break;
                }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (com.alipay.remoting.exception.RemotingException e) {
            e.printStackTrace();
        }

        // 规则引擎
        RuleEngineEvent ruleEngineEvent = new RuleEngineEvent();
        ruleEngineEvent.setMessage(event.getInternalMessage());
        NotifyCenter.publishEvent(ruleEngineEvent);
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return PublishEvent.class;
    }
}
