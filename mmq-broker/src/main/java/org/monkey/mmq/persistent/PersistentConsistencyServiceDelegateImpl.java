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


import org.monkey.mmq.core.distributed.ProtocolManager;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.metadata.Datum;
import org.monkey.mmq.metadata.Record;
import org.monkey.mmq.metadata.RecordListener;
import org.monkey.mmq.metadata.UtilsAndCommons;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * Persistent consistency service delegate.
 *
 * @author solley
 */
@Component("persistentConsistencyServiceDelegate")
public class PersistentConsistencyServiceDelegateImpl implements PersistentConsistencyService {

    private final BasePersistentServiceProcessor persistentConsistencyService;

    public PersistentConsistencyServiceDelegateImpl(ProtocolManager protocolManager)
            throws Exception {

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
                EnvUtil.getStandaloneMode() ? new StandalonePersistentServiceProcessor()
                        : new PersistentServiceProcessor(protocolManager);
        processor.afterConstruct();
        return processor;
    }
}
