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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.JacksonUtils;
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.service.DupPubRelMessageStoreService;
import org.monkey.mmq.service.DupPublishMessageStoreService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.monkey.mmq.core.common.Constants.CLIENT_DISCONNECT;


/**
 * DISCONNECT连接处理
 * @author Solley
 */
public class DisConnect {

	private SessionStoreService sessionStoreService;

	private SubscribeStoreService subscribeStoreService;

	private DupPublishMessageStoreService dupPublishMessageStoreService;

	private DupPubRelMessageStoreService dupPubRelMessageStoreService;

	public DisConnect(SessionStoreService sessionStoreService, SubscribeStoreService subscribeStoreService, DupPublishMessageStoreService dupPublishMessageStoreService, DupPubRelMessageStoreService dupPubRelMessageStoreService) {
		this.sessionStoreService = sessionStoreService;
		this.subscribeStoreService = subscribeStoreService;
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
		this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
	}

	public void processDisConnect(Channel channel, MqttMessage msg) throws MmqException {
		String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
		SessionMateData sessionStore = sessionStoreService.get(clientId);

		// 判断是否已经删除
		if (sessionStore == null) {
            return;
        }

		if (sessionStore.isCleanSession()) {
			subscribeStoreService.deleteForClient(clientId);
			dupPublishMessageStoreService.deleteForClient(clientId);
			dupPubRelMessageStoreService.deleteForClient(clientId);
		}
		LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"DISCONNECT - clientId: {}, cleanSession: {}", clientId, sessionStore.isCleanSession());
		sessionStoreService.delete(clientId, channel.id().asLongText());

		// 发送设备下线通知
		List<SubscribeMateData> subscribeStores = subscribeStoreService.search(CLIENT_DISCONNECT + "/" + clientId);
		subscribeStores.forEach(subscribeStore -> {
			Map payload = new HashMap();
			payload.put("clientId", clientId);
			InetSocketAddress clientIpSocket = (InetSocketAddress) channel.remoteAddress();
			String clientIp = clientIpSocket.getAddress().getHostAddress();
			payload.put("ip", clientIp);
			MqttQoS respQoS = MqttQoS.AT_LEAST_ONCE;
			MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
					new MqttPublishVariableHeader(CLIENT_DISCONNECT + "/" + clientId, 0), Unpooled.buffer().writeBytes(JacksonUtils.toJson(payload).getBytes()));
			SessionMateData subSession = sessionStoreService.get(subscribeStore.getClientId());
			if (subSession != null) {
				subSession.getChannel().writeAndFlush(publishMessage);
			}
		});
		channel.close();
	}

}
