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

package org.monkey.mmq.core.exception.runtime;

/**
 * MMQ serialization exception.
 *
 * @author solley
 */
public class MmqSerializationException extends MmqRuntimeException {

    public static final int ERROR_CODE = 100;

    private static final long serialVersionUID = -4308536346316915612L;

    private static final String DEFAULT_MSG = "MMQ serialize failed. ";

    private static final String MSG_FOR_SPECIFIED_CLASS = "MMQ serialize for class [%s] failed. ";

    private Class<?> serializedClass;

    public MmqSerializationException() {
        super(ERROR_CODE);
    }

    public MmqSerializationException(Class<?> serializedClass) {
        super(ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()));
        this.serializedClass = serializedClass;
    }

    public MmqSerializationException(Throwable throwable) {
        super(ERROR_CODE, DEFAULT_MSG, throwable);
    }

    public MmqSerializationException(Class<?> serializedClass, Throwable throwable) {
        super(ERROR_CODE, String.format(MSG_FOR_SPECIFIED_CLASS, serializedClass.getName()), throwable);
        this.serializedClass = serializedClass;
    }
    
    public Class<?> getSerializedClass() {
        return serializedClass;
    }
}
