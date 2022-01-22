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

package org.monkey.mmq.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

/**
 * WebSocket Mqtt消息编解码器
 * @author Solley
 */
public class MqttWebSocketCodec extends MessageToMessageCodec<WebSocketFrame, ByteBuf> {

	private ByteBuf data = null;

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		BinaryWebSocketFrame fram = new BinaryWebSocketFrame();
		fram.content().writeBytes(msg);
		out.add(fram);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
		if (frame instanceof BinaryWebSocketFrame) {
			if (!frame.isFinalFragment()) {
				if (data != null) {
					data.release();
				}
				ByteBuf buf = frame.content();
				data = ctx.alloc().buffer(buf.readableBytes());
				data.writeBytes(buf);
				buf.release();
				return;
			} else {
				ByteBuf byteBuf = frame.content();
				byteBuf.retain();
				out.add(byteBuf);
				if (data != null) {
					data.release();
					data = null;
				}
				return;
			}
		} else if (frame instanceof ContinuationWebSocketFrame) {
			if (data == null) {
				frame.content().release();
				return;
			}
			data.writeBytes(frame.content());
			frame.content().release();
			if (frame.isFinalFragment()) {
				out.add(data);
				data = null;
				return;
			}
		} else {
			if (data != null) {
				data.release();
				data = null;
			}
			ByteBuf byteBuf = frame.content();
			byteBuf.retain();
			out.add(byteBuf);
		}
	}
}
