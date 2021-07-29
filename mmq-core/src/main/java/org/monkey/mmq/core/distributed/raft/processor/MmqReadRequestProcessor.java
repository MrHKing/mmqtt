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

package org.monkey.mmq.core.distributed.raft.processor;


import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import org.monkey.mmq.core.consistency.Serializer;
import org.monkey.mmq.core.distributed.raft.JRaftServer;
import org.monkey.mmq.core.entity.ReadRequest;

/**
 * mmq request processor for {@link org.monkey.mmq.core.entity.ReadRequest}.
 *
 * @author solley
 */
public class MmqReadRequestProcessor extends AbstractProcessor implements RpcProcessor<ReadRequest> {
    
    private static final String INTEREST_NAME = ReadRequest.class.getName();
    
    private final JRaftServer server;
    
    public MmqReadRequestProcessor(JRaftServer server, Serializer serializer) {
        super(serializer);
        this.server = server;
    }
    
    @Override
    public void handleRequest(RpcContext rpcCtx, ReadRequest request) {
        handleRequest(server, request.getGroup(), rpcCtx, request);
    }
    
    @Override
    public String interest() {
        return INTEREST_NAME;
    }
}
