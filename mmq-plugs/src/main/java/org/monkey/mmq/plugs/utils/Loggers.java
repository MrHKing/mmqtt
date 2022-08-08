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

package org.monkey.mmq.plugs.utils;

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

    public static final Logger PLUGS = LoggerFactory.getLogger("org.monkey.mmq.plugs");
    
    public static void setLogLevel(String logName, String level) {
        
        switch (logName) {
            case "mmq-plugs":
                ((ch.qos.logback.classic.Logger) PLUGS).setLevel(Level.valueOf(level));
                break;
            default:
                break;
        }
        
    }
}
