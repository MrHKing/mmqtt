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

package org.monkey.mmq.persistent;


import org.monkey.mmq.config.KeyBuilder;
import org.monkey.mmq.core.common.Constants;
import org.monkey.mmq.core.consistency.persistent.BasePersistentServiceProcessor;
import org.monkey.mmq.core.consistency.persistent.PersistentConsistencyService;
import org.monkey.mmq.core.consistency.persistent.PersistentServiceProcessor;
import org.monkey.mmq.core.consistency.persistent.StandalonePersistentServiceProcessor;
import org.monkey.mmq.core.distributed.ProtocolManager;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.consistency.matedata.Datum;
import org.monkey.mmq.core.consistency.matedata.Record;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.config.UtilsAndCommons;
import org.monkey.mmq.core.actor.metadata.message.*;
import org.monkey.mmq.core.actor.metadata.subscribe.SubscribeMateData;
import org.monkey.mmq.core.actor.metadata.system.SystemInfoMateData;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Optional;

/**
 * Persistent consistency service delegate.
 *
 * @author solley
 */
@Component("mqttPersistentConsistencyServiceDelegate")
public class MqttPersistentConsistencyServiceDelegateImpl implements PersistentConsistencyService {

    private final BasePersistentServiceProcessor persistentConsistencyService;

    private final String baseDir;

    public MqttPersistentConsistencyServiceDelegateImpl(ProtocolManager protocolManager)
            throws Exception {
        this.baseDir = Paths.get(UtilsAndCommons.DATA_BASE_DIR, "data").toString();
        this.persistentConsistencyService = createNewPersistentServiceProcessor(protocolManager);
        init();
    }

    private void init() {
    }
    
    @Override
    public void put(String key, Record value) throws MmqException {
        persistentConsistencyService.put(key, value);
    }
    
    @Override
    public void remove(String key) throws MmqException {
        persistentConsistencyService.remove(key);
    }
    
    @Override
    public Datum get(String key) throws MmqException {
        return persistentConsistencyService.get(key);
    }
    
    @Override
    public void listen(String key, RecordListener listener) throws MmqException {
        persistentConsistencyService.listen(key, listener);
    }
    
    @Override
    public void unListen(String key, RecordListener listener) throws MmqException {
        persistentConsistencyService.unListen(key, listener);
    }
    
    @Override
    public boolean isAvailable() {
        return persistentConsistencyService.isAvailable();
    }
    
    @Override
    public Optional<String> getErrorMsg() {
        return persistentConsistencyService.getErrorMsg();
    }
    
    private BasePersistentServiceProcessor createNewPersistentServiceProcessor(ProtocolManager protocolManager) throws Exception {
        final BasePersistentServiceProcessor processor =
                EnvUtil.getStandaloneMode() ? new StandalonePersistentServiceProcessor(this.baseDir,
                        Constants.MQTT_PERSISTENT_BROKER_GROUP, this::getClassOfRecordFromKey)
                        : new PersistentServiceProcessor(protocolManager, this.baseDir,
                        Constants.MQTT_PERSISTENT_BROKER_GROUP, this::getClassOfRecordFromKey);
        processor.afterConstruct();
        return processor;
    }

    protected Class<? extends Record> getClassOfRecordFromKey(String key) {
        if (KeyBuilder.matchSessionStoreKey(key)) {
            return ClientMateData.class;
        } else if (KeyBuilder.matchSystemRunTimeKey(key)) {
            return SystemInfoMateData.class;
        } else if (KeyBuilder.matchSubscribeKey(key)) {
            return SubscribeMateData.class;
        } else if (KeyBuilder.matchPublishKey(key)) {
            return DupPublishMessageMateData.class;
        } else if (KeyBuilder.matchPubRelKey(key)) {
            return DupPubRelMessageMateData.class;
        } else if (KeyBuilder.matchRetainKey(key)) {
            return RetainMessageMateData.class;
        }
        return Record.class;
    }
}
