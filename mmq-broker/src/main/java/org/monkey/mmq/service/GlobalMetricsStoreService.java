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

import org.monkey.mmq.config.KeyBuilder;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.config.UtilsAndCommons;
import org.monkey.mmq.core.actor.metadata.message.ClientMateData;
import org.monkey.mmq.core.actor.metadata.message.PublishInOutType;
import org.monkey.mmq.core.actor.metadata.message.PublishMateData;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.actor.metadata.system.SystemInfoMateData;
import org.monkey.mmq.metrics.GlobalMQTTMessageCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static org.monkey.mmq.core.actor.metadata.message.PublishInOutType.IN;

/**
 * 系统信息
 *
 * @author solley
 */
@Service
public class GlobalMetricsStoreService  implements RecordListener<PublishMateData> {

    @Resource(name = "mqttPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    @Autowired
    GlobalMQTTMessageCounter globalMQTTMessageCounter;

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getPublishStoreKey(), this);
        } catch (MmqException e) {
            Loggers.BROKER_SERVER.error("listen global metrics service failed.", e);
        }
    }

    public void put(String clientId, int bytes, PublishInOutType publishInOutType) throws MmqException {
        PublishMateData publishMateData = new PublishMateData();
        publishMateData.setBytes(bytes);
        publishMateData.setOutInType(publishInOutType.name());
        consistencyService.put(UtilsAndCommons.PUBLISH_STORE + clientId, publishMateData);
    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchPublishKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchPublishKey(key);
    }

    @Override
    public void onChange(String key, PublishMateData value) throws Exception {
        if (IN.name().equals(value.getOutInType())) {
            globalMQTTMessageCounter.countInboundTraffic(value.getBytes());
        } else {
            globalMQTTMessageCounter.countOutboundTraffic(value.getBytes());
        }

    }

    @Override
    public void onDelete(String key) throws Exception {

    }
}
