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
import org.monkey.mmq.core.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PUBREL连接处理
 * @author Solley
 */
public class PubRel {

	private static final Logger LOGGER = LoggerFactory.getLogger(PubRel.class);

	public void processPubRel(Channel channel, MqttMessageIdVariableHeader variableHeader) {
		MqttMessage pubCompMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
		LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBREL - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
		channel.writeAndFlush(pubCompMessage);
	}

}
