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

package org.monkey.mmq.core.consistency.cp;

import org.monkey.mmq.core.consistency.Config;
import org.monkey.mmq.core.consistency.ConsistencyProtocol;

/**
 * cp protocol.
 *
 * @author solley
 */
@SuppressWarnings("all")
public interface CPProtocol<C extends Config, P extends RequestProcessor4CP> extends ConsistencyProtocol<C, P> {
    
    /**
     * Returns whether this node is a leader node
     *
     * @param group business module info
     * @return is leader
     */
    boolean isLeader(String group);
    
}
