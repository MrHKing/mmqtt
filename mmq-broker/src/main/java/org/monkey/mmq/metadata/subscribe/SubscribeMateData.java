/**
 * Copyright (c) 2020, Solley (hkk@yanboo.com.cn) All rights reserved.
 */

package org.monkey.mmq.metadata.subscribe;

import org.monkey.mmq.metadata.Record;

import java.io.Serializable;

/**
 * 订阅存储
 * @author Solley
 */
public class SubscribeMateData implements Record, Serializable {

	private static final long serialVersionUID = 1276156087085594264L;

	private String clientId;

	private String topicFilter;

	private String key;

	private int mqttQoS;

	public SubscribeMateData() {

	}

	public SubscribeMateData(String clientId, String topicFilter, int mqttQoS) {
		this.clientId = clientId;
		this.topicFilter = topicFilter;
		this.mqttQoS = mqttQoS;
	}

	public String getClientId() {
		return clientId;
	}

	public SubscribeMateData setClientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public String getTopicFilter() {
		return topicFilter;
	}

	public SubscribeMateData setTopicFilter(String topicFilter) {
		this.topicFilter = topicFilter;
		return this;
	}

	public int getMqttQoS() {
		return mqttQoS;
	}

	public SubscribeMateData setMqttQoS(int mqttQoS) {
		this.mqttQoS = mqttQoS;
		return this;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
