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
package org.monkey.mmq.service;

/**
 * 会话存储
 *
 * @author solley
 */

import akka.actor.*;
import com.alipay.remoting.rpc.RpcClient;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import org.monkey.mmq.core.actor.message.ClientRemoveMessage;
import org.monkey.mmq.core.actor.message.RejectMessage;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.entity.RejectClient;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.config.KeyBuilder;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.config.UtilsAndCommons;
import org.monkey.mmq.core.actor.metadata.message.ClientMateData;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.core.actor.metadata.system.SystemInfoMateData;
import org.monkey.mmq.core.actor.message.ClientPutMessage;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.notifier.ClientActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionStoreService implements RecordListener<ClientMateData> {

    @Resource(name = "mqttPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    ActorSystem actorSystem;

    RpcClient rpcClient;

    @Autowired
    private SystemInfoStoreService systemInfoStoreService;

    private final ServerMemberManager memberManager;

    private final SubscribeStoreService subscribeStoreService;

    private final DupPublishMessageStoreService dupPublishMessageStoreService;

    private final Map<String, SessionMateData> storage = new ConcurrentHashMap<>();

    private final Map<String, ClientMateData> clientStory = new ConcurrentHashMap<>();

    private final Map<String, ActorRef> clientActors = new ConcurrentHashMap<>();

    public SessionStoreService(ServerMemberManager memberManager,
                               SubscribeStoreService subscribeStoreService,
                               DupPublishMessageStoreService dupPublishMessageStoreService,
                               ActorSystem actorSystem, RpcClient rpcClient) {
        this.memberManager = memberManager;
        this.subscribeStoreService = subscribeStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
        this.actorSystem = actorSystem;
        this.rpcClient = rpcClient;

        // add driver actor
        actorSystem.actorOf((Props.create(ClientActor.class,
                consistencyService,
                memberManager,
                subscribeStoreService,
                this,
                dupPublishMessageStoreService,
                rpcClient
        )), "driver");
    }

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getSessionStoreKey(), this);
        } catch (MmqException e) {
            Loggers.BROKER_SERVER.error("listen Session service failed.", e);
        }
    }

    public Collection<ClientMateData> getClients() {
        return clientStory.values();
    }

    public void put(String clientId, SessionMateData sessionStore) throws MmqException {
        storage.put(clientId, sessionStore);
        InetSocketAddress clientIpSocket = (InetSocketAddress)sessionStore.getChannel().remoteAddress();
        String clientIp = clientIpSocket.getAddress().getHostAddress();

        // put client message
        ClientPutMessage clientPutMessage = new ClientPutMessage();
        clientPutMessage.setClientMateData(new ClientMateData(clientId, sessionStore.getUser(), clientIp, this.memberManager.getSelf().getIp(), this.memberManager.getSelf().getPort()));

        // create client actor
        ActorRef clientActor = actorSystem.actorOf((Props.create(ClientActor.class,
                consistencyService,
                memberManager,
                subscribeStoreService,
                this,
                dupPublishMessageStoreService,
                rpcClient
        )), clientId);
        clientActors.put(clientId, clientActor);
        clientActor.tell(clientPutMessage, ActorRef.noSender());
    }

    public SessionMateData get(String clientId) {
        if (StringUtils.isEmpty(clientId)) return null;
        return storage.get(clientId);
    }

    public SessionMateData getByChannelId(String id) {
        Collection<SessionMateData> sessionMateDataCollection = storage.values();
        Optional<SessionMateData> sessionMateData = sessionMateDataCollection.stream().filter(x -> x.getChannel().id().asLongText().equals(id)).findFirst();
        if (sessionMateData.isPresent()) {
            return sessionMateData.get();
        } else {
            return null;
        }
    }

    public void rejectClient(String clientId) {
        SessionMateData sessionMateData = this.get(clientId);
        if (sessionMateData != null) {
            sessionMateData.getChannel().close();
        } else {
            ClientMateData clientMateData = clientStory.get(UtilsAndCommons.SESSION_STORE + clientId);
            if (clientMateData == null) {
                try {
                    consistencyService.remove(UtilsAndCommons.SESSION_STORE + clientId);
                } catch (MmqException e) {
                    Loggers.BROKER_SERVER.error("remove session key failed.", e);
                }
            }
            RejectMessage rejectMessage = new RejectMessage();
            rejectMessage.setRejectClient(RejectClient.newBuilder().setClientId(clientId).build());
            rejectMessage.setNodeIp(clientMateData.getNodeIp());
            rejectMessage.setNodePort(clientMateData.getNodePort());
            ActorSelection actorRef = actorSystem.actorSelection("/user/" + clientId);
            actorRef.tell(rejectMessage, ActorRef.noSender());
        }
    }

    public boolean containsKey(String clientId) {
        if (StringUtils.isEmpty(clientId)) return false;
        return storage.containsKey(clientId);
    }

    public void delete(String clientId) throws MmqException {
        storage.remove(clientId);
        ClientRemoveMessage clientRemoveMessage = new ClientRemoveMessage();
        ClientMateData clientMateData = new ClientMateData();
        clientMateData.setClientId(clientId);
        clientRemoveMessage.setClientMateData(clientMateData);
        ActorSelection actorRef = actorSystem.actorSelection("/user/" + clientId);
        actorRef.tell(clientRemoveMessage, ActorRef.noSender());

    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchSessionStoreKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchSessionStoreKey(key);
    }

    @Override
    public void onChange(String key, ClientMateData value) throws Exception {

        // 节点IP为空则删除
//        if (StringUtils.isEmpty(value.getNodeIp())) {
//            this.delete(value.getClientId());
//        }
//
        // 判断是否是连接本节点的客户端
//        if (this.memberManager.getSelf().getIp().equals(value.getNodeIp())) {
//            SessionMateData sessionMateData = storage.get(value.getClientId());
//            if (sessionMateData != null) {
//                clientStory.put(key, value);
//            } else {
//                this.delete(value.getClientId());
//            }
//        } else {
//            clientStory.put(key, value);
//        }
        clientStory.put(key, value);
        SystemInfoMateData systemInfoMateData = systemInfoStoreService.getSystemInfo();
        systemInfoMateData.setClientCount(clientStory.size());
        systemInfoStoreService.put(systemInfoMateData);
    }

    @Override
    public void onDelete(String key) throws Exception {

        ClientMateData clientMateData = clientStory.get(key);
        SystemInfoMateData systemInfoMateData = systemInfoStoreService.getSystemInfo();
        if (clientActors.get(clientMateData.getClientId()) != null) {
            actorSystem.stop(clientActors.get(clientMateData.getClientId()));
            clientActors.remove(clientMateData.getClientId());
        }
        clientStory.remove(key);
        systemInfoMateData.setClientCount(clientStory.size());
        systemInfoStoreService.put(systemInfoMateData);
    }
}
