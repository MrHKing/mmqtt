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
package org.monkey.mmq.core.actor.message;

import org.monkey.mmq.core.actor.ActorMsg;
import org.monkey.mmq.core.actor.MsgType;
import org.monkey.mmq.core.actor.metadata.message.PublishInOutType;
import org.monkey.mmq.core.entity.InternalMessage;

/**
 * 发布事件
 *
 * @author solley
 */
public class PublishMessage implements ActorMsg {

    public InternalMessage message;

    private String nodeIp;

    private int nodePort;

    private boolean isLocal;

    private int bytes;

    private PublishInOutType publishInOutType;

    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public PublishInOutType getPublishInOutType() {
        return publishInOutType;
    }

    public void setPublishInOutType(PublishInOutType publishInOutType) {
        this.publishInOutType = publishInOutType;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public InternalMessage getInternalMessage() {
        return message;
    }

    public void setInternalMessage(InternalMessage message) {
        this.message = message;
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.PUBLISH_MSG;
    }
}
