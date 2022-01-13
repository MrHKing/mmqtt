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

package org.monkey.mmq.config.matedata;

/**
 * Key operations for data.
 *
 * @author solley
 * @since 1.0.0
 */
public class KeyBuilder {

    public static boolean matchResourcesKey(String key) {
        return key.startsWith(UtilsAndCommons.RESOURCES_STORE);
    }

    public static boolean matchRuleEngineKey(String key) {
        return key.startsWith(UtilsAndCommons.RULE_ENGINE_STORE);
    }

    public static boolean matchModulesKey(String key) {
        return key.startsWith(UtilsAndCommons.MODULES_STORE);
    }

    public static String getModulesKey() {
        return UtilsAndCommons.MODULES_STORE;
    }

    public static String getResourcesKey() {
        return UtilsAndCommons.RESOURCES_STORE;
    }

    public static String getRuleEngineKey() {
        return UtilsAndCommons.RULE_ENGINE_STORE;
    }
}
