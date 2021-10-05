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
import org.monkey.mmq.metadata.KeyBuilder;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.metadata.UtilsAndCommons;
import org.monkey.mmq.metadata.message.DupPublishMessageMateData;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Publish指令
 *
 * @author solley
 */
@Service
public class DupPublishMessageStoreService  {

    private Map<String, Map<Integer, DupPublishMessageMateData>> dupPublishMessageStoreMap = new ConcurrentHashMap<>();

    /**
     * Init
     */
    @PostConstruct
    public void init() {

    }

    public List<DupPublishMessageMateData> get(String clientId) {
        if (StringUtils.isEmpty(clientId)) return new ArrayList<>();
        if (dupPublishMessageStoreMap.get(clientId) == null) return new ArrayList<>();
        return new ArrayList<>(dupPublishMessageStoreMap.get(clientId).values());
    }

    public void put(String clientId, DupPublishMessageMateData dupPublishMessageStore)  {
        if (StringUtils.isEmpty(clientId)) return;
        Map<Integer,DupPublishMessageMateData> messageMateDataMap = dupPublishMessageStoreMap.get(clientId);
        if (messageMateDataMap == null) {
            messageMateDataMap = new ConcurrentHashMap<>();
            dupPublishMessageStoreMap.put(clientId, messageMateDataMap);
        }
        messageMateDataMap.put(dupPublishMessageStore.getMessageId(), dupPublishMessageStore);
        //dupPublishMessageStoreMap.put(clientId, messageMateDataMap);
    }

    @Async
    public void delete(String clientId, int messageId) {
        if (StringUtils.isEmpty(clientId)) return;
        Map<Integer,DupPublishMessageMateData> messageMateDataMap = dupPublishMessageStoreMap.get(clientId);
        if (messageMateDataMap == null || messageMateDataMap.size() <= 0) return;
        messageMateDataMap.remove(messageId);
    }

    @Async
    public void deleteForClient(String clientId) {
        if (StringUtils.isEmpty(clientId)) return;
        dupPublishMessageStoreMap.remove(clientId);
    }
}
