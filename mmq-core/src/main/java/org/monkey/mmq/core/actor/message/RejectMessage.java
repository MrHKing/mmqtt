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
import org.monkey.mmq.core.entity.RejectClient;

/**
 * 发布事件
 *
 * @author solley
 */
public class RejectMessage implements ActorMsg {

    public RejectClient rejectClient;

    private String nodeIp;

    private int nodePort;

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

    public RejectClient getRejectClient() {
        return rejectClient;
    }

    public void setRejectClient(RejectClient rejectClient) {
        this.rejectClient = rejectClient;
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.REJECT_CLIENT;
    }
}
