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

package org.monkey.mmq.core.utils;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loggers for core.
 *
 * @author solley
 * @since 1.2.0
 */
public class Loggers {
    
    public static final Logger CORE = LoggerFactory.getLogger("org.monkey.mmq.core");

    public static final Logger RAFT = LoggerFactory.getLogger("org.monkey.mmq.core.distributed.raft");
    
    public static final Logger CLUSTER = LoggerFactory.getLogger("org.monkey.mmq.core.cluster");
    
    public static void setLogLevel(String logName, String level) {
        
        switch (logName) {
            case "mmq-core":
                ((ch.qos.logback.classic.Logger) CORE).setLevel(Level.valueOf(level));
                break;
            case "core-raft":
                ((ch.qos.logback.classic.Logger) RAFT).setLevel(Level.valueOf(level));
                break;
            case "core-cluster":
                ((ch.qos.logback.classic.Logger) CLUSTER).setLevel(Level.valueOf(level));
                break;
            default:
                break;
        }
        
    }
}
