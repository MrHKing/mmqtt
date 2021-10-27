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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.listener.Subscriber;
import org.monkey.mmq.metadata.message.DupPublishMessageMateData;
import org.monkey.mmq.metadata.message.SessionMateData;
import org.monkey.mmq.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.service.DupPublishMessageStoreService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author solley
 */
@Component
public class SysMessageManager extends Subscriber<SysMessageEvent> {

    private final int queueMaxSize = 65539;

    private final SubscribeStoreService subscribeStoreService;

    private final SessionStoreService sessionStoreService;
    private final DupPublishMessageStoreService dupPublishMessageStoreService;

    public SysMessageManager(SubscribeStoreService subscribeStoreService,
                            SessionStoreService sessionStoreService,
                            DupPublishMessageStoreService dupPublishMessageStoreService) {
        NotifyCenter.registerToPublisher(SysMessageEvent.class, queueMaxSize);
        NotifyCenter.registerSubscriber(this);

        this.subscribeStoreService = subscribeStoreService;
        this.sessionStoreService = sessionStoreService;
        this.dupPublishMessageStoreService = dupPublishMessageStoreService;
    }

    @Override
    public void onEvent(SysMessageEvent event) {
        // 处理消息
        this.sendPublishMessage(event.getTopic(), event.getMqttQoS(),
                event.getPayload().getBytes(), false,
                false, -1);
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return SysMessageEvent.class;
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
