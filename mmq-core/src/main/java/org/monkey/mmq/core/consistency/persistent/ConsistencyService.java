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

import org.monkey.mmq.core.consistency.matedata.Datum;
import org.monkey.mmq.core.consistency.matedata.Record;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.exception.MmqException;


import java.util.Optional;

/**
 * Consistence service for all implementations to derive.
 *
 * <p>We announce this consistency service to decouple the specific consistency implementation with business logic.
 * User should not be aware of what consistency protocol is being used.
 *
 * <p>In this way, we also provide space for user to extend the underlying consistency protocols, as long as they obey
 * ourconsistency baseline.
 *
 * @author solley
 */
public interface ConsistencyService {
    
    /**
     * Put a data related to a key to Mmq cluster.
     *
     * @param key   key of data, this key should be globally unique
     * @param value value of data
     * @throws MmqException Mmq exception
     */
    void put(String key, Record value) throws MmqException;
    
    /**
     * Remove a data from Mmq cluster.
     *
     * @param key key of data
     * @throws MmqException Mmq exception
     */
    void remove(String key) throws MmqException;
    
    /**
     * Get a data from Mmq cluster.
     *
     * @param key key of data
     * @return data related to the key
     * @throws MmqException Mmq exception
     */
    Datum get(String key) throws MmqException;
    
    /**
     * Listen for changes of a data.
     *
     * @param key      key of data
     * @param listener callback of data change
     * @throws MmqException Mmq exception
     */
    void listen(String key, RecordListener listener) throws MmqException;
    
    /**
     * Cancel listening of a data.
     *
     * @param key      key of data
     * @param listener callback of data change
     * @throws MmqException Mmq exception
     */
    void unListen(String key, RecordListener listener) throws MmqException;
    
    /**
     * Get the error message of the consistency protocol.
     *
     * @return the consistency protocol error message.
     */
    Optional<String> getErrorMsg();
    
    /**
     * Tell the status of this consistency service.
     *
     * @return true if available
     */
    boolean isAvailable();
}