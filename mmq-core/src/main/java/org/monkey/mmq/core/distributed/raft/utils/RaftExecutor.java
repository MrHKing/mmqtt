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


import org.monkey.mmq.core.distributed.raft.JRaftServer;
import org.monkey.mmq.core.distributed.raft.RaftConfig;
import org.monkey.mmq.core.distributed.raft.RaftSysConstants;
import org.monkey.mmq.core.executor.ExecutorFactory;
import org.monkey.mmq.core.executor.NameThreadFactory;
import org.monkey.mmq.core.utils.ClassUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * raft executor.
 *
 * @author solley
 */
public final class RaftExecutor {
    
    private static ExecutorService raftCoreExecutor;
    
    private static ExecutorService raftCliServiceExecutor;
    
    private static ScheduledExecutorService raftCommonExecutor;
    
    private static ExecutorService raftSnapshotExecutor;
    
    private static final String OWNER = ClassUtils.getCanonicalName(JRaftServer.class);
    
    private RaftExecutor() {
    }
    
    /**
     * init raft executor by {@link RaftConfig}.
     *
     * @param config {@link RaftConfig}
     */
    public static void init(RaftConfig config) {
        
        int raftCoreThreadNum = Integer.parseInt(config.getValOfDefault(RaftSysConstants.RAFT_CORE_THREAD_NUM, "8"));
        int raftCliServiceThreadNum = Integer
                .parseInt(config.getValOfDefault(RaftSysConstants.RAFT_CLI_SERVICE_THREAD_NUM, "4"));
        
        raftCoreExecutor = ExecutorFactory.Managed.newFixedExecutorService(OWNER, raftCoreThreadNum,
                new NameThreadFactory("org.monkey.mmq.core.raft-core"));
        
        raftCliServiceExecutor = ExecutorFactory.Managed.newFixedExecutorService(OWNER, raftCliServiceThreadNum,
                new NameThreadFactory("org.monkey.mmq.core.raft-cli-service"));
        
        raftCommonExecutor = ExecutorFactory.Managed.newScheduledExecutorService(OWNER, 8,
                new NameThreadFactory("org.monkey.mmq.core.protocol.raft-common"));
        
        int snapshotNum = raftCoreThreadNum / 2;
        snapshotNum = snapshotNum == 0 ? raftCoreThreadNum : snapshotNum;
        
        raftSnapshotExecutor = ExecutorFactory.Managed.newFixedExecutorService(OWNER, snapshotNum,
                        new NameThreadFactory("org.monkey.mmq.core.raft-snapshot"));
        
    }
    
    public static void scheduleRaftMemberRefreshJob(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        raftCommonExecutor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }
    
    public static ExecutorService getRaftCoreExecutor() {
        return raftCoreExecutor;
    }
    
    public static ExecutorService getRaftCliServiceExecutor() {
        return raftCliServiceExecutor;
    }
    
    public static void executeByCommon(Runnable r) {
        raftCommonExecutor.execute(r);
    }
    
    public static void scheduleByCommon(Runnable r, long delayMs) {
        raftCommonExecutor.schedule(r, delayMs, TimeUnit.MILLISECONDS);
    }
    
    public static void scheduleAtFixedRateByCommon(Runnable command, long initialDelayMs, long periodMs) {
        raftCommonExecutor.scheduleAtFixedRate(command, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }
    
    public static ScheduledExecutorService getRaftCommonExecutor() {
        return raftCommonExecutor;
    }
    
    public static void doSnapshot(Runnable runnable) {
        raftSnapshotExecutor.execute(runnable);
    }
    
}
