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


import com.alipay.sofa.jraft.Node;
import org.monkey.mmq.core.cluster.Member;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.consistency.ProtocolMetaData;
import org.monkey.mmq.core.consistency.SerializeFactory;
import org.monkey.mmq.core.consistency.Serializer;
import org.monkey.mmq.core.consistency.cp.CPProtocol;
import org.monkey.mmq.core.consistency.cp.MetadataKey;
import org.monkey.mmq.core.consistency.cp.RequestProcessor4CP;
import org.monkey.mmq.core.consistency.model.RestResult;
import org.monkey.mmq.core.distributed.AbstractConsistencyProtocol;
import org.monkey.mmq.core.distributed.raft.exception.NoSuchRaftGroupException;
import org.monkey.mmq.core.entity.ReadRequest;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.entity.WriteRequest;
import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.listener.Subscriber;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.core.utils.MapUtil;
import org.monkey.mmq.core.utils.ThreadUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A concrete implementation of CP protocol: JRaft.
 *
 * <pre>
 *                                           ┌──────────────────────┐
 *            ┌──────────────────────┐       │                      ▼
 *            │   ProtocolManager    │       │        ┌───────────────────────────┐
 *            └──────────────────────┘       │        │for p in [LogProcessor4CP] │
 *                        │                  │        └───────────────────────────┘
 *                        ▼                  │                      │
 *      ┌──────────────────────────────────┐ │                      ▼
 *      │    discovery LogProcessor4CP     │ │             ┌─────────────────┐
 *      └──────────────────────────────────┘ │             │  get p.group()  │
 *                        │                  │             └─────────────────┘
 *                        ▼                  │                      │
 *                 ┌─────────────┐           │                      │
 *                 │ RaftConfig  │           │                      ▼
 *                 └─────────────┘           │      ┌──────────────────────────────┐
 *                        │                  │      │  create raft group service   │
 *                        ▼                  │      └──────────────────────────────┘
 *              ┌──────────────────┐         │
 *              │  JRaftProtocol   │         │
 *              └──────────────────┘         │
 *                        │                  │
 *                     init()                │
 *                        │                  │
 *                        ▼                  │
 *               ┌─────────────────┐         │
 *               │   JRaftServer   │         │
 *               └─────────────────┘         │
 *                        │                  │
 *                        │                  │
 *                        ▼                  │
 *             ┌────────────────────┐        │
 *             │JRaftServer.start() │        │
 *             └────────────────────┘        │
 *                        │                  │
 *                        └──────────────────┘
 * </pre>
 *
 * @author solley
 */
@SuppressWarnings("all")
public class JRaftProtocol extends AbstractConsistencyProtocol<RaftConfig, RequestProcessor4CP>
        implements CPProtocol<RaftConfig, RequestProcessor4CP> {
    
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private final AtomicBoolean shutdowned = new AtomicBoolean(false);
    
    private final Serializer serializer = SerializeFactory.getDefault();
    
    private RaftConfig raftConfig;
    
    private JRaftServer raftServer;
    
    private JRaftMaintainService jRaftMaintainService;
    
    private ServerMemberManager memberManager;
    
    public JRaftProtocol(ServerMemberManager memberManager) throws Exception {
        this.memberManager = memberManager;
        this.raftServer = new JRaftServer();
        this.jRaftMaintainService = new JRaftMaintainService(raftServer);
    }
    
    @Override
    public void init(RaftConfig config) {
        if (initialized.compareAndSet(false, true)) {
            this.raftConfig = config;
            NotifyCenter.registerToSharePublisher(RaftEvent.class);
            this.raftServer.init(this.raftConfig);
            this.raftServer.start();
            
            // There is only one consumer to ensure that the internal consumption
            // is sequential and there is no concurrent competition
            NotifyCenter.registerSubscriber(new Subscriber<RaftEvent>() {
                @Override
                public void onEvent(RaftEvent event) {
                    Loggers.RAFT.info("This Raft event changes : {}", event);
                    final String groupId = event.getGroupId();
                    Map<String, Map<String, Object>> value = new HashMap<>();
                    Map<String, Object> properties = new HashMap<>();
                    final String leader = event.getLeader();
                    final Long term = event.getTerm();
                    final List<String> raftClusterInfo = event.getRaftClusterInfo();
                    final String errMsg = event.getErrMsg();
                    
                    // Leader information needs to be selectively updated. If it is valid data,
                    // the information in the protocol metadata is updated.
                    MapUtil.putIfValNoEmpty(properties, MetadataKey.LEADER_META_DATA, leader);
                    MapUtil.putIfValNoNull(properties, MetadataKey.TERM_META_DATA, term);
                    MapUtil.putIfValNoEmpty(properties, MetadataKey.RAFT_GROUP_MEMBER, raftClusterInfo);
                    MapUtil.putIfValNoEmpty(properties, MetadataKey.ERR_MSG, errMsg);
                    
                    value.put(groupId, properties);
                    metaData.load(value);
                    
                    // The metadata information is injected into the metadata information of the node
                    injectProtocolMetaData(metaData);
                }
                
                @Override
                public Class<? extends Event> subscribeType() {
                    return RaftEvent.class;
                }
                
            });
        }
    }
    
    @Override
    public void addRequestProcessors(Collection<RequestProcessor4CP> processors) {
        raftServer.createMultiRaftGroup(processors);
    }
    
    @Override
    public Response getData(ReadRequest request) throws Exception {
        CompletableFuture<Response> future = aGetData(request);
        return future.get(5_000L, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public CompletableFuture<Response> aGetData(ReadRequest request) {
        return raftServer.get(request);
    }
    
    @Override
    public Response write(WriteRequest request) throws Exception {
        CompletableFuture<Response> future = writeAsync(request);
        // Here you wait for 10 seconds, as long as possible, for the request to complete
        return future.get(10_000L, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public CompletableFuture<Response> writeAsync(WriteRequest request) {
        return raftServer.commit(request.getGroup(), request, new CompletableFuture<>());
    }
    
    @Override
    public void memberChange(Set<String> addresses) {
        for (int i = 0; i < 5; i++) {
            if (this.raftServer.peerChange(jRaftMaintainService, addresses)) {
                return;
            }
            ThreadUtils.sleep(100L);
        }
        Loggers.RAFT.warn("peer removal failed");
    }
    
    @Override
    public void shutdown() {
        if (initialized.get() && shutdowned.compareAndSet(false, true)) {
            Loggers.RAFT.info("shutdown jraft server");
            raftServer.shutdown();
        }
    }
    
    @Override
    public RestResult<String> execute(Map<String, String> args) {
        return jRaftMaintainService.execute(args);
    }
    
    private void injectProtocolMetaData(ProtocolMetaData metaData) {
        Member member = memberManager.getSelf();
        member.setExtendVal("raftMetaData", metaData);
        memberManager.update(member);
    }
    
    @Override
    public boolean isLeader(String group) {
        Node node = raftServer.findNodeByGroup(group);
        if (node == null) {
            throw new NoSuchRaftGroupException(group);
        }
        return node.isLeader();
    }
}
