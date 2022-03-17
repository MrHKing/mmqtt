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
 * PUBREL重发消息存储元数据
 * @author Solley
 */
public class DupPubRelMessageMateData implements Record, Serializable {

	private static final long serialVersionUID = -4111642532532950980L;

	private String clientId;

	private int messageId;

	public String getClientId() {
		return clientId;
	}

	public DupPubRelMessageMateData setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public int getMessageId() {
		return messageId;
	}

	public DupPubRelMessageMateData setMessageId(int messageId) {
		this.messageId = messageId;
		return this;
	}

}
