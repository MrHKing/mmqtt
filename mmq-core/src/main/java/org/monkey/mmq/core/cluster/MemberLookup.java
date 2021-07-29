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

package org.monkey.mmq.core.cluster;

import org.monkey.mmq.core.exception.MmqException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Member node addressing mode.
 *
 * @author solley
 */
public interface MemberLookup {
    
    /**
     * start.
     *
     * @throws MmqException MmqException
     */
    void start() throws MmqException;
    
    /**
     * is using address server.
     *
     * @return using address server or not.
     */
    boolean useAddressServer();
    
    /**
     * Inject the ServerMemberManager property.
     *
     * @param memberManager {@link ServerMemberManager}
     */
    void injectMemberManager(ServerMemberManager memberManager);
    
    /**
     * The addressing pattern finds cluster nodes.
     *
     * @param members {@link Collection}
     */
    void afterLookup(Collection<Member> members);
    
    /**
     * Addressing mode closed.
     *
     * @throws MmqException MmqException
     */
    void destroy() throws MmqException;
    
    /**
     * Some data information about the addressing pattern.
     *
     * @return {@link Map}
     */
    default Map<String, Object> info() {
        return Collections.emptyMap();
    }
    
}
