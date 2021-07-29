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

package org.monkey.mmq.metadata.message;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

import java.io.Serializable;

/**
 * 会话存储
 * @author Solley
 */
public class SessionMateData implements Serializable {

	private static final long serialVersionUID = 5209539791996944490L;

	private String clientId;

	private Channel channel;

	private boolean cleanSession;

	private MqttPublishMessage willMessage;

	public SessionMateData(String clientId, Channel channel, boolean cleanSession, MqttPublishMessage willMessage) {
		this.clientId = clientId;
		this.channel = channel;
		this.cleanSession = cleanSession;
		this.willMessage = willMessage;
	}

	public String getClientId() {
		return clientId;
	}

	public SessionMateData setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public Channel getChannel() {
		return channel;
	}

	public SessionMateData setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}

	public boolean isCleanSession() {
		return cleanSession;
	}

	public SessionMateData setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
		return this;
	}

	public MqttPublishMessage getWillMessage() {
		return willMessage;
	}

	public SessionMateData setWillMessage(MqttPublishMessage willMessage) {
		this.willMessage = willMessage;
		return this;
	}
}
