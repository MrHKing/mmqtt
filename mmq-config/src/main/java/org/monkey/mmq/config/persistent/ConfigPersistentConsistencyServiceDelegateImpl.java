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

package org.monkey.mmq.config.persistent;


import org.monkey.mmq.config.matedata.*;
import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.config.matedata.RuleEngineMateData;
import org.monkey.mmq.core.consistency.matedata.Datum;
import org.monkey.mmq.core.consistency.matedata.Record;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.consistency.persistent.BasePersistentServiceProcessor;
import org.monkey.mmq.core.consistency.persistent.PersistentConsistencyService;
import org.monkey.mmq.core.consistency.persistent.PersistentServiceProcessor;
import org.monkey.mmq.core.consistency.persistent.StandalonePersistentServiceProcessor;
import org.monkey.mmq.core.distributed.ProtocolManager;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.exception.MmqException;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Optional;

import static org.monkey.mmq.core.common.Constants.MQTT_PERSISTENT_CONFIG_GROUP;

/**
 * Persistent consistency service delegate.
 *
 * @author solley
 */
@Component("configPersistentConsistencyServiceDelegate")
public class ConfigPersistentConsistencyServiceDelegateImpl implements PersistentConsistencyService {

    private final BasePersistentServiceProcessor persistentConsistencyService;

    private final String baseDir;

    public ConfigPersistentConsistencyServiceDelegateImpl(ProtocolManager protocolManager)
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
                        MQTT_PERSISTENT_CONFIG_GROUP, this::getClassOfRecordFromKey)
                        : new PersistentServiceProcessor(protocolManager, this.baseDir,
                        MQTT_PERSISTENT_CONFIG_GROUP, this::getClassOfRecordFromKey);
        processor.afterConstruct();
        return processor;
    }

    protected Class<? extends Record> getClassOfRecordFromKey(String key) {
        if (KeyBuilder.matchResourcesKey(key)) {
            return ResourcesMateData.class;
        } else if (KeyBuilder.matchRuleEngineKey(key)) {
            return RuleEngineMateData.class;
        } else if (KeyBuilder.matchModulesKey(key)) {
            return ModelMateData.class;
        }
        return Record.class;
    }
}
