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

package org.monkey.mmq.auth.persistent;

import org.monkey.mmq.config.matedata.UtilsAndCommons;

import static org.monkey.mmq.auth.persistent.UtilsAndCommons.AUTH_STORE;

/**
 * Key operations for data.
 *
 * @author solley
 * @since 1.0.0
 */
public class KeyBuilder {

    public static boolean matchAuthKey(String key) {
        return key.startsWith(AUTH_STORE);
    }

    public static String getAuthKey() {
        return AUTH_STORE;
    }
}
