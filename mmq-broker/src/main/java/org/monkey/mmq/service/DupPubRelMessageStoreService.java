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

import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.core.actor.metadata.message.DupPubRelMessageMateData;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PubRel指令
 *
 * @author solley
 */
@Service
public class DupPubRelMessageStoreService {

    private Map<String, Map<Integer, DupPubRelMessageMateData>> dupPubRelMessageMateDataMap = new ConcurrentHashMap<>();

    /**
     * Init
     */
    @PostConstruct
    public void init() {

    }

    public List<DupPubRelMessageMateData> get(String clientId) {
        if (StringUtils.isEmpty(clientId)) return new ArrayList<>();
        if (dupPubRelMessageMateDataMap.get(clientId) == null) return new ArrayList<>();
        return new ArrayList<>(dupPubRelMessageMateDataMap.get(clientId).values());
    }

    public void put(String clientId, DupPubRelMessageMateData dupPubRelMessageStore) {
        if (StringUtils.isEmpty(clientId)) return;
        Map<Integer, DupPubRelMessageMateData> messageMateDataMap = dupPubRelMessageMateDataMap.get(clientId);
        if (messageMateDataMap == null) {
            messageMateDataMap = new ConcurrentHashMap<>();
            dupPubRelMessageMateDataMap.put(clientId, messageMateDataMap);
        }
        messageMateDataMap.put(dupPubRelMessageStore.getMessageId(), dupPubRelMessageStore);
    }

    @Async
    public void delete(String clientId, int messageId) {
        if (StringUtils.isEmpty(clientId)) return;
        Map<Integer,DupPubRelMessageMateData> messageMateDataMap = dupPubRelMessageMateDataMap.get(clientId);
        if (messageMateDataMap == null || messageMateDataMap.size() <= 0) return;
        messageMateDataMap.remove(messageId);
    }

    @Async
    public void deleteForClient(String clientId) {
        if (StringUtils.isEmpty(clientId)) return;
        dupPubRelMessageMateDataMap.remove(clientId);
    }
}
