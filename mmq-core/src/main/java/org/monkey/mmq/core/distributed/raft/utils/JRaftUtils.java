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


import com.alipay.sofa.jraft.CliService;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.alipay.sofa.jraft.util.RpcFactoryHelper;
import org.monkey.mmq.core.cluster.ServerMemberManager;
import org.monkey.mmq.core.consistency.SerializeFactory;
import org.monkey.mmq.core.distributed.raft.JRaftServer;
import org.monkey.mmq.core.distributed.raft.processor.MmqGetRequestProcessor;
import org.monkey.mmq.core.distributed.raft.processor.MmqLogProcessor;
import org.monkey.mmq.core.distributed.raft.processor.MmqReadRequestProcessor;
import org.monkey.mmq.core.distributed.raft.processor.MmqWriteRequestProcessor;
import org.monkey.mmq.core.entity.*;
import org.monkey.mmq.core.utils.ApplicationUtils;
import org.monkey.mmq.core.utils.DiskUtils;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.core.utils.ThreadUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JRaft utils.
 *
 * @author solley
 */
@SuppressWarnings("all")
public class JRaftUtils {
    
    public static RpcServer initRpcServer(JRaftServer server, PeerId peerId) {
//        GrpcRaftRpcFactory raftRpcFactory = (GrpcRaftRpcFactory) RpcFactoryHelper.rpcFactory();
//        raftRpcFactory.registerProtobufSerializer(Log.class.getName(), Log.getDefaultInstance());
//        raftRpcFactory.registerProtobufSerializer(GetRequest.class.getName(), GetRequest.getDefaultInstance());
//        raftRpcFactory.registerProtobufSerializer(WriteRequest.class.getName(), WriteRequest.getDefaultInstance());
//        raftRpcFactory.registerProtobufSerializer(ReadRequest.class.getName(), ReadRequest.getDefaultInstance());
//        raftRpcFactory.registerProtobufSerializer(Response.class.getName(), Response.getDefaultInstance());
//
//        MarshallerRegistry registry = raftRpcFactory.getMarshallerRegistry();
//        registry.registerResponseInstance(Log.class.getName(), Response.getDefaultInstance());
//        registry.registerResponseInstance(GetRequest.class.getName(), Response.getDefaultInstance());
//
//        registry.registerResponseInstance(WriteRequest.class.getName(), Response.getDefaultInstance());
//        registry.registerResponseInstance(ReadRequest.class.getName(), Response.getDefaultInstance());
        
        final RpcServer rpcServer = RaftRpcServerFactory.createRaftRpcServer(peerId.getEndpoint());
//        RaftRpcServerFactory.addRaftRequestProcessors(rpcServer, RaftExecutor.getRaftCoreExecutor(),
//                RaftExecutor.getRaftCliServiceExecutor());
        
        // Deprecated
        rpcServer.registerProcessor(new MmqLogProcessor(server, SerializeFactory.getDefault()));
        // Deprecated
        rpcServer.registerProcessor(new MmqGetRequestProcessor(server, SerializeFactory.getDefault()));
        
        rpcServer.registerProcessor(new MmqWriteRequestProcessor(server, SerializeFactory.getDefault()));
        rpcServer.registerProcessor(new MmqReadRequestProcessor(server, SerializeFactory.getDefault()));
        
        return rpcServer;
    }
    
    public static final void initDirectory(String parentPath, String groupName, NodeOptions copy) {
        final String logUri = Paths.get(parentPath, groupName, "log").toString();
        final String snapshotUri = Paths.get(parentPath, groupName, "snapshot").toString();
        final String metaDataUri = Paths.get(parentPath, groupName, "meta-data").toString();
        
        // Initialize the raft file storage path for different services
        try {
            DiskUtils.forceMkdir(new File(logUri));
            DiskUtils.forceMkdir(new File(snapshotUri));
            DiskUtils.forceMkdir(new File(metaDataUri));
        } catch (Exception e) {
            Loggers.RAFT.error("Init Raft-File dir have some error, cause: ", e);
            throw new RuntimeException(e);
        }
        
        copy.setLogUri(logUri);
        copy.setRaftMetaUri(metaDataUri);
        copy.setSnapshotUri(snapshotUri);
    }

    public static List<String> toStrings(List<PeerId> peerIds) {
        return peerIds.stream().map(peerId -> peerId.getEndpoint().toString()).collect(Collectors.toList());
    }
    
    public static void joinCluster(CliService cliService, Collection<String> members, Configuration conf, String group,
                                   PeerId self) {
        ServerMemberManager memberManager = ApplicationUtils.getBean(ServerMemberManager.class);
        if (!memberManager.isFirstIp()) {
            return;
        }
        Set<PeerId> peerIds = new HashSet<>();
        for (String s : members) {
            peerIds.add(PeerId.parsePeer(s));
        }
        peerIds.remove(self);
        for (; ; ) {
            if (peerIds.isEmpty()) {
                return;
            }
            conf = RouteTable.getInstance().getConfiguration(group);
            Iterator<PeerId> iterator = peerIds.iterator();
            while (iterator.hasNext()) {
                final PeerId peerId = iterator.next();
                
                if (conf.contains(peerId)) {
                    iterator.remove();
                    continue;
                }
                
                Status status = cliService.addPeer(group, conf, peerId);
                if (status.isOk()) {
                    iterator.remove();
                }
            }
            ThreadUtils.sleep(1000L);
        }
    }
    
}
