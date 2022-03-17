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

package org.monkey.mmq.core.actor.metadata.message;

import org.monkey.mmq.core.consistency.matedata.Record;

import java.io.Serializable;

/**
 * Retain标志消息存储元数据
 * @author Solley
 */
public class RetainMessageMateData implements Record, Serializable {

	private static final long serialVersionUID = -7548204047370972779L;

	private String topic;

	private byte[] messageBytes;

	private int mqttQoS;

	public String getTopic() {
		return topic;
	}

	public RetainMessageMateData setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	public byte[] getMessageBytes() {
		return messageBytes;
	}

	public RetainMessageMateData setMessageBytes(byte[] messageBytes) {
		this.messageBytes = messageBytes;
		return this;
	}

	public int getMqttQoS() {
		return mqttQoS;
	}

	public RetainMessageMateData setMqttQoS(int mqttQoS) {
		this.mqttQoS = mqttQoS;
		return this;
	}
}
