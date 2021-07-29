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

package org.monkey.mmq.core.distributed.raft;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.error.RaftError;
import com.google.protobuf.Message;
import org.monkey.mmq.core.entity.Response;

/**
 * implement jraft closure.
 *
 * @author solley
 */
public class MmqClosure implements Closure {

    private Message message;

    private Closure closure;

    private MmqStatus mmqStatus = new MmqStatus();

    public MmqClosure(Message message, Closure closure) {
        this.message = message;
        this.closure = closure;
    }
    
    @Override
    public void run(Status status) {
        mmqStatus.setStatus(status);
        closure.run(mmqStatus);
        clear();
    }
    
    private void clear() {
        message = null;
        closure = null;
        mmqStatus = null;
    }
    
    public void setResponse(Response response) {
        this.mmqStatus.setResponse(response);
    }
    
    public void setThrowable(Throwable throwable) {
        this.mmqStatus.setThrowable(throwable);
    }
    
    public Message getMessage() {
        return message;
    }
    
    // Pass the Throwable inside the state machine to the outer layer
    
    @SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
    public static class MmqStatus extends Status {
        
        private Status status;
        
        private Response response = null;
        
        private Throwable throwable = null;
        
        public void setStatus(Status status) {
            this.status = status;
        }
        
        @Override
        public void reset() {
            status.reset();
        }
        
        @Override
        public boolean isOk() {
            return status.isOk();
        }
        
        @Override
        public int getCode() {
            return status.getCode();
        }
        
        @Override
        public void setCode(int code) {
            status.setCode(code);
        }
        
        @Override
        public RaftError getRaftError() {
            return status.getRaftError();
        }
        
        @Override
        public void setError(int code, String fmt, Object... args) {
            status.setError(code, fmt, args);
        }
        
        @Override
        public void setError(RaftError error, String fmt, Object... args) {
            status.setError(error, fmt, args);
        }
        
        @Override
        public String toString() {
            return status.toString();
        }
        
        @Override
        public Status copy() {
            MmqStatus copy = new MmqStatus();
            copy.status = this.status;
            copy.response = this.response;
            copy.throwable = this.throwable;
            return copy;
        }
        
        @Override
        public String getErrorMsg() {
            return status.getErrorMsg();
        }
        
        @Override
        public void setErrorMsg(String errMsg) {
            status.setErrorMsg(errMsg);
        }
        
        public Response getResponse() {
            return response;
        }
        
        public void setResponse(Response response) {
            this.response = response;
        }
        
        public Throwable getThrowable() {
            return throwable;
        }
        
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }
        
    }
}
