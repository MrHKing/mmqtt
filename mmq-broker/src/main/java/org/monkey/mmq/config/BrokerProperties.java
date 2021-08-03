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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 服务配置
 * @author Solley
 */
@Component
public class BrokerProperties implements ApplicationListener<WebServerInitializedEvent> {

	/**
	 * Broker唯一标识
	 */
	private String id;

	/**
	 * SSL端口号, 默认8883端口
	 */
	@Value("${mmq.broker.port}")
	private int port;

	/**
	 * WebSocket SSL端口号, 默认9993端口
	 */
	@Value("${mmq.broker.websocketPort}")
	private int websocketPort;

	/**
	 * WebSocket Path值, 默认值 /mqtt
	 */
	private String websocketPath = "/mqtt";

	/**
	 * SSL密钥文件密码
	 */
	@Value("${mmq.broker.ssl.password}")
	private String sslPassword;

	/**
	 * SSL是否启用
	 */
	@Value("${mmq.broker.ssl.enabled}")
	private boolean sslEnabled;

	/**
	 * 心跳时间(秒), 默认60秒, 该值可被客户端连接时相应配置覆盖
	 */
	private int keepAlive = 60;

	/**
	 * 是否开启Epoll模式, 默认关闭
	 */
	private boolean useEpoll = false;

	/**
	 * Sokcet参数, 存放已完成三次握手请求的队列最大长度, 默认511长度
	 */
	private int soBacklog = 511;

	/**
	 * Socket参数, 是否开启心跳保活机制, 默认开启
	 */
	private boolean soKeepAlive = true;

	public String getId() {
		return id;
	}

	public BrokerProperties setId(String id) {
		this.id = id;
		return this;
	}

	public int getPort() {
		return port;
	}

	public BrokerProperties setPort(int sslPort) {
		this.port = sslPort;
		return this;
	}

	public int getWebsocketPort() {
		return websocketPort;
	}

	public BrokerProperties setWebsocketPort(int websocketPort) {
		this.websocketPort = websocketPort;
		return this;
	}

	public String getWebsocketPath() {
		return websocketPath;
	}

	public BrokerProperties setWebsocketPath(String websocketPath) {
		this.websocketPath = websocketPath;
		return this;
	}

	public String getSslPassword() {
		return sslPassword;
	}

	public BrokerProperties setSslPassword(String sslPassword) {
		this.sslPassword = sslPassword;
		return this;
	}

	public boolean getSslEnabled() {
		return sslEnabled;
	}

	public BrokerProperties setSslEnabled(boolean sslEnabled) {
		this.sslEnabled = sslEnabled;
		return this;
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

	@Override
	public void onApplicationEvent(WebServerInitializedEvent webServerInitializedEvent) {

	}
}
