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

package org.monkey.mmq.metadata;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * mqtt data.
 *
 * @author solley
 */
public class Datum<T extends Record> implements Serializable {
    
    private static final long serialVersionUID = -2525482315889753720L;
    
    public String key;
    
    public T value;
    
    public AtomicLong timestamp = new AtomicLong(0L);
    
    /**
     * Create datum.
     *
     * @param key   key of datum
     * @param value value of datum
     * @param <T>   Types of value
     * @return new datum
     */
    public static <T extends Record> Datum createDatum(final String key, final T value) {
        Datum datum = new Datum();
        datum.key = key;
        datum.value = value;
        return datum;
    }
}
