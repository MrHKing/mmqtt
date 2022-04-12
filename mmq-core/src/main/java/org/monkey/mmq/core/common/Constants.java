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

package org.monkey.mmq.core.common;

import java.util.concurrent.TimeUnit;

/**
 * Constants.
 *
 * @author solley
 */
public class Constants {

    public static final String MQTT_PERSISTENT_AUTH_GROUP = "mqtt_persistent_auth";

    public static final String MQTT_PERSISTENT_CONFIG_GROUP = "mqtt_persistent_config";

    public static final String MQTT_PERSISTENT_BROKER_GROUP = "mqtt_persistent_broker";
    
    public static final String NULL = "";
    
    public static final String ACCESS_TOKEN = "accessToken";
    
    public static final String TOKEN_TTL = "tokenTtl";
    
    public static final String GLOBAL_ADMIN = "globalAdmin";
    
    public static final String USERNAME = "username";

    public static final String ENCODE = "UTF-8";

    public static final String SYSTOPIC = "$SYS";

    public static final String RULE_ENGINE =  SYSTOPIC + "/RULE";

    public static final String MODULES =  SYSTOPIC + "/MODULES";
}
