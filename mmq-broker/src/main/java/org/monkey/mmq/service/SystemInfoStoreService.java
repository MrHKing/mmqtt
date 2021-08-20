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

import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.metadata.KeyBuilder;
import org.monkey.mmq.metadata.RecordListener;
import org.monkey.mmq.metadata.UtilsAndCommons;
import org.monkey.mmq.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.metadata.system.SystemInfoMateData;
import org.monkey.mmq.persistent.ConsistencyService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static org.monkey.mmq.metadata.UtilsAndCommons.SYSTEM_RUN_TIME_STORE;

/**
 * 系统信息
 *
 * @author solley
 */
@Service
public class SystemInfoStoreService implements RecordListener<SystemInfoMateData> {

    @Resource(name = "persistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    private SystemInfoMateData systemInfoMateData;

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            systemInfoMateData = new SystemInfoMateData("1.0.0", "MMQ",  System.currentTimeMillis());
            consistencyService.listen(KeyBuilder.getSystemRunTimeStoreKey(), this);
            consistencyService.put(SYSTEM_RUN_TIME_STORE, systemInfoMateData);
        } catch (MmqException e) {
            Loggers.BROKER_SERVER.error("listen subscribe service failed.", e);
        }
    }

    public SystemInfoMateData getSystemInfo() {
        return systemInfoMateData;
    }

    public void put(SystemInfoMateData systemInfoMateData) throws MmqException {
        consistencyService.put(SYSTEM_RUN_TIME_STORE, systemInfoMateData);
    }

    public void addClient() {

    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchSystemRunTimeKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchSystemRunTimeKey(key);
    }

    @Override
    public void onChange(String key, SystemInfoMateData value) throws Exception {
        systemInfoMateData = value;
    }

    @Override
    public void onDelete(String key) throws Exception {
        systemInfoMateData = null;
    }
}
