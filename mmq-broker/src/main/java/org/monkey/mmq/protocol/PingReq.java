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
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.core.utils.Loggers;

/**
 * PINGREQ连接处理
 * @author Solley
 */
public class PingReq {

	public void processPingReq(Channel channel, MqttMessage msg) {
		MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
		LoggerUtils.printIfDebugEnabled(Loggers.BROKER,"PINGREQ - clientId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get());
		channel.writeAndFlush(pingRespMessage);
	}

}
