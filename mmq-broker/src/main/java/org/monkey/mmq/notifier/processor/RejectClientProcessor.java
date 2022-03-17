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
package org.monkey.mmq.notifier.processor;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import org.monkey.mmq.core.entity.RejectClient;
import org.monkey.mmq.core.actor.metadata.message.SessionMateData;
import org.monkey.mmq.service.SessionStoreService;

/**
 * Health Controller.
 *
 * @author solley
 */
public class RejectClientProcessor extends SyncUserProcessor<RejectClient> {

    private static final String INTEREST_NAME = RejectClient.class.getName();

    private final SessionStoreService sessionStoreService;

    public RejectClientProcessor(SessionStoreService sessionStoreService) {
        this.sessionStoreService = sessionStoreService;
    }

    @Override
    public Object handleRequest(BizContext bizContext, RejectClient rejectClient) throws Exception {
        SessionMateData sessionMateData = sessionStoreService.get(rejectClient.getClientId());
        if (sessionMateData == null) return null;
        sessionMateData.getChannel().close();
        return null;
    }

    @Override
    public String interest() {
        return INTEREST_NAME;
    }
}
