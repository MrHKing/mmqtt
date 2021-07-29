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
 * MMQ runtime exception.
 *
 * @author solley
 */
public class MmqRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 3513491993982293262L;

    public static final String ERROR_MESSAGE_FORMAT = "errCode: %d, errMsg: %s ";

    private int errCode;

    public MmqRuntimeException(int errCode) {
        super();
        this.errCode = errCode;
    }

    public MmqRuntimeException(int errCode, String errMsg) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg));
        this.errCode = errCode;
    }

    public MmqRuntimeException(int errCode, Throwable throwable) {
        super(throwable);
        this.errCode = errCode;
    }

    public MmqRuntimeException(int errCode, String errMsg, Throwable throwable) {
        super(String.format(ERROR_MESSAGE_FORMAT, errCode, errMsg), throwable);
        this.errCode = errCode;
    }
    
    public int getErrCode() {
        return errCode;
    }
    
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }
}
