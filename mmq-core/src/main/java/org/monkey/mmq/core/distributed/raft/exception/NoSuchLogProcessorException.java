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

package org.monkey.mmq.core.distributed.raft.exception;

/**
 * There is no log processing group corresponding to the group.
 *
 * @author solley
 */
public class NoSuchLogProcessorException extends RuntimeException {
    
    public NoSuchLogProcessorException() {
    }
    
    public NoSuchLogProcessorException(String message) {
        super(message);
    }
    
    public NoSuchLogProcessorException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NoSuchLogProcessorException(Throwable cause) {
        super(cause);
    }
    
    public NoSuchLogProcessorException(String message, Throwable cause, boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
