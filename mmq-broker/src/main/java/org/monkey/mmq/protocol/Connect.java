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


import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.monkey.mmq.auth.service.IMqttAuthService;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.core.actor.metadata.message.DupPubRelMessageMateData;
import org.monkey.mmq.core.actor.metadata.message.DupPublishMessageMateData;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.service.DupPubRelMessageStoreService;
import org.monkey.mmq.service.DupPublishMessageStoreService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;

import java.util.List;

/**
 * CONNECT连接处理
 * @author Solley
 */
public class Connect {

	private SessionStoreService sessionStoreService;

	private SubscribeStoreService subscribeStoreService;

	private DupPublishMessageStoreService dupPublishMessageStoreService;

	private DupPubRelMessageStoreService dupPubRelMessageStoreService;

	private IMqttAuthService authService;

	public Connect(SessionStoreService sessionStoreService, SubscribeStoreService subscribeStoreService, DupPublishMessageStoreService dupPublishMessageStoreService, DupPubRelMessageStoreService dupPubRelMessageStoreService, IMqttAuthService authService) {
		this.sessionStoreService = sessionStoreService;
		this.subscribeStoreService = subscribeStoreService;
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
		this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
		this.authService = authService;
	}

	public void processConnect(Channel channel, MqttConnectMessage msg) throws MmqException {
		// 消息解码器出现异常
		if (msg.decoderResult().isFailure()) {
			Throwable cause = msg.decoderResult().cause();
			if (cause instanceof MqttUnacceptableProtocolVersionException) {
				// 不支持的协议版本
				MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
					new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
				channel.writeAndFlush(connAckMessage);
				channel.close();
				return;
			} else if (cause instanceof MqttIdentifierRejectedException) {
				// 不合格的clientId
				MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
					new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
				channel.writeAndFlush(connAckMessage);
				channel.close();
				return;
			}
			channel.close();
			return;
		}
		// clientId为空或null的情况, 这里要求客户端必须提供clientId, 不管cleanSession是否为1, 此处没有参考标准协议实现
		if (StringUtils.isBlank(msg.payload().clientIdentifier())) {
			MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
				new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
			channel.writeAndFlush(connAckMessage);
			channel.close();
			return;
		}
		// 用户名和密码验证, 这里要求客户端连接时必须提供用户名和密码, 不管是否设置用户名标志和密码标志为1, 此处没有参考标准协议实现
		String username = msg.payload().userName();
		String password = msg.payload().passwordInBytes() == null ? null : new String(msg.payload().passwordInBytes(), CharsetUtil.UTF_8);

		if (!authService.checkValid(username, password)) {
			MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
				new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
			channel.writeAndFlush(connAckMessage);
			channel.close();
			return;
		}

		// 如果会话中已存储这个新连接的clientId, 就关闭之前该clientId的连接
		if (sessionStoreService.containsKey(msg.payload().clientIdentifier())) {
			SessionMateData sessionStore = sessionStoreService.get(msg.payload().clientIdentifier());
			Channel previous = sessionStore.getChannel();
			Boolean cleanSession = sessionStore.isCleanSession();
			if (cleanSession) {
				sessionStoreService.delete(msg.payload().clientIdentifier());
				subscribeStoreService.deleteForClient(msg.payload().clientIdentifier());
				dupPublishMessageStoreService.deleteForClient(msg.payload().clientIdentifier());
				dupPubRelMessageStoreService.deleteForClient(msg.payload().clientIdentifier());
			}
			previous.close();
		}
		// 处理遗嘱信息
		SessionMateData sessionStore = new SessionMateData(msg.payload().clientIdentifier(),
				channel, msg.variableHeader().isCleanSession(), null, username);
		if (msg.variableHeader().isWillFlag()) {
			MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
				new MqttPublishVariableHeader(msg.payload().willTopic(), 0), Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()));
			sessionStore.setWillMessage(willMessage);
		}
		// 处理连接心跳包
		if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
			if (channel.pipeline().names().contains("idle")) {
				channel.pipeline().remove("idle");
			}
			channel.pipeline().addFirst("idle", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
		}
		// 至此存储会话信息及返回接受客户端连接
		sessionStoreService.put(msg.payload().clientIdentifier(), sessionStore);
		// 将clientId存储到channel的map中
		channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
		Boolean sessionPresent = sessionStoreService.containsKey(msg.payload().clientIdentifier()) && !msg.variableHeader().isCleanSession();
		MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
			new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
		channel.writeAndFlush(okResp);
		Loggers.BROKER_PROTOCOL.info("CONNECT - clientId: {}, cleanSession: {}", msg.payload().clientIdentifier(), msg.variableHeader().isCleanSession());
		// 如果cleanSession为0, 需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
		if (!msg.variableHeader().isCleanSession()) {
			List<DupPublishMessageMateData> dupPublishMessageStoreList = dupPublishMessageStoreService.get(msg.payload().clientIdentifier());
			List<DupPubRelMessageMateData> dupPubRelMessageStoreList = dupPubRelMessageStoreService.get(msg.payload().clientIdentifier());
			dupPublishMessageStoreList.forEach(dupPublishMessageStore -> {
				MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(dupPublishMessageStore.getMqttQoS()), false, 0),
					new MqttPublishVariableHeader(dupPublishMessageStore.getTopic(), dupPublishMessageStore.getMessageId()), Unpooled.buffer().writeBytes(dupPublishMessageStore.getMessageBytes()));
				channel.writeAndFlush(publishMessage);
			});
			dupPubRelMessageStoreList.forEach(dupPubRelMessageStore -> {
				MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
					MqttMessageIdVariableHeader.from(dupPubRelMessageStore.getMessageId()), null);
				channel.writeAndFlush(pubRelMessage);
			});
		}
	}

}
