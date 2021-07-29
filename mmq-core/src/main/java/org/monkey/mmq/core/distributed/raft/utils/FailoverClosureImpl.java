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

package org.monkey.mmq.core.distributed.raft.utils;


import com.alipay.sofa.jraft.Status;
import org.monkey.mmq.core.distributed.raft.exception.ConsistencyException;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.utils.Objects;

import java.util.concurrent.CompletableFuture;

/**
 * Closure with internal retry mechanism.
 *
 * @author solley
 */
public class FailoverClosureImpl implements FailoverClosure {
    
    private final CompletableFuture<Response> future;
    
    private volatile Response data;
    
    private volatile Throwable throwable;
    
    public FailoverClosureImpl(final CompletableFuture<Response> future) {
        this.future = future;
    }
    
    @Override
    public void setResponse(Response data) {
        this.data = data;
    }
    
    @Override
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
    
    @Override
    public void run(Status status) {
        if (status.isOk()) {
            future.complete(data);
            return;
        }
        final Throwable throwable = this.throwable;
        future.completeExceptionally(Objects.nonNull(throwable) ? new ConsistencyException(throwable.toString())
                : new ConsistencyException("operation failure"));
    }
    
}
