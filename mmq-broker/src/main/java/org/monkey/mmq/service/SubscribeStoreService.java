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
import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.metadata.Datum;
import org.monkey.mmq.metadata.KeyBuilder;
import org.monkey.mmq.metadata.RecordListener;
import org.monkey.mmq.metadata.UtilsAndCommons;
import org.monkey.mmq.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.persistent.ConsistencyService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Health Controller.
 *
 * @author solley
 */
@Service
public class SubscribeStoreService implements RecordListener<SubscribeMateData> {

    @Resource(name = "persistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    private Map<String, SubscribeMateData> subscribes = new ConcurrentHashMap<>();

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getSubscribeStoreKey(), this);
        } catch (MmqException e) {
            Loggers.BROKER_SERVER.error("listen subscribe service failed.", e);
        }
    }

    public void put(String topicFilter, SubscribeMateData subscribeStore) throws MmqException {
        consistencyService.put(UtilsAndCommons.SUBSCRIBE_STORE + subscribeStore.getClientId() + topicFilter, subscribeStore);
    }

    @Async
    public void delete(String topicFilter, String clientId) throws MmqException {
        consistencyService.remove(UtilsAndCommons.SUBSCRIBE_STORE + clientId + topicFilter);
    }

    @Async
    public void deleteForClient(String clientId) {
        try {
            Set<Map.Entry<String, SubscribeMateData>> curClientIdSubscribeStore
                    = subscribes.entrySet().stream().filter(topic -> topic.getValue().getClientId().equals(clientId))
                    .collect(Collectors.toSet());
            curClientIdSubscribeStore.forEach(subscribe -> {
                try {
                    consistencyService.remove(subscribe.getKey());
                } catch (MmqException e) {
                    Loggers.BROKER_SERVER.error(e.getMessage());
                }
            });
        } catch (Exception e) {
            Loggers.BROKER_SERVER.error(e.getMessage());
        }
    }

    public List<SubscribeMateData> search(String topicFilter) {
        List<SubscribeMateData> subscribeStores = new ArrayList<SubscribeMateData>();
        Set<Map.Entry<String, SubscribeMateData>> subNotWildcard
                = subscribes.entrySet().stream().filter(value ->
                value.getValue().getTopicFilter().equals(topicFilter))
                .collect(Collectors.toSet());
        subNotWildcard.forEach(subscribe -> {
            subscribeStores.add(subscribe.getValue());
        });

        Set<Map.Entry<String, SubscribeMateData>> subWildcard
                = new HashSet<>();
        for (Map.Entry<String, SubscribeMateData> value : subscribes.entrySet()) {
            if ((value.getValue().getTopicFilter().endsWith("#") || value.getValue().getTopicFilter().endsWith("+"))
                    && topicFilter.startsWith(value.getValue().getTopicFilter().substring(0, value.getValue().getTopicFilter().length() - 1))) {
                subWildcard.add(value);
            }
        }
        subWildcard.forEach(subscribe -> {
            subscribeStores.add(subscribe.getValue());
        });
        return subscribeStores;
    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchSubscribeKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchSubscribeKey(key);
    }

    @Override
    public void onChange(String key, SubscribeMateData value) throws Exception {
        subscribes.put(key, value);
    }

    @Override
    public void onDelete(String key) throws Exception {
        subscribes.remove(key);
    }
}
