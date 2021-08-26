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

package org.monkey.mmq.protocol;

import com.google.protobuf.ByteString;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.metadata.message.DupPublishMessageMateData;
import org.monkey.mmq.metadata.message.RetainMessageMateData;
import org.monkey.mmq.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.notifier.PublicEventType;
import org.monkey.mmq.notifier.PublishEvent;
import org.monkey.mmq.service.*;

import java.util.List;

/**
 * PUBLISH连接处理
 * @author Solley
 */
public class Publish {

	private SessionStoreService sessionStoreService;

	private SubscribeStoreService subscribeStoreService;

	private MessageIdService messageIdService;

	private RetainMessageStoreService retainMessageStoreService;

	private DupPublishMessageStoreService dupPublishMessageStoreService;

	public Publish(SessionStoreService sessionStoreService, SubscribeStoreService subscribeStoreService,
				   MessageIdService messageIdService, RetainMessageStoreService retainMessageStoreService,
				   DupPublishMessageStoreService dupPublishMessageStoreService) {
		this.sessionStoreService = sessionStoreService;
		this.subscribeStoreService = subscribeStoreService;
		this.messageIdService = messageIdService;
		this.retainMessageStoreService = retainMessageStoreService;
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
	}

	public void processPublish(Channel channel, MqttPublishMessage msg) throws MmqException {
		// QoS=0
		if (msg.fixedHeader().qosLevel() == MqttQoS.AT_MOST_ONCE) {
			byte[] messageBytes = new byte[msg.payload().readableBytes()];
			msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
			PublishEvent publishEvent = new PublishEvent();
			publishEvent.setPublicEventType(PublicEventType.PUBLISH_MESSAGE);
			publishEvent.setInternalMessage(InternalMessage.newBuilder()
					.setTopic(msg.variableHeader().topicName())
					.setMqttQoS(msg.fixedHeader().qosLevel().value())
					.setMessageBytes(ByteString.copyFrom(messageBytes))
					.setDup(false).setRetain(false).build());
			NotifyCenter.publishEvent(publishEvent);
			this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
		}
		// QoS=1
		if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
			byte[] messageBytes = new byte[msg.payload().readableBytes()];
			msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
			PublishEvent publishEvent = new PublishEvent();
			publishEvent.setPublicEventType(PublicEventType.PUBLISH_MESSAGE);
			publishEvent.setInternalMessage(InternalMessage.newBuilder().setTopic(msg.variableHeader().topicName())
					.setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(ByteString.copyFrom(messageBytes))
					.setDup(false).setRetain(false).build());
			NotifyCenter.publishEvent(publishEvent);
			this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
			this.sendPubAckMessage(channel, msg.variableHeader().packetId());
		}
		// QoS=2
		if (msg.fixedHeader().qosLevel() == MqttQoS.EXACTLY_ONCE) {
			byte[] messageBytes = new byte[msg.payload().readableBytes()];
			msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
			PublishEvent publishEvent = new PublishEvent();
			publishEvent.setPublicEventType(PublicEventType.PUBLISH_MESSAGE);
			publishEvent.setInternalMessage(InternalMessage.newBuilder().setTopic(msg.variableHeader().topicName())
					.setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(ByteString.copyFrom(messageBytes))
					.setDup(false).setRetain(false).build());
			NotifyCenter.publishEvent(publishEvent);
			this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
			this.sendPubRecMessage(channel, msg.variableHeader().packetId());
		}
		// retain=1, 保留消息
		if (msg.fixedHeader().isRetain()) {
			byte[] messageBytes = new byte[msg.payload().readableBytes()];
			msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
			if (messageBytes.length == 0) {
				retainMessageStoreService.remove(msg.variableHeader().topicName());
			} else {
				RetainMessageMateData retainMessageStore = new RetainMessageMateData().setTopic(msg.variableHeader().topicName()).setMqttQoS(msg.fixedHeader().qosLevel().value())
					.setMessageBytes(messageBytes);
				retainMessageStoreService.put(msg.variableHeader().topicName(), retainMessageStore);
			}
		}
	}

	private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
		List<SubscribeMateData> subscribeStores = subscribeStoreService.search(topic);
		subscribeStores.forEach(subscribeStore -> {
			try {
				if (sessionStoreService.containsKey(subscribeStore.getClientId())) {
					// 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
					MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
					if (respQoS == MqttQoS.AT_MOST_ONCE) {
						MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
							new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
						LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
						sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
					}
					if (respQoS == MqttQoS.AT_LEAST_ONCE) {
						int messageId = messageIdService.getNextMessageId();
						MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
							new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
						LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
						DupPublishMessageMateData dupPublishMessageStore = new DupPublishMessageMateData().setClientId(subscribeStore.getClientId())
							.setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
						dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
						sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
					}
					if (respQoS == MqttQoS.EXACTLY_ONCE) {
						int messageId = messageIdService.getNextMessageId();
						MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
							new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
						LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
						DupPublishMessageMateData dupPublishMessageStore = new DupPublishMessageMateData().setClientId(subscribeStore.getClientId())
							.setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
						dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
						sessionStoreService.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
					}
				}
			} catch (MmqException e) {
				e.printStackTrace();
			}
		});
	}

	private void sendPubAckMessage(Channel channel, int messageId) {
		MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(messageId), null);
		channel.writeAndFlush(pubAckMessage);
	}

	private void sendPubRecMessage(Channel channel, int messageId) {
		MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(messageId), null);
		channel.writeAndFlush(pubRecMessage);
	}

}
