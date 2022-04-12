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


import org.monkey.mmq.core.env.EnvUtil;

import java.io.File;

/**
 * Naming utils and common values.
 *
 * @author solley
 */
@SuppressWarnings("PMD.ThreadPoolCreationle")
public class UtilsAndCommons {

    public static final String AUTH_STORE = "00-00---000-AUTH_STORE-000---00-00";

    public static final String DATA_BASE_DIR =
            EnvUtil.getMmqHome() + File.separator + "data" + File.separator + "auth";
}
