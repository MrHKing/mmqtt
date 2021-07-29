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

package org.monkey.mmq.core.distributed;


import org.monkey.mmq.core.executor.ExecutorFactory;
import org.monkey.mmq.core.utils.ClassUtils;

import java.util.concurrent.ExecutorService;

/**
 * ProtocolExecutor.
 *
 * @author solley
 */
public final class ProtocolExecutor {
    
    private static final ExecutorService CP_MEMBER_CHANGE_EXECUTOR = ExecutorFactory.Managed
            .newSingleExecutorService(ClassUtils.getCanonicalName(ProtocolManager.class));
    
    private static final ExecutorService AP_MEMBER_CHANGE_EXECUTOR = ExecutorFactory.Managed
            .newSingleExecutorService(ClassUtils.getCanonicalName(ProtocolManager.class));
    
    public static void cpMemberChange(Runnable runnable) {
        CP_MEMBER_CHANGE_EXECUTOR.execute(runnable);
    }
    
    public static void apMemberChange(Runnable runnable) {
        AP_MEMBER_CHANGE_EXECUTOR.execute(runnable);
    }
    
}