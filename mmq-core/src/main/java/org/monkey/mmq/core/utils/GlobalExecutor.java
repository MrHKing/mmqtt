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

package org.monkey.mmq.core.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.executor.ExecutorFactory;
import org.monkey.mmq.core.executor.NameThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * core module global executor.
 *
 * @author solley
 */
@SuppressWarnings("all")
public class GlobalExecutor {
    
    private static final ScheduledExecutorService COMMON_EXECUTOR = ExecutorFactory.Managed
            .newScheduledExecutorService(ClassUtils.getCanonicalName(GlobalExecutor.class), 4,
                    new NameThreadFactory("org.monkey.mmq.core.common"));
    
    public static final ThreadPoolExecutor sdkRpcExecutor = new ThreadPoolExecutor(
            EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()),
            EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()), 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(RemoteUtils.getRemoteExecutorQueueSize()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("mmq-grpc-executor-%d").build());
    
    public static final ThreadPoolExecutor clusterRpcExecutor = new ThreadPoolExecutor(
            EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()),
            EnvUtil.getAvailableProcessors(RemoteUtils.getRemoteExecutorTimesOfProcessors()), 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(RemoteUtils.getRemoteExecutorQueueSize()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("mmq-cluster-grpc-executor-%d").build());
    
    public static void runWithoutThread(Runnable runnable) {
        runnable.run();
    }
    
    public static void executeByCommon(Runnable runnable) {
        if (COMMON_EXECUTOR.isShutdown()) {
            return;
        }
        COMMON_EXECUTOR.execute(runnable);
    }
    
    public static void scheduleByCommon(Runnable runnable, long delayMs) {
        if (COMMON_EXECUTOR.isShutdown()) {
            return;
        }
        COMMON_EXECUTOR.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
    }
}
