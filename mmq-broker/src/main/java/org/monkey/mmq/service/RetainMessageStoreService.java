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
import org.monkey.mmq.config.KeyBuilder;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.config.UtilsAndCommons;
import org.monkey.mmq.core.actor.metadata.message.RetainMessageMateData;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Retain消息存储
 *
 * @author solley
 */
@Service
public class RetainMessageStoreService implements RecordListener<RetainMessageMateData>  {

    @Resource(name = "mqttPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    private Map<String, RetainMessageMateData> retainMessageMateDataConcurrentHashMap = new ConcurrentHashMap<>();

    public void put(String topic, RetainMessageMateData retainMessageStore) throws MmqException {
        String key = UtilsAndCommons.RETAIN_STORE + topic;
        consistencyService.put(key, retainMessageStore);
    }

    public void remove(String topic) throws MmqException {
        String key = UtilsAndCommons.RETAIN_STORE + topic;
        consistencyService.remove(key);
    }

    public List<RetainMessageMateData> search(String topicFilter) {
        List<RetainMessageMateData> retainMessageMateData = new ArrayList<RetainMessageMateData>();
        Set<Map.Entry<String, RetainMessageMateData>> subNotWildcard
                = retainMessageMateDataConcurrentHashMap.entrySet().stream().filter(value ->
                value.getValue().getTopic().equals(topicFilter))
                .collect(Collectors.toSet());
        subNotWildcard.forEach(retainMessage -> {
            retainMessageMateData.add(retainMessage.getValue());
        });

        Set<Map.Entry<String, RetainMessageMateData>> subWildcard
                = new HashSet<>();
        for (Map.Entry<String, RetainMessageMateData> value : retainMessageMateDataConcurrentHashMap.entrySet()) {
            if ((value.getValue().getTopic().endsWith("#") || value.getValue().getTopic().endsWith("+"))
                    && topicFilter.startsWith(value.getValue().getTopic().substring(value.getValue().getTopic().length() - 1))) {
                subWildcard.add(value);
            }
        }
        subWildcard.forEach(retainMessage -> {
            retainMessageMateData.add(retainMessage.getValue());
        });
        return retainMessageMateData;
    }

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getRetainStoreKey(), this);
        } catch (MmqException e) {
            Loggers.BROKER_SERVER.error("listen subscribe service failed.", e);
        }
    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchRetainKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchRetainKey(key);
    }

    @Override
    public void onChange(String key, RetainMessageMateData value) throws Exception {
        retainMessageMateDataConcurrentHashMap.put(key, value);
    }

    @Override
    public void onDelete(String key) throws Exception {
        retainMessageMateDataConcurrentHashMap.remove(key);
    }
}
