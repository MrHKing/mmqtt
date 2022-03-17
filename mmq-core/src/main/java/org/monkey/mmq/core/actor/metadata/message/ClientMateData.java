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
package org.monkey.mmq.core.actor.metadata.message;

import org.monkey.mmq.core.consistency.matedata.Record;

import java.io.Serializable;
import java.util.Date;

/**
 * Health Controller.
 *
 * @author solley
 */
public class ClientMateData implements Record, Serializable {

    private static final long serialVersionUID = 5209539791996944490L;

    private String clientId;

    private String user;

    private Date connectTime;

    private String address;

    private String nodeIp;

    private int nodePort;

    public ClientMateData() {

    }

    public ClientMateData(String clientId, String user, String address, String nodeId, int nodePort) {
        this.clientId = clientId;
        this.user = user;
        this.address = address;
        this.connectTime = new Date();
        this.nodeIp = nodeId;
        this.nodePort = nodePort;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(Date connectTime) {
        this.connectTime = connectTime;
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
}
