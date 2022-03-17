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
package org.monkey.mmq.core.actor.metadata.system;

import org.monkey.mmq.core.consistency.matedata.Record;

import java.io.Serializable;

/**
 * Health Controller.
 *
 * @author solley
 */
public class SystemInfoMateData implements Record, Serializable {

    private Integer clientCount;

    private long systemRunTime;

    private String version;

    private String systemName;

    private Integer subscribeCount;

    public SystemInfoMateData(String version, String systemName, long systemRunTime) {
        this.version = version;
        this.systemName = systemName;
        this.systemRunTime = systemRunTime;
        this.clientCount = 0;
        this.subscribeCount = 0;
    }


    public Integer getClientCount() {
        return clientCount;
    }

    public void setClientCount(Integer clientCount) {
        this.clientCount = clientCount;
    }

    public long getSystemRunTime() {
        return systemRunTime;
    }

    public void setSystemRunTime(long systemRunTime) {
        this.systemRunTime = systemRunTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Integer getSubscribeCount() {
        return subscribeCount;
    }

    public void setSubscribeCount(Integer subscribeCount) {
        this.subscribeCount = subscribeCount;
    }

}
