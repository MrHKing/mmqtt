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

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.service.DupPubRelMessageStoreService;
import org.monkey.mmq.service.DupPublishMessageStoreService;
import org.monkey.mmq.service.SessionStoreService;
import org.monkey.mmq.service.SubscribeStoreService;


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
		sessionStoreService.delete(clientId);
		channel.close();
	}

}
