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
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.service.SubscribeStoreService;

import java.util.List;

/**
 * UNSUBSCRIBE连接处理
 * @author Solley
 */
public class UnSubscribe {

	private SubscribeStoreService subscribeStoreService;

	public UnSubscribe(SubscribeStoreService subscribeStoreService) {
		this.subscribeStoreService = subscribeStoreService;
	}

	public void processUnSubscribe(Channel channel, MqttUnsubscribeMessage msg) {
		List<String> topicFilters = msg.payload().topics();
		String clinetId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
		topicFilters.forEach(topicFilter -> {
			try {
				subscribeStoreService.delete(topicFilter, clinetId);
			} catch (MmqException e) {
				LoggerUtils.printIfDebugEnabled(Loggers.BROKER,"UNSUBSCRIBE - ErrorCode: {}, ErrMsg: {}", e.getErrCode(), e.getErrMsg());
			}
			LoggerUtils.printIfDebugEnabled(Loggers.BROKER,"UNSUBSCRIBE - clientId: {}, topicFilter: {}", clinetId, topicFilter);
		});
		MqttUnsubAckMessage unsubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
			new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
			MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
		channel.writeAndFlush(unsubAckMessage);
	}

}
