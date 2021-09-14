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

import org.monkey.mmq.auth.service.IAuthService;
import org.monkey.mmq.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
	private IAuthService authService;

	@Autowired
	private MessageIdService messageIdService;

	@Autowired
	private RetainMessageStoreService messageStoreService;

	@Autowired
	private DupPublishMessageStoreService dupPublishMessageStoreService;

	@Autowired
	private DupPubRelMessageStoreService dupPubRelMessageStoreService;

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

	public Mono<Connect> connect() {
		if (connect == null) {
			connect = new Connect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService, authService);
		}
		return Mono.just(connect);
	}

	public Mono<Subscribe> subscribe() {
		if (subscribe == null) {
			subscribe = new Subscribe(subscribeStoreService, messageIdService, messageStoreService);
		}
		return Mono.just(subscribe);
	}

	public Mono<UnSubscribe> unSubscribe() {
		if (unSubscribe == null) {
			unSubscribe = new UnSubscribe(subscribeStoreService);
		}
		return Mono.just(unSubscribe);
	}

	public Mono<Publish> publish() {
		if (publish == null) {
			publish = new Publish(sessionStoreService, subscribeStoreService, messageIdService, messageStoreService, dupPublishMessageStoreService);
		}
		return Mono.just(publish);
	}

	public Mono<DisConnect> disConnect() {
		if (disConnect == null) {
			disConnect = new DisConnect(sessionStoreService, subscribeStoreService, dupPublishMessageStoreService, dupPubRelMessageStoreService);
		}
		return Mono.just(disConnect);
	}

	public Mono<PingReq> pingReq() {
		if (pingReq == null) {
			pingReq = new PingReq();
		}
		return Mono.just(pingReq);
	}

	public Mono<PubRel> pubRel() {
		if (pubRel == null) {
			pubRel = new PubRel();
		}
		return Mono.just(pubRel);
	}

	public Mono<PubAck> pubAck() {
		if (pubAck == null) {
			pubAck = new PubAck(messageIdService, dupPublishMessageStoreService);
		}
		return Mono.just(pubAck);
	}

	public Mono<PubRec> pubRec() {
		if (pubRec == null) {
			pubRec = new PubRec(dupPublishMessageStoreService, dupPubRelMessageStoreService);
		}
		return Mono.just(pubRec);
	}

	public Mono<PubComp> pubComp() {
		if (pubComp == null) {
			pubComp = new PubComp(messageIdService, dupPubRelMessageStoreService);
		}
		return Mono.just(pubComp);
	}

	public SessionStoreService getSessionStoreService() {
		return sessionStoreService;
	}

}
