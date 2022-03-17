package org.monkey.mmq.notifier;

import akka.actor.AbstractActor;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcServer;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.monkey.mmq.core.actor.ActorMsg;
import org.monkey.mmq.core.actor.message.*;
import org.monkey.mmq.core.actor.metadata.message.DupPublishMessageMateData;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.config.UtilsAndCommons;
import org.monkey.mmq.notifier.processor.PublishRequestProcessor;
import org.monkey.mmq.notifier.processor.RejectClientProcessor;
import org.monkey.mmq.service.DupPublishMessageStoreService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;

import java.util.List;

/**
 * @ClassNameClientEventManager
 * @Description
 * @Author Solley
 * @Date2021/12/15 21:51
 * @Version V1.0
 **/
public class ClientActor extends AbstractActor {

    public ClientActor(ConsistencyService consistencyService,
                       ServerMemberManager memberManager,
                       SubscribeStoreService subscribeStoreService,
                       SessionStoreService sessionStoreService,
                       DupPublishMessageStoreService dupPublishMessageStoreService,RpcClient rpcClient) {
        this.consistencyService = consistencyService;
        this.subscribeStoreService = subscribeStoreService;
        this.sessionStoreService = sessionStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;

        this.memberManager = memberManager;
        this.rpcClient = rpcClient;
    }

    private final SubscribeStoreService subscribeStoreService;

    private final SessionStoreService sessionStoreService;
    private final DupPublishMessageStoreService dupPublishMessageStoreService;

    private ConsistencyService consistencyService;

    private final RpcClient rpcClient;

    private final ServerMemberManager memberManager;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ActorMsg.class, msg -> {
            switch (msg.getMsgType()) {
                case CLIENT_PUT:
                    clientPut((ClientPutMessage) msg);
                    break;
                case CLIENT_REMOVE:
                    clientRemove((ClientRemoveMessage) msg);
                    break;
                case PUBLISH_MSG:
                    publishMsg((PublishMessage) msg);
                    break;
                case REJECT_CLIENT:
                    rejectClient((RejectMessage) msg);
                    break;
                case SYSTEM_INFO:
                    systemInfo((SystemMessage) msg);
                default:
                    return;
            }
        }).build();
    }

    protected void clientPut(ClientPutMessage clientPutMessage) {
        try {
            consistencyService.put(UtilsAndCommons.SESSION_STORE + clientPutMessage.getClientMateData().getClientId(),
                    clientPutMessage.getClientMateData());
        } catch (MmqException e) {
            Loggers.BROKER_SERVER.error("client session put failed.", e);
        }
    }

    protected void clientRemove(ClientRemoveMessage clientRemoveMessage) {
        try {
            consistencyService.remove(UtilsAndCommons.SESSION_STORE + clientRemoveMessage.getClientMateData().getClientId());
        } catch (MmqException e) {
            Loggers.BROKER_SERVER.error("client session remove failed.", e);
        }
    }

    protected void publishMsg(PublishMessage publishMessage) {
        try {
            rpcClient.oneway(publishMessage.getNodeIp() + ":" + (publishMessage.getNodePort() + 10),
                    publishMessage.getInternalMessage());
        } catch (RemotingException e) {
            Loggers.BROKER_SERVER.error("client publish message remoting failed.", e);
        } catch (InterruptedException e) {
            Loggers.BROKER_SERVER.error("client publish message interrupted failed.", e);
        }
    }

    protected void rejectClient(RejectMessage rejectMessage) {
        try {
            rpcClient.oneway(rejectMessage.getNodeIp() + ":" + (rejectMessage.getNodePort() + 10),
                    rejectMessage.getRejectClient());
        } catch (RemotingException e) {
            Loggers.BROKER_SERVER.error("reject client remoting failed.", e);
        } catch (InterruptedException e) {
            Loggers.BROKER_SERVER.error("reject client message interrupted failed.", e);
        }
    }

    protected void systemInfo(SystemMessage systemMessage) {
        this.sendPublishMessage(systemMessage.getTopic(), systemMessage.getMqttQoS(),
                systemMessage.getPayload() == null ? new byte[0] : systemMessage.getPayload().getBytes(), false,
                false, -1);
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup, int messageId) {
        List<SubscribeMateData> subscribeStores = subscribeStoreService.search(topic);
        subscribeStores.forEach(subscribeStore -> {
            if (sessionStoreService.containsKey(subscribeStore.getClientId())) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
                    Loggers.BROKER_NOTIFIER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
                    SessionMateData sessionStore = sessionStoreService.get(subscribeStore.getClientId());
                    if (sessionStore != null) {
                        sessionStore.getChannel().writeAndFlush(publishMessage);
                    }
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    Loggers.BROKER_NOTIFIER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    DupPublishMessageMateData dupPublishMessageStore = new DupPublishMessageMateData().setClientId(subscribeStore.getClientId())
                            .setTopic(topic).setMessageId(messageId).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
                    dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
                    SessionMateData sessionStore = sessionStoreService.get(subscribeStore.getClientId());
                    if (sessionStore != null) {
                        sessionStore.getChannel().writeAndFlush(publishMessage);
                    }
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    Loggers.BROKER_NOTIFIER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    DupPublishMessageMateData dupPublishMessageStore = new DupPublishMessageMateData().setClientId(subscribeStore.getClientId())
                            .setTopic(topic).setMessageId(messageId).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
                    dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
                    SessionMateData sessionStore = sessionStoreService.get(subscribeStore.getClientId());
                    if (sessionStore != null) {
                        sessionStore.getChannel().writeAndFlush(publishMessage);
                    }
                }
            }
        });
    }
}
