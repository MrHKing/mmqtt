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

package org.monkey.mmq.core.cluster.lookup;

import org.apache.commons.lang3.StringUtils;
import org.monkey.mmq.core.cluster.AbstractMemberLookup;
import org.monkey.mmq.core.cluster.Member;
import org.monkey.mmq.core.cluster.MemberUtil;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.file.FileChangeEvent;
import org.monkey.mmq.core.file.FileWatcher;
import org.monkey.mmq.core.file.WatchFileCenter;
import org.monkey.mmq.core.utils.Loggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Cluster.conf file managed cluster member node addressing pattern.
 *
 * @author solley
 */
public class FileConfigMemberLookup extends AbstractMemberLookup {
    
    private static final String DEFAULT_SEARCH_SEQ = "cluster.conf";
    
    private FileWatcher watcher = new FileWatcher() {
        @Override
        public void onChange(FileChangeEvent event) {
            readClusterConfFromDisk();
        }
        
        @Override
        public boolean interest(String context) {
            return StringUtils.contains(context, DEFAULT_SEARCH_SEQ);
        }
    };
    
    @Override
    public void doStart() throws MmqException {
        readClusterConfFromDisk();
        
        // Use the inotify mechanism to monitor file changes and automatically
        // trigger the reading of cluster.conf
        try {
            WatchFileCenter.registerWatcher(EnvUtil.getConfPath(), watcher);
        } catch (Throwable e) {
            Loggers.CLUSTER.error("An exception occurred in the launch file monitor : {}", e.getMessage());
        }
    }
    
    @Override
    public boolean useAddressServer() {
        return false;
    }
    
    @Override
    public void destroy() throws MmqException {
        WatchFileCenter.deregisterWatcher(EnvUtil.getConfPath(), watcher);
    }
    
    private void readClusterConfFromDisk() {
        Collection<Member> tmpMembers = new ArrayList<>();
        try {
            List<String> tmp = EnvUtil.readClusterConf();
            tmpMembers = MemberUtil.readServerConf(tmp);
        } catch (Throwable e) {
            Loggers.CLUSTER
                    .error("mmq-XXXX [serverlist] failed to get serverlist from disk!, error : {}", e.getMessage());
        }
        
        afterLookup(tmpMembers);
    }
}
