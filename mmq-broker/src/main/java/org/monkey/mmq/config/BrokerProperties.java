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

package org.monkey.mmq.config;

import org.monkey.mmq.core.env.EnvUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 服务配置
 * @author Solley
 */
@Component
public class BrokerProperties {

	private static final String MMQ_BROKER_PORT_PROPERTY = "mmq.broker.port";
	private static final String DEFAULT_MMQ_BROKER_PORT = "1883";

	private static final String MMQ_BROKER_WEBSOCKET_PROPERTY = "mmq.broker.websocketPort";
	private static final String DEFAULT_MMQ_BROKER_WEBSOCKET = "2883";

	private static final String MMQ_BROKER_SSL_PORT_PROPERTY = "mmq.broker.ssl.port";
	private static final String DEFAULT_MMQ_BROKER_SSL_PORT = "1663";

	private static final String MMQ_BROKER_WEBSOCKET_SSL_PROPERTY = "mmq.broker.ssl.websocketPort";
	private static final String DEFAULT_MMQ_BROKER_SSL_WEBSOCKET = "2663";

	private static final String MMQ_BROKER_SSL_PASSWORD = "mmq.broker.ssl.password";
	private static final String DEFAULT_MMQ_BROKER_SSL_PASSWORD = "mmq";

	private static final String MMQ_BROKER_SSL_CERT_PATH = "mmq.broker.ssl.certPath";
	private static final String DEFAULT_MMQ_BROKER_SSL_CERT_PATH  = "cert/mmq.pfx";

	/**
	 * WebSocket Path值, 默认值 /mqtt
	 */
	private String websocketPath = "/mqtt";

	/**
	 * 心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖
	 */
	private int keepAlive = 60;

	/**
	 * 是否开启Epoll模式, 默认关闭
	 */
	private boolean useEpoll = false;

	/**
	 * Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认1024长度
	 */
	private int soBacklog = 1024;

	/**
	 * Socket参数, 是否开启心跳保活机制, 默认开启
	 */
	private boolean soKeepAlive = true;

	public int getPort() {
		return Integer.parseInt(EnvUtil.getProperty(MMQ_BROKER_PORT_PROPERTY, DEFAULT_MMQ_BROKER_PORT));
	}

	public int getWebsocketPort() {
		return Integer.parseInt(EnvUtil.getProperty(MMQ_BROKER_WEBSOCKET_PROPERTY, DEFAULT_MMQ_BROKER_WEBSOCKET));
	}

	public int getSSLPort() {
		return Integer.parseInt(EnvUtil.getProperty(MMQ_BROKER_SSL_PORT_PROPERTY, DEFAULT_MMQ_BROKER_SSL_PORT));
	}

	public int getSSLWebsocketPort() {
		return Integer.parseInt(EnvUtil.getProperty(MMQ_BROKER_WEBSOCKET_SSL_PROPERTY, DEFAULT_MMQ_BROKER_SSL_WEBSOCKET));
	}

	public String getWebsocketPath() {
		return websocketPath;
	}

	public String getSslPassword() {
		return EnvUtil.getProperty(MMQ_BROKER_SSL_PASSWORD, DEFAULT_MMQ_BROKER_SSL_PASSWORD);
	}

	public String getSslCertPath() {
		return EnvUtil.getProperty(MMQ_BROKER_SSL_CERT_PATH, DEFAULT_MMQ_BROKER_SSL_CERT_PATH);
	}

	public int getKeepAlive() {
		return keepAlive;
	}

	public BrokerProperties setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
		return this;
	}

	public boolean isUseEpoll() {
		return useEpoll;
	}

	public BrokerProperties setUseEpoll(boolean useEpoll) {
		this.useEpoll = useEpoll;
		return this;
	}

	public int getSoBacklog() {
		return soBacklog;
	}

	public BrokerProperties setSoBacklog(int soBacklog) {
		this.soBacklog = soBacklog;
		return this;
	}

	public boolean isSoKeepAlive() {
		return soKeepAlive;
	}

	public BrokerProperties setSoKeepAlive(boolean soKeepAlive) {
		this.soKeepAlive = soKeepAlive;
		return this;
	}
}
