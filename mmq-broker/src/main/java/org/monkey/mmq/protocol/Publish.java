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

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import com.google.protobuf.ByteString;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.cluster.Member;
import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.core.actor.metadata.message.DupPublishMessageMateData;
import org.monkey.mmq.core.actor.metadata.message.RetainMessageMateData;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.core.actor.message.PublishMessage;
import org.monkey.mmq.config.matedata.RuleEngineMessage;
import org.monkey.mmq.service.*;

import java.util.List;

/**
 * PUBLISH连接处理
 * @author Solley
 */
public class Publish {

	private SessionStoreService sessionStoreService;

	private SubscribeStoreService subscribeStoreService;

	private RetainMessageStoreService retainMessageStoreService;

	private DupPublishMessageStoreService dupPublishMessageStoreService;

	private ActorSystem actorSystem;

	private final Member local;

	public Publish(SessionStoreService sessionStoreService, SubscribeStoreService subscribeStoreService,
				   RetainMessageStoreService retainMessageStoreService,
				   DupPublishMessageStoreService dupPublishMessageStoreService,
				   Member local,
				   ActorSystem actorSystem) {
		this.sessionStoreService = sessionStoreService;
		this.subscribeStoreService = subscribeStoreService;
		this.retainMessageStoreService = retainMessageStoreService;
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
		this.local = local;
		this.actorSystem = actorSystem;
	}

	public void processPublish(Channel channel, MqttPublishMessage msg) throws MmqException {

		byte[] messageBytes = new byte[msg.payload().readableBytes()];
		String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
		msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
		this.sendPublishMessage(clientId, msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false, msg.variableHeader().packetId(), channel);
		if (MqttQoS.AT_LEAST_ONCE == msg.fixedHeader().qosLevel()) {
			sendPubAckMessage(channel, msg.variableHeader().packetId());
		} else if (MqttQoS.EXACTLY_ONCE == msg.fixedHeader().qosLevel()) {
			sendPubRecMessage(channel, msg.variableHeader().packetId());
		}

		// 规则引擎
		SessionMateData sessionStore = sessionStoreService.get(clientId);
		if (sessionStore == null) return;

		RuleEngineMessage ruleEngineMessage = new RuleEngineMessage();
		ruleEngineMessage.setUsername(sessionStore.getUser());
		ruleEngineMessage.setMessage(InternalMessage.newBuilder()
				.setTopic(msg.variableHeader().topicName())
				.setMqttQoS(msg.fixedHeader().qosLevel().value())
				.setMessageBytes(ByteString.copyFrom(messageBytes))
				.setDup(false).setRetain(false).setMessageId(msg.variableHeader().packetId()).build());
		ActorSelection actorSelection = actorSystem.actorSelection("/user/rule*");
		actorSelection.tell(ruleEngineMessage, ActorRef.noSender());
		
		// retain=1, 保留消息
		if (msg.fixedHeader().isRetain()) {
			if (messageBytes.length == 0) {
				retainMessageStoreService.remove(msg.variableHeader().topicName());
			} else {
				RetainMessageMateData retainMessageStore = new RetainMessageMateData().setTopic(msg.variableHeader().topicName()).setMqttQoS(msg.fixedHeader().qosLevel().value())
					.setMessageBytes(messageBytes);
				retainMessageStoreService.put(msg.variableHeader().topicName(), retainMessageStore);
			}
		}
	}

	private void sendPublishMessage(String clientId, String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup, int packetId, Channel channel) {
		List<SubscribeMateData> subscribeStores = subscribeStoreService.search(topic);

		subscribeStores.forEach(subscribeStore -> {
				// 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
				MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
				SessionMateData sessionStore = sessionStoreService.get(subscribeStore.getClientId());
				if (sessionStore != null
						&& this.local.getIp().equals(subscribeStore.getNodeIp())
						&& subscribeStore.getNodePort() == this.local.getPort()) {
					if (respQoS == MqttQoS.AT_MOST_ONCE) {
						MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
							new MqttPublishVariableHeader(topic, packetId), Unpooled.buffer().writeBytes(messageBytes));
						LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
						sessionStore.getChannel().writeAndFlush(publishMessage);
					}
					if (respQoS == MqttQoS.AT_LEAST_ONCE) {
						MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
							new MqttPublishVariableHeader(topic, packetId), Unpooled.buffer().writeBytes(messageBytes));
						LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), packetId);
						DupPublishMessageMateData dupPublishMessageStore = new DupPublishMessageMateData().setClientId(subscribeStore.getClientId())
							.setTopic(topic).setMqttQoS(respQoS.value()).setMessageId(packetId).setMessageBytes(messageBytes);
						dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
						sessionStore.getChannel().writeAndFlush(publishMessage);
					}
					if (respQoS == MqttQoS.EXACTLY_ONCE) {
						MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
							new MqttPublishVariableHeader(topic, packetId), Unpooled.buffer().writeBytes(messageBytes));
						LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), packetId);
						DupPublishMessageMateData dupPublishMessageStore = new DupPublishMessageMateData().setClientId(subscribeStore.getClientId())
							.setTopic(topic).setMqttQoS(respQoS.value()).setMessageId(packetId).setMessageBytes(messageBytes);
						dupPublishMessageStoreService.put(subscribeStore.getClientId(), dupPublishMessageStore);
						sessionStore.getChannel().writeAndFlush(publishMessage);
					}
				} else {
					PublishMessage publishMessage = new PublishMessage();
					publishMessage.setNodeIp(subscribeStore.getNodeIp());
					publishMessage.setNodePort(subscribeStore.getNodePort());
					publishMessage.setInternalMessage(InternalMessage.newBuilder()
							.setTopic(topic)
							.setMqttQoS(respQoS.value())
							.setClientId(subscribeStore.getClientId())
							.setMessageBytes(ByteString.copyFrom(messageBytes))
							.setDup(false).setRetain(false).setMessageId(packetId).build());
					ActorSelection actorSelection = actorSystem.actorSelection("/user/" + clientId);
					actorSelection.tell(publishMessage, ActorRef.noSender());
				}
		});
	}

	public void sendPubAckMessage(Channel channel, int messageId) {
		MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(messageId), null);
		channel.writeAndFlush(pubAckMessage);
	}

	private void sendPubRecMessage(Channel channel, int messageId) {
		MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.EXACTLY_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(messageId), null);
		channel.writeAndFlush(pubRecMessage);
	}

}
