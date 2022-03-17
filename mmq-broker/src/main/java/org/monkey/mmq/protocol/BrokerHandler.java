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

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.core.actor.message.SystemMessage;

import java.io.IOException;

import static org.monkey.mmq.core.common.Constants.MODULES;

/**
 * MQTT消息处理
 * @author Solley
 */
public class BrokerHandler extends ChannelInboundHandlerAdapter {

	private ProtocolProcess protocolProcess;

	private ActorSystem actorSystem;

	public BrokerHandler(ProtocolProcess protocolProcess, ActorSystem actorSystem) {
		this.protocolProcess = protocolProcess;
		this.actorSystem = actorSystem;
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		//此处对断网进行了处理
		protocolProcess.disConnect().processDisConnect(ctx.channel(), null);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object obj) {
		MqttMessage msg = (MqttMessage) obj;
		// Do something with msg
		try {
			if (msg == null) {
				Loggers.BROKER_SERVER.error("解码错误");
				return;
			}
			// 消息解码器出现异常
			if (msg.decoderResult().isFailure()) {
				Throwable cause = msg.decoderResult().cause();
				Loggers.BROKER_SERVER.error(cause.getMessage());
				if (cause instanceof MqttUnacceptableProtocolVersionException) {
					// 不支持的协议版本
					MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
							new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
					ctx.writeAndFlush(connAckMessage);
					ctx.close();
					return;
				} else if (cause instanceof MqttIdentifierRejectedException) {
					// 不合格的clientId
					MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
							new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
							new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
					ctx.writeAndFlush(connAckMessage);
					ctx.close();
					return;
				}
				ctx.close();
				return;
			}
			switch (msg.fixedHeader().messageType()) {
				case CONNECT:
					//protocolProcess.getConnectExecutor().submit(() -> {
						try {
							protocolProcess.connect().processConnect(ctx.channel(), (MqttConnectMessage) msg);
						} catch (MmqException e) {
							Loggers.BROKER_SERVER.error(e.getErrMsg());
							SystemMessage systemMessage = new SystemMessage();
							systemMessage.setTopic(MODULES);
							systemMessage.setPayload(e.getMessage());
							systemMessage.setMqttQoS(MqttQoS.AT_LEAST_ONCE);
							ActorSelection actorRef = actorSystem.actorSelection("/user/" + ((MqttConnectMessage) msg).payload().clientIdentifier());
							actorRef.tell(systemMessage, ActorRef.noSender());
							//此处对断网进行了处理
							ctx.channel().close();
						}
					//});
					break;
				case CONNACK:
					break;
				case PUBLISH:
					//protocolProcess.getPubExecutor().submit(() -> {
						try {
							protocolProcess.publish().processPublish(ctx.channel(), (MqttPublishMessage) msg);
						} catch (MmqException e) {
							Loggers.BROKER_SERVER.error(e.getErrMsg());
						}
					//});
					break;
				case PUBACK:
	//				protocolProcess.getPubExecutor().submit(() -> {
						try {
							protocolProcess.pubAck().processPubAck(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
						} catch (MmqException e) {
							Loggers.BROKER_SERVER.error(e.getErrMsg());
						}
	//				});
					break;
				case PUBREC:
	//				protocolProcess.getSubExecutor().submit(() -> {
						try {
							protocolProcess.pubRec().processPubRec(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
						} catch (MmqException e) {
							Loggers.BROKER_SERVER.error(e.getErrMsg());
						}
	//				});

					break;
				case PUBREL:
	//				protocolProcess.getPubExecutor().submit(() -> {
						protocolProcess.pubRel().processPubRel(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
	//				});
					break;
				case PUBCOMP:
	//				protocolProcess.getSubExecutor().submit(() -> {
						try {
							protocolProcess.pubComp().processPubComp(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
						} catch (MmqException e) {
							Loggers.BROKER_SERVER.error(e.getErrMsg());
						}
	//				});
					break;
				case SUBSCRIBE:
	//				protocolProcess.getSubExecutor().submit(() -> {
						protocolProcess.subscribe().processSubscribe(ctx.channel(), (MqttSubscribeMessage) msg);
	//				});
					break;
				case SUBACK:
					break;
				case UNSUBSCRIBE:
	//				protocolProcess.getSubExecutor().submit(() -> {
						protocolProcess.unSubscribe().processUnSubscribe(ctx.channel(), (MqttUnsubscribeMessage) msg);
	//				});
					break;
				case UNSUBACK:
					break;
				case PINGREQ:
	//				protocolProcess.getPingExecutor().submit(() -> {
						protocolProcess.pingReq().processPingReq(ctx.channel(), msg);
	//				});
					break;
				case PINGRESP:
					break;
				case DISCONNECT:
	//				protocolProcess.getConnectExecutor().submit(() -> {
						try {
							protocolProcess.disConnect().processDisConnect(ctx.channel(), msg);
						} catch (MmqException e) {
							Loggers.BROKER_SERVER.error(e.getErrMsg());
						}
	//				});
					break;
				default:
					break;
			}
		} finally {
			ReferenceCountUtil.release(msg);
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
						this.protocolProcess.publish().processPublish(ctx.channel(), sessionStore.getWillMessage());
					}
				}
				ctx.close();
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {

	}

	protected String info() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return System.currentTimeMillis() + " " + this.getClass().getSimpleName() + ": ";
	}
}
