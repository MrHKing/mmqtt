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

import cn.hutool.core.util.StrUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.LoggerUtils;
import org.monkey.mmq.core.actor.metadata.message.RetainMessageMateData;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.service.RetainMessageStoreService;
import org.monkey.mmq.service.SubscribeStoreService;

import java.util.ArrayList;
import java.util.List;

/**
 * SUBSCRIBE连接处理
 * @author Solley
 */
public class Subscribe {

	private SubscribeStoreService subscribeStoreService;

	private RetainMessageStoreService retainMessageStoreService;

	public Subscribe(SubscribeStoreService subscribeStoreService,  RetainMessageStoreService retainMessageStoreService) {
		this.subscribeStoreService = subscribeStoreService;
		this.retainMessageStoreService = retainMessageStoreService;
	}

	public void processSubscribe(Channel channel, MqttSubscribeMessage msg) {
		List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
		if (this.validTopicFilter(topicSubscriptions)) {
			String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
			List<Integer> mqttQoSList = new ArrayList<Integer>();
			topicSubscriptions.forEach(topicSubscription -> {
				String topicFilter = topicSubscription.topicName();
				MqttQoS mqttQoS = topicSubscription.qualityOfService();
				SubscribeMateData subscribeStore = new SubscribeMateData(clientId, topicFilter, mqttQoS.value());
				try {
					subscribeStoreService.put(topicFilter, subscribeStore);
				} catch (MmqException e) {
					LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"Subscribe - clientId: {}, subscribe: {}",  channel.attr(AttributeKey.valueOf("clientId")).get(), topicFilter);
				}
				mqttQoSList.add(mqttQoS.value());
				LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", clientId, topicFilter, mqttQoS.value());
			});
			MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
				new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
				MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
				new MqttSubAckPayload(mqttQoSList));
			channel.writeAndFlush(subAckMessage);
			// 发布保留消息
			topicSubscriptions.forEach(topicSubscription -> {
				String topicFilter = topicSubscription.topicName();
				MqttQoS mqttQoS = topicSubscription.qualityOfService();
				this.sendRetainMessage(channel, topicFilter, mqttQoS, msg.variableHeader().messageId());
			});
		} else {
			channel.close();
		}
	}

	private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
		for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
			String topicFilter = topicSubscription.topicName();
			// 以#或+符号开头的、以/符号结尾的及不存在/符号的订阅按非法订阅处理, 这里没有参考标准协议
			if (StrUtil.startWith(topicFilter, '#') || StrUtil.startWith(topicFilter, '+')) return false;
			if (StrUtil.contains(topicFilter, '#')) {
				// 不是以/#字符串结尾的订阅按非法订阅处理
				if (!StrUtil.endWith(topicFilter, "/#")) return false;
				// 如果出现多个#符号的订阅按非法订阅处理
				if (StrUtil.count(topicFilter, '#') > 1) return false;
			}
			if (StrUtil.contains(topicFilter, '+')) {
				//如果+符号和/+字符串出现的次数不等的情况按非法订阅处理
				if (StrUtil.count(topicFilter, '+') != StrUtil.count(topicFilter, "/+")) return false;
			}
		}
		return true;
	}

	private void sendRetainMessage(Channel channel, String topicFilter, MqttQoS mqttQoS, int packetId) {
		List<RetainMessageMateData> retainMessageStores = retainMessageStoreService.search(topicFilter);
		retainMessageStores.forEach(retainMessageStore -> {
			MqttQoS respQoS = retainMessageStore.getMqttQoS() > mqttQoS.value() ? mqttQoS : MqttQoS.valueOf(retainMessageStore.getMqttQoS());
			if (respQoS == MqttQoS.AT_MOST_ONCE) {
				MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
					new MqttPublishVariableHeader(retainMessageStore.getTopic(), 0), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
				LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessageStore.getTopic(), respQoS.value());
				channel.writeAndFlush(publishMessage);
			}
			if (respQoS == MqttQoS.AT_LEAST_ONCE) {
				MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
					new MqttPublishVariableHeader(retainMessageStore.getTopic(), packetId), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
				LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessageStore.getTopic(), respQoS.value(), packetId);
				channel.writeAndFlush(publishMessage);
			}
			if (respQoS == MqttQoS.EXACTLY_ONCE) {
				MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
					new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
					new MqttPublishVariableHeader(retainMessageStore.getTopic(), packetId), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
				LoggerUtils.printIfDebugEnabled(Loggers.BROKER_PROTOCOL,"PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), retainMessageStore.getTopic(), respQoS.value(), packetId);
				channel.writeAndFlush(publishMessage);
			}
		});
	}

}
