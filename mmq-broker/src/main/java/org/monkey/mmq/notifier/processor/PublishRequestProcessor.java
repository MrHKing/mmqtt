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
package org.monkey.mmq.notifier.processor;

import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.cluster.Member;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.entity.ReadRequest;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.notifier.RuleEngineEvent;
import org.monkey.mmq.service.MessageIdService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * RPC消息接受
 *
 * @author solley
 */
public class PublishRequestProcessor implements RpcProcessor<InternalMessage> {

    private static final String INTEREST_NAME = InternalMessage.class.getName();

    private final Member local;

    private final SubscribeStoreService subscribeStoreService;

    private final SessionStoreService sessionStoreService;

    private final MessageIdService messageIdService;

    public PublishRequestProcessor(Member local,
                                   SubscribeStoreService subscribeStoreService,
                                   SessionStoreService sessionStoreService,
                                   MessageIdService messageIdService) {
        this.local = local;
        this.subscribeStoreService = subscribeStoreService;
        this.sessionStoreService = sessionStoreService;
        this.messageIdService = messageIdService;
    }

    @Override
    public void handleRequest(RpcContext rpcContext, InternalMessage message) {
        // 处理消息
        this.sendPublishMessage(message.getTopic(), MqttQoS.valueOf(message.getMqttQoS()), message.getMessageBytes().toByteArray(), message.getRetain(), message.getDup());

        // 规则引擎
        RuleEngineEvent ruleEngineEvent = new RuleEngineEvent();
        ruleEngineEvent.setMessage(message);
        NotifyCenter.publishEvent(ruleEngineEvent);
    }

    @Override
    public String interest() {
        return INTEREST_NAME;
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
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
                    sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    Loggers.BROKER_NOTIFIER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = messageIdService.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    Loggers.BROKER_NOTIFIER.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }
        });
    }

    private String getProcessId(String fallback) {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf(64);
        if (index < 1) {
            return fallback;
        } else {
            try {
                return Long.toString(Long.parseLong(jvmName.substring(0, index)));
            } catch (NumberFormatException var4) {
                return fallback;
            }
        }
    }
}
