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

package org.monkey.mmq.config;

/**
 * Key operations for data.
 *
 * @author solley
 * @since 1.0.0
 */
public class KeyBuilder {

    public static boolean matchSubscribeKey(String key) {
        return key.startsWith(UtilsAndCommons.SUBSCRIBE_STORE);
    }

    public static String getSubscribeStoreKey() {
        return UtilsAndCommons.SUBSCRIBE_STORE;
    }

    public static boolean matchPublishKey(String key) {
        return key.startsWith(UtilsAndCommons.PUBLISH_STORE);
    }

    public static String getPublishStoreKey() {
        return UtilsAndCommons.PUBLISH_STORE;
    }

    public static boolean matchPubRelKey(String key) {
        return key.startsWith(UtilsAndCommons.PUBREL_STORE);
    }

    public static String getPubRelStoreKey() {
        return UtilsAndCommons.PUBREL_STORE;
    }

    public static boolean matchMessageIdKey(String key) {
        return key.startsWith(UtilsAndCommons.MESSAGE_Id_STORE);
    }

    public static String getMessageIdStoreKey() {
        return UtilsAndCommons.MESSAGE_Id_STORE;
    }

    public static boolean matchRetainKey(String key) {
        return key.startsWith(UtilsAndCommons.RETAIN_STORE);
    }

    public static String getRetainStoreKey() {
        return UtilsAndCommons.RETAIN_STORE;
    }

    public static boolean matchSystemRunTimeKey(String key) {
        return key.startsWith(UtilsAndCommons.SYSTEM_RUN_TIME_STORE);
    }

    public static String getSystemRunTimeStoreKey() {
        return UtilsAndCommons.SYSTEM_RUN_TIME_STORE;
    }

    public static boolean matchSessionStoreKey(String key) {
        return key.startsWith(UtilsAndCommons.SESSION_STORE);
    }

    public static String getSessionStoreKey() {
        return UtilsAndCommons.SESSION_STORE;
    }
}
