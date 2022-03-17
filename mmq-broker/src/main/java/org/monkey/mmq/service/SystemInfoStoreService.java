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
package org.monkey.mmq.service;

import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.actor.metadata.system.SystemInfoMateData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 系统信息
 *
 * @author solley
 */
@Service
public class SystemInfoStoreService {

    private SystemInfoMateData systemInfoMateData;

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        systemInfoMateData = new SystemInfoMateData("1.0.0", "MMQ",  System.currentTimeMillis());
    }

    public SystemInfoMateData getSystemInfo() {
        return systemInfoMateData;
    }

    public void put(SystemInfoMateData systemInfoMateData) throws MmqException {
        this.systemInfoMateData = systemInfoMateData;
    }
}
