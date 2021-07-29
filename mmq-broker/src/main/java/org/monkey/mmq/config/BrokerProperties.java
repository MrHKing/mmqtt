/**
 * Copyright (c) 2020, Solley (hkk@yanboo.com.cn) All rights reserved.
 */

package org.monkey.mmq.config;

import org.springframework.stereotype.Component;

/**
 * 服务配置
 * @author Solley
 */
@Component
public class BrokerProperties {

	/**
	 * Broker唯一标识
	 */
	private String id;

	/**
	 * SSL端口号, 默认8883端口
	 */
	private int port = 8886;

	/**
	 * WebSocket SSL端口号, 默认9993端口
	 */
	private int websocketPort = 9996;

	/**
	 * WebSocket Path值, 默认值 /mqtt
	 */
	private String websocketPath = "/mqtt";

	/**
	 * SSL密钥文件密码
	 */
	private String sslPassword;

	/**
	 * SSL是否启用
	 */
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

	/**
	 * 是否集群
	 */
	private boolean isClustered = false;

	/**
	 * 是否启用kafka
	 */
	private boolean kafkaEnable = false;

	/**
	 * 是否k8s模式下
	 */
	private boolean k8sEnable = false;

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

	public boolean getIsClustered() {
		return isClustered;
	}

	public void setIsClustered(boolean isClustered) {
		this.isClustered = isClustered;
	}

	public boolean getKafkaEnable() {
		return kafkaEnable;
	}

	public void setKafkaEnable(boolean kafkaEnable) {
		this.kafkaEnable = kafkaEnable;
	}

	public boolean getK8sEnable() {
		return k8sEnable;
	}

	public void setK8sEnable(boolean k8sEnable) {
		this.k8sEnable = k8sEnable;
	}
}
