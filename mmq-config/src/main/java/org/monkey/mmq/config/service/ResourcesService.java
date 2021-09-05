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
package org.monkey.mmq.config.service;

import org.monkey.mmq.config.config.Loggers;
import org.monkey.mmq.config.matedata.KeyBuilder;
import org.monkey.mmq.config.matedata.resources.ResourcesMateData;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.core.exception.MmqException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Health Controller.
 *
 * @author solley
 */
public class ResourcesService implements RecordListener<ResourcesMateData> {



    @Resource(name = "configPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getResourcesKey(), this);
        } catch (MmqException e) {
            Loggers.CONFIG_SERVER.error("listen resources service failed.", e);
        }
    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchResourcesKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchResourcesKey(key);
    }

    @Override
    public void onChange(String key, ResourcesMateData value) throws Exception {

    }

    @Override
    public void onDelete(String key) throws Exception {

    }
}
