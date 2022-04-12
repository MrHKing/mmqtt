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

import akka.actor.ActorSystem;
import org.monkey.mmq.auth.service.IMqttAuthService;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 协议处理
 * @author Solley
 */
@Component
public class ProtocolProcess {

	@Autowired
	private SessionStoreService sessionStoreService;

	@Autowired
	private SubscribeStoreService subscribeStoreService;

	@Qualifier("mqttAuthService")
	@Autowired
	private IMqttAuthService authService;

	@Autowired
	private RetainMessageStoreService messageStoreService;

	@Autowired
	private DupPublishMessageStoreService dupPublishMessageStoreService;

	@Autowired
	private DupPubRelMessageStoreService dupPubRelMessageStoreService;

	@Autowired
	private ActorSystem actorSystem;

	public final ServerMemberManager memberManager;

	public ProtocolProcess(ServerMemberManager memberManager) {
		this.memberManager = memberManager;
	}

	private ExecutorService connectExecutor;
	private ExecutorService pubExecutor;
	private ExecutorService subExecutor;
	private ExecutorService pingExecutor;

	private LinkedBlockingQueue<Runnable> connectQueue;
	private LinkedBlockingQueue<Runnable> pubQueue;
	private LinkedBlockingQueue<Runnable> subQueue;
	private LinkedBlockingQueue<Runnable> pingQueue;

	@PostConstruct
	public void init() {
		this.connectQueue = new LinkedBlockingQueue<>(100000);
		this.pubQueue = new LinkedBlockingQueue<>(100000);
		this.subQueue = new LinkedBlockingQueue<>(100000);
		this.pingQueue = new LinkedBlockingQueue<>(10000);

		int coreThreadNum = Runtime.getRuntime().availableProcessors();
		this.connectExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
				coreThreadNum * 2,
				60000,
				TimeUnit.MILLISECONDS,
				connectQueue,
				new ThreadFactoryImpl("ConnectThread"),
				new RejectHandler("connect", 100000));
		this.pubExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
				coreThreadNum * 2,
				60000,
				TimeUnit.MILLISECONDS,
				pubQueue,
				new ThreadFactoryImpl("PubThread"),
				new RejectHandler("pub", 100000));
		this.subExecutor = new ThreadPoolExecutor(coreThreadNum * 2,
				coreThreadNum * 2,
				60000,
				TimeUnit.MILLISECONDS,
				subQueue,
				new ThreadFactoryImpl("SubThread"),
				new RejectHandler("sub", 100000));
		this.pingExecutor = new ThreadPoolExecutor(coreThreadNum,
				coreThreadNum,
				60000,
				TimeUnit.MILLISECONDS,
				pingQueue,
				new ThreadFactoryImpl("PingThread"),
				new RejectHandler("heartbeat", 100000));
	}

	private Connect connect;

	private Subscribe subscribe;

	private UnSubscribe unSubscribe;

	private Publish publish;

	private DisConnect disConnect;

	private PingReq pingReq;

	private PubRel pubRel;

	private PubAck pubAck;

	private PubRec pubRec;

	private PubComp pubComp;

	public ExecutorService getConnectExecutor() {
		return connectExecutor;
	}

	public ExecutorService getPubExecutor() {
		return pubExecutor;
	}

	public ExecutorService getSubExecutor() {
		return subExecutor;
	}

	public ExecutorService getPingExecutor() {
		return pingExecutor;
	}

	public Connect connect() {
		if (connect == null) {
			connect = new Connect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService, authService);
		}
		return connect;
	}

	public Subscribe subscribe() {
		if (subscribe == null) {
			subscribe = new Subscribe(subscribeStoreService, messageStoreService);
		}
		return subscribe;
	}

	public UnSubscribe unSubscribe() {
		if (unSubscribe == null) {
			unSubscribe = new UnSubscribe(subscribeStoreService);
		}
		return unSubscribe;
	}

	public Publish publish() {
		if (publish == null) {
			publish = new Publish(sessionStoreService, subscribeStoreService, messageStoreService, dupPublishMessageStoreService, memberManager.getSelf(), actorSystem);
		}
		return publish;
	}

	public DisConnect disConnect() {
		if (disConnect == null) {
			disConnect = new DisConnect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService);
		}
		return disConnect;
	}

	public PingReq pingReq() {
		if (pingReq == null) {
			pingReq = new PingReq();
		}
		return pingReq;
	}

	public PubRel pubRel() {
		if (pubRel == null) {
			pubRel = new PubRel();
		}
		return pubRel;
	}

	public PubAck pubAck() {
		if (pubAck == null) {
			pubAck = new PubAck(dupPublishMessageStoreService);
		}
		return pubAck;
	}

	public PubRec pubRec() {
		if (pubRec == null) {
			pubRec = new PubRec(dupPublishMessageStoreService, dupPubRelMessageStoreService);
		}
		return pubRec;
	}

	public PubComp pubComp() {
		if (pubComp == null) {
			pubComp = new PubComp(dupPubRelMessageStoreService);
		}
		return pubComp;
	}

	public SessionStoreService getSessionStoreService() {
		return sessionStoreService;
	}

}
