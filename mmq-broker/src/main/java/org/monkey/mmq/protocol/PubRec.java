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
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.core.actor.metadata.message.DupPubRelMessageMateData;
import org.monkey.mmq.service.DupPubRelMessageStoreService;
import org.monkey.mmq.service.DupPublishMessageStoreService;

/**
 * PUBREC连接处理
 * @author Solley
 */
public class PubRec {

	private DupPublishMessageStoreService dupPublishMessageStoreService;

	private DupPubRelMessageStoreService dupPubRelMessageStoreService;

	public PubRec(DupPublishMessageStoreService dupPublishMessageStoreService, DupPubRelMessageStoreService dupPubRelMessageStoreService) {
		this.dupPublishMessageStoreService = dupPublishMessageStoreService;
		this.dupPubRelMessageStoreService = dupPubRelMessageStoreService;
	}

	public void processPubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) throws MmqException {
		MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
		LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBREC - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
		dupPublishMessageStoreService.delete((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
		DupPubRelMessageMateData dupPubRelMessageStore = new DupPubRelMessageMateData().setClientId((String) channel.attr(AttributeKey.valueOf("clientId")).get())
			.setMessageId(variableHeader.messageId());
		dupPubRelMessageStoreService.put((String) channel.attr(AttributeKey.valueOf("clientId")).get(), dupPubRelMessageStore);
		channel.writeAndFlush(pubRelMessage);
	}

}
