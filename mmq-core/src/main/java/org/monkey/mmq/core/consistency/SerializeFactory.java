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

package org.monkey.mmq.core.consistency;


import org.monkey.mmq.core.consistency.serialize.HessianSerializer;
import org.monkey.mmq.core.spi.MmqServiceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Serialization factory.
 *
 * @author solley
 */
public class SerializeFactory {
    
    public static final String HESSIAN_INDEX = "Hessian".toLowerCase();
    
    private static final Map<String, Serializer> SERIALIZER_MAP = new HashMap<String, Serializer>(4);
    
    public static String defaultSerializer = HESSIAN_INDEX;
    
    static {
        Serializer serializer = new HessianSerializer();
        SERIALIZER_MAP.put(HESSIAN_INDEX, serializer);
        for (Serializer item : MmqServiceLoader.load(Serializer.class)) {
            SERIALIZER_MAP.put(item.name().toLowerCase(), item);
        }
    }
    
    public static Serializer getDefault() {
        return SERIALIZER_MAP.get(defaultSerializer);
    }
    
    public static Serializer getSerializer(String type) {
        return SERIALIZER_MAP.get(type.toLowerCase());
    }
}
