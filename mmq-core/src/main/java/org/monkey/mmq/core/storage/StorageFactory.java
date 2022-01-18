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

package org.monkey.mmq.core.storage;

import org.monkey.mmq.core.storage.kv.FileKvStorage;
import org.monkey.mmq.core.storage.kv.KvStorage;
import org.monkey.mmq.core.storage.kv.MemoryKvStorage;
import org.monkey.mmq.core.storage.kv.RocksDBStorage;

/**
 * Ket-value Storage factory.
 *
 * @author solley
 */
public final class StorageFactory {
    
    /**
     * Create {@link KvStorage} implementation.
     *
     * @param type    type of {@link KvStorage}
     * @param label   label for {@code RocksStorage}
     * @param baseDir base dir of storage file.
     * @return implementation of {@link KvStorage}
     * @throws Exception exception during creating {@link KvStorage}
     */
    public static KvStorage createKvStorage(KvStorage.KvType type, final String label, final String baseDir)
            throws Exception {
        switch (type) {
            case File:
                return new FileKvStorage(baseDir);
            case Memory:
                return new MemoryKvStorage();
            case RocksDB:
                return new RocksDBStorage(baseDir);
            default:
                throw new IllegalArgumentException("this kv type : [" + type.name() + "] not support");
        }
    }
    
}
