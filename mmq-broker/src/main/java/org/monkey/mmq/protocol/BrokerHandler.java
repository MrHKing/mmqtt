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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.metadata.message.SessionMateData;

import java.io.IOException;

/**
 * MQTT消息处理
 * @author Solley
 */
public class BrokerHandler extends SimpleChannelInboundHandler<MqttMessage> {

	private ProtocolProcess protocolProcess;

	public BrokerHandler(ProtocolProcess protocolProcess) {
		this.protocolProcess = protocolProcess;
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		//此处对断网进行了处理
		protocolProcess.disConnect().subscribe(x -> {
			try {
				x.processDisConnect(ctx.channel(), null);
			} catch (MmqException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
		switch (msg.fixedHeader().messageType()) {
			case CONNECT:
				protocolProcess.connect().subscribe(x -> {
					try {
						x.processConnect(ctx.channel(), (MqttConnectMessage) msg);
					} catch (MmqException e) {
						e.printStackTrace();
					}
				});
				break;
			case CONNACK:
				break;
			case PUBLISH:
				protocolProcess.publish().subscribe(x -> {
					try {
						x.processPublish(ctx.channel(), (MqttPublishMessage) msg);
					} catch (MmqException e) {
						e.printStackTrace();
					}
				});
				break;
			case PUBACK:
				protocolProcess.pubAck().subscribe(x -> {
					try {
						x.processPubAck(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
					} catch (MmqException e) {
						e.printStackTrace();
					}
				});
				break;
			case PUBREC:
				protocolProcess.pubRec().subscribe(x -> {
					try {
						x.processPubRec(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
					} catch (MmqException e) {
						e.printStackTrace();
					}
				});
				break;
			case PUBREL:
				protocolProcess.pubRel().subscribe(x -> x.processPubRel(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader()));
				break;
			case PUBCOMP:
				protocolProcess.pubComp().subscribe(x -> {
					try {
						x.processPubComp(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
					} catch (MmqException e) {
						e.printStackTrace();
					}
				});
				break;
			case SUBSCRIBE:
				protocolProcess.subscribe().subscribe(x -> x.processSubscribe(ctx.channel(), (MqttSubscribeMessage) msg));
				break;
			case SUBACK:
				break;
			case UNSUBSCRIBE:
				protocolProcess.unSubscribe().subscribe(x -> x.processUnSubscribe(ctx.channel(), (MqttUnsubscribeMessage) msg));
				break;
			case UNSUBACK:
				break;
			case PINGREQ:
				protocolProcess.pingReq().subscribe(x -> x.processPingReq(ctx.channel(), msg));
				break;
			case PINGRESP:
				break;
			case DISCONNECT:
				protocolProcess.disConnect().subscribe(x -> {
					try {
						x.processDisConnect(ctx.channel(), msg);
					} catch (MmqException e) {
						e.printStackTrace();
					}
				});
				break;
			default:
				break;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException) {
			// 远程主机强迫关闭了一个现有的连接的异常
			ctx.close();
		} else {
			super.exceptionCaught(ctx, cause);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
			if (idleStateEvent.state() == IdleState.ALL_IDLE) {
				Channel channel = ctx.channel();
				String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
				// 发送遗嘱消息
				if (this.protocolProcess.getSessionStoreService().containsKey(clientId)) {
					SessionMateData sessionStore = this.protocolProcess.getSessionStoreService().get(clientId);
					if (sessionStore.getWillMessage() != null) {
						this.protocolProcess.publish().subscribe(x -> {
							try {
								x.processPublish(ctx.channel(), sessionStore.getWillMessage());
							} catch (MmqException e) {
								e.printStackTrace();
							}
						});
					}
				}
				ctx.close();
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
