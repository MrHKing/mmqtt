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

/**
 * Health Controller.
 *
 * @author solley
 */

import org.monkey.mmq.core.consistency.SerializeFactory;
import org.monkey.mmq.core.consistency.Serializer;
import org.monkey.mmq.core.exception.ErrorCode;
import org.monkey.mmq.core.exception.KvStorageException;
import org.monkey.mmq.core.storage.kv.MemoryKvStorage;
import org.monkey.mmq.core.utils.ByteUtils;
import org.monkey.mmq.metadata.message.SessionMateData;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
public class SessionStoreService extends MemoryKvStorage {

    protected final Serializer serializer;

    private final Map<String, SessionMateData> storage = new ConcurrentSkipListMap<>();

    public SessionStoreService() {
        this.serializer = SerializeFactory.getSerializer("Hessian");
    }

    public void put(String clientId, SessionMateData sessionStore) {
        storage.put(clientId, sessionStore);
    }

    public SessionMateData get(String clientId) {
        return storage.get(clientId);
    }

    public boolean containsKey(String clientId) {
        return storage.containsKey(clientId);
    }

    public void delete(String clientId) {
        storage.remove(clientId);
    }
}
