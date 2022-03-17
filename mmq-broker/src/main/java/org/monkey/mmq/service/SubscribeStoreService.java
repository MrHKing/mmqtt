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

import cn.hutool.core.util.StrUtil;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.config.KeyBuilder;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.config.UtilsAndCommons;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.core.actor.metadata.system.SystemInfoMateData;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订阅一致性存储
 *
 * @author solley
 */
@Service
public class SubscribeStoreService implements RecordListener<SubscribeMateData> {

    @Resource(name = "mqttPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    @Autowired
    private SystemInfoStoreService systemInfoStoreService;

    private Map<String, ConcurrentHashMap<String, SubscribeMateData>> subscribes = new ConcurrentHashMap<>();

    private Map<String, ConcurrentHashMap<String, SubscribeMateData>> subWildcard = new ConcurrentHashMap<>();

    private final ServerMemberManager memberManager;

    public SubscribeStoreService(ServerMemberManager memberManager) {
        this.memberManager = memberManager;
    }

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
        String key = UtilsAndCommons.SUBSCRIBE_STORE + topicFilter + subscribeStore.getClientId();
        subscribeStore.setKey(key);
        subscribeStore.setNodeIp(memberManager.getSelf().getIp());
        subscribeStore.setNodePort(memberManager.getSelf().getPort());
        consistencyService.put(key, subscribeStore);
    }

    @Async
    public void delete(String topicFilter, String clientId) throws MmqException {
        String key = UtilsAndCommons.SUBSCRIBE_STORE + topicFilter + clientId;
        consistencyService.remove(key);
    }

    public List<SubscribeMateData> getSubscribes() {
        List<SubscribeMateData> subscribeStores = new ArrayList<SubscribeMateData>();
        subscribes.forEach((topicFilter, map) -> {
            subscribeStores.addAll(map.values());
        });

        subWildcard.forEach((topicFilter, map) -> {
            subscribeStores.addAll(map.values());
        });
        return subscribeStores;
    }

    @Async
    public void deleteForClient(String clientId) {
        try {
            subscribes.forEach((subKey, client) -> {
                client.forEach((key, value)->{
                    if (value.getClientId().equals(clientId)) {
                        try {
                            consistencyService.remove(key);
                        } catch (MmqException e) {
                            Loggers.BROKER_SERVER.error(e.getMessage());
                        }
                    }
                });
            });

            subWildcard.forEach((subKey, client) -> {
                client.forEach((key, value)->{
                    if (value.getClientId().equals(clientId)) {
                        try {
                            consistencyService.remove(key);
                        } catch (MmqException e) {
                            Loggers.BROKER_SERVER.error(e.getMessage());
                        }
                    }
                });
            });

        } catch (Exception e) {
            Loggers.BROKER_SERVER.error(e.getMessage());
        }
    }

    public List<SubscribeMateData> search(String topic) {
        List<SubscribeMateData> subscribeStores = new ArrayList<SubscribeMateData>();
        subscribes.forEach((topicFilter, map) -> {
            if (topic.equals(topicFilter)) {
                subscribeStores.addAll(map.values());
            }
        });

        subWildcard.forEach((topicFilter, map) -> {
            if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
                List<String> splitTopics = StrUtil.split(topic, '/');//a
                List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');//#
                String newTopicFilter = "";
                for (int i = 0; i < spliteTopicFilters.size(); i++) {
                    String value = spliteTopicFilters.get(i);
                    if (value.equals("+")) {
                        newTopicFilter = newTopicFilter + "+/";
                    } else if (value.equals("#")) {
                        newTopicFilter = newTopicFilter + "#/";
                        break;
                    } else {
                        newTopicFilter = newTopicFilter + splitTopics.get(i) + "/";
                    }
                }
                newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
                if (topicFilter.equals(newTopicFilter)) {
                    subscribeStores.addAll(map.values());
                }
            }
        });
//        return subscribeStores.stream().collect(
//                Collectors.collectingAndThen(
//                        Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SubscribeMateData::getClientId))), ArrayList::new));
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
        if (StrUtil.contains(value.getTopicFilter(), '#') || StrUtil.contains(value.getTopicFilter(), '+')) {
            ConcurrentHashMap<String, SubscribeMateData> clientConcurrentHashMap = subWildcard.get(value.getTopicFilter());
            if (clientConcurrentHashMap == null) {
                clientConcurrentHashMap = new ConcurrentHashMap<>();
            }
            clientConcurrentHashMap.put(key, value);
            subWildcard.put(value.getTopicFilter(), clientConcurrentHashMap);
        } else  {
            ConcurrentHashMap<String, SubscribeMateData> clientConcurrentHashMap = subscribes.get(value.getTopicFilter());
            if (clientConcurrentHashMap == null) {
                clientConcurrentHashMap = new ConcurrentHashMap<>();
            }
            clientConcurrentHashMap.put(key, value);
            subscribes.put(value.getTopicFilter(), clientConcurrentHashMap);
        }

        AtomicInteger subCount = new AtomicInteger();
        subscribes.forEach((subKey, client) -> {
            subCount.addAndGet(client.size());
        });
        subWildcard.forEach((subKey, client) -> {
            subCount.addAndGet(client.size());
        });
        SystemInfoMateData systemInfoMateData = systemInfoStoreService.getSystemInfo();
        systemInfoMateData.setSubscribeCount(subCount.get());
        systemInfoStoreService.put(systemInfoMateData);
    }

    @Override
    public void onDelete(String key) throws Exception {
        AtomicInteger subCount = new AtomicInteger();
        subscribes.forEach((subKey, client) -> {
            client.remove(key);
            subCount.addAndGet(client.size());
        });
        subWildcard.forEach((subKey, client) -> {
            client.remove(key);
            subCount.addAndGet(client.size());
        });

        SystemInfoMateData systemInfoMateData = systemInfoStoreService.getSystemInfo();
        systemInfoMateData.setSubscribeCount(subCount.get());
        systemInfoStoreService.put(systemInfoMateData);
    }
}
