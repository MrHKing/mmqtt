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

package org.monkey.mmq.core.consistency.persistent;

import com.google.common.collect.Lists;
import org.monkey.mmq.core.exception.ErrorCode;
import org.monkey.mmq.core.exception.KvStorageException;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.exception.runtime.MmqRuntimeException;
import org.monkey.mmq.core.storage.StorageFactory;
import org.monkey.mmq.core.storage.kv.KvStorage;
import org.monkey.mmq.core.storage.kv.MemoryKvStorage;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.core.utils.TimerContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Kv storage implementation for mqtt.
 *
 * @author solley
 */
public class MmqKvStorage extends MemoryKvStorage {

    private static final String LOAD_SNAPSHOT = MmqKvStorage.class.getSimpleName() + ".snapshotLoad";

    private static final String LABEL = "mqtt-persistent";

    private final String baseDir;

    private final KvStorage baseDirStorage;

    private final Map<String, KvStorage> mqttKvStorage;

    private boolean isSnapshotLoad = false;

    public boolean isSnapshotLoad() {
        return this.isSnapshotLoad;
    }

    public MmqKvStorage(final String baseDir) throws Exception {
        this.baseDir = baseDir;
        this.baseDirStorage = StorageFactory.createKvStorage(KvType.RocksDB, LABEL, baseDir);
        this.mqttKvStorage = new ConcurrentHashMap<>(16);
    }


    
    @Override
    public byte[] get(byte[] key) throws KvStorageException {
        // First get the data from the memory Cache
        byte[] result = super.get(key);
        if (null == result) {
            try {
                KvStorage storage = createActualStorageIfAbsent(key);
                result = null == storage ? null : storage.get(key);
                if (null != result) {
                    super.put(key, result);
                }
            } catch (Exception e) {
                throw new KvStorageException(ErrorCode.KVStorageWriteError.getCode(),
                        "Get data failed, key: " + new String(key) + ", detail: " + e.getMessage(), e);
            }
        }
        return result;
    }
    
    @Override
    public Map<byte[], byte[]> batchGet(List<byte[]> keys) throws KvStorageException {
        Map<byte[], byte[]> result = new HashMap<>(keys.size());
        for (byte[] key : keys) {
            byte[] val = get(key);
            if (val != null) {
                result.put(key, val);
            }
        }
        return result;
    }
    
    @Override
    public void put(byte[] key, byte[] value) throws KvStorageException {
        try {
            KvStorage storage = createActualStorageIfAbsent(key);
            storage.put(key, value);
        } catch (Exception e) {
            throw new KvStorageException(ErrorCode.KVStorageWriteError.getCode(),
                    "Put data failed, key: " + new String(key) + ", detail: " + e.getMessage(), e);
        }
        // after actual storage put success, put it in memory, memory put should success all the time
        super.put(key, value);
    }
    
    @Override
    public void batchPut(List<byte[]> keys, List<byte[]> values) throws KvStorageException {
        if (keys.size() != values.size()) {
            throw new KvStorageException(ErrorCode.KVStorageBatchWriteError,
                    "key's size must be equal to value's size");
        }
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            put(keys.get(i), values.get(i));
        }
    }
    
    @Override
    public void delete(byte[] key) throws KvStorageException {
        try {
            KvStorage storage = createActualStorageIfAbsent(key);
            if (null != storage) {
                storage.delete(key);
            }
        } catch (Exception e) {
            throw new KvStorageException(ErrorCode.KVStorageDeleteError.getCode(),
                    "Delete data failed, key: " + new String(key) + ", detail: " + e.getMessage(), e);
        }
        // after actual storage delete success, put it in memory, memory delete should success all the time
        super.delete(key);
    }
    
    @Override
    public void batchDelete(List<byte[]> keys) throws KvStorageException {
        for (byte[] each : keys) {
            delete(each);
        }
    }
    
    @Override
    public void doSnapshot(String backupPath) throws KvStorageException {
        baseDirStorage.doSnapshot(backupPath);
    }
    
    @Override
    public void snapshotLoad(String path) throws KvStorageException {
        TimerContext.start(LOAD_SNAPSHOT);
        try {
            baseDirStorage.snapshotLoad(path);
            loadSnapshotFromActualStorage(baseDirStorage);
            loadNamespaceSnapshot();
            this.isSnapshotLoad = true;
        } finally {
            TimerContext.end(LOAD_SNAPSHOT, Loggers.RAFT);
        }
    }
    
    private void loadSnapshotFromActualStorage(KvStorage actualStorage) throws KvStorageException {
        for (byte[] each : actualStorage.allKeys()) {
            byte[] datum = actualStorage.get(each);
            super.put(each, datum);
        }
    }
    
    private void loadNamespaceSnapshot() {
        for (String each : getAllNamespaceDirs()) {
            try {
                KvStorage kvStorage = createActualStorageIfAbsent(each);
                loadSnapshotFromActualStorage(kvStorage);
            } catch (Exception e) {
                Loggers.RAFT.error("load snapshot for namespace {} failed", each, e);
            }
        }
    }
    
    private List<String> getAllNamespaceDirs() {
        File[] files = new File(baseDir).listFiles();
        List<String> result = Collections.emptyList();
        if (null != files) {
            result = new ArrayList<>(files.length);
            for (File each : files) {
                if (each.isDirectory()) {
                    result.add(each.getName());
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    @Override
    public List<byte[]> allKeys() {
        try {
            KvStorage storage = createActualStorageIfAbsent(org.apache.commons.lang3.StringUtils.EMPTY);
            return storage.allKeys();
        } catch (Exception e) {
            throw new MmqRuntimeException(MmqException.SERVER_ERROR, e);
        }
    }
    
    @Override
    public void shutdown() {
        baseDirStorage.shutdown();
        for (KvStorage each : mqttKvStorage.values()) {
            each.shutdown();
        }
        mqttKvStorage.clear();
        super.shutdown();
    }
    
    private KvStorage createActualStorageIfAbsent(byte[] key) throws Exception {
        String keyString = new String(key);
        return createActualStorageIfAbsent(org.apache.commons.lang3.StringUtils.EMPTY);
    }
    
    private KvStorage createActualStorageIfAbsent(String mqtt) throws Exception {
        if (StringUtils.isBlank(mqtt)) {
            return baseDirStorage;
        }
        
        Function<String, KvStorage> kvStorageBuilder = key -> {
            try {
                String namespacePath = Paths.get(baseDir, key).toString();
                return StorageFactory.createKvStorage(KvType.RocksDB, LABEL, namespacePath);
            } catch (Exception e) {
                throw new MmqRuntimeException(MmqException.SERVER_ERROR, e);
            }
        };
        mqttKvStorage.computeIfAbsent(mqtt, kvStorageBuilder);
        return mqttKvStorage.get(mqtt);
    }
}
