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

import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RemotingException;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.rpc.impl.GrpcRaftRpcFactory;
import com.alipay.sofa.jraft.rpc.impl.MarshallerRegistry;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.alipay.sofa.jraft.util.RpcFactoryHelper;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.common.Constants;
import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.entity.Log;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.listener.Subscriber;
import org.monkey.mmq.core.utils.InetUtils;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.notifier.processor.PublishRequestProcessor;
import org.monkey.mmq.service.MessageIdService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Health Controller.
 *
 * @author solley
 */
@Component
public final class BroadcastManager extends Subscriber<PublishEvent> {

    private final int queueMaxSize = 16;

    private final RpcServer rpcServer;

    private final ServerMemberManager memberManager;

//    private final RaftGroupService raftGroupService;

    private final CliClientServiceImpl cliClientService;

    public BroadcastManager(ServerMemberManager memberManager,
                            SubscribeStoreService subscribeStoreService,
                            SessionStoreService sessionStoreService,
                            MessageIdService messageIdService) {
        this.memberManager = memberManager;

        // init rpc
        String localAddress = InetUtils.getSelfIP() + ":" + (this.memberManager.getSelf().getPort() + 10);
        GrpcRaftRpcFactory raftRpcFactory = (GrpcRaftRpcFactory) RpcFactoryHelper.rpcFactory();
        raftRpcFactory.registerProtobufSerializer(InternalMessage.class.getName(), InternalMessage.getDefaultInstance());
        MarshallerRegistry registry = raftRpcFactory.getMarshallerRegistry();
        registry.registerResponseInstance(InternalMessage.class.getName(), Response.getDefaultInstance());
        rpcServer = raftRpcFactory.createRpcServer(PeerId.parsePeer(localAddress).getEndpoint());
        rpcServer.registerProcessor(new PublishRequestProcessor(
                this.memberManager.getSelf(),
                subscribeStoreService,
                sessionStoreService,
                messageIdService));
        if (!this.rpcServer.init(null)) {
            Loggers.BROKER.error("Fail to init [BaseRpcServer].");
            throw new RuntimeException("Fail to init [BaseRpcServer].");
        }
        //raftGroupService = new RaftGroupService(Constants.MQTT_PERSISTENT_BROKER_GROUP, localPeerId, copy, rpcServer, true);

        cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());

        NotifyCenter.registerToPublisher(ValueChangeEvent.class, queueMaxSize);
        NotifyCenter.registerSubscriber(this);
    }

    @Override
    public void onEvent(PublishEvent event) {
        this.memberManager.allMembersWithoutSelf().forEach(member -> {
            try {
                cliClientService.getRpcClient().invokeAsync(PeerId.parsePeer(member.getIp() + ":" + (member.getPort() + 10)).getEndpoint(), event.getInternalMessage(), (result, throwable) -> {

                }, 5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemotingException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public Class<? extends Event> subscribeType() {
        return PublishEvent.class;
    }
}
