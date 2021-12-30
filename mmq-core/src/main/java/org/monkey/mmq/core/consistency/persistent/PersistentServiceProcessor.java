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

package org.monkey.mmq.core.consistency.persistent;


import com.google.protobuf.ByteString;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.monkey.mmq.core.common.Constants;
import org.monkey.mmq.core.consistency.ProtocolMetaData;
import org.monkey.mmq.core.consistency.cp.CPProtocol;
import org.monkey.mmq.core.consistency.cp.MetadataKey;
import org.monkey.mmq.core.consistency.matedata.Datum;
import org.monkey.mmq.core.consistency.matedata.Record;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.distributed.ProtocolManager;
import org.monkey.mmq.core.entity.ReadRequest;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.entity.WriteRequest;
import org.monkey.mmq.core.exception.ErrorCode;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.ByteUtils;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.core.utils.StringUtils;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * In cluster mode, start the Raft protocol.
 *
 * @author solley
 */
@SuppressWarnings("PMD.ServiceOrDaoClassShouldEndWithImplRule")
public class PersistentServiceProcessor extends BasePersistentServiceProcessor {
    
    private final CPProtocol protocol;

    private final String raftGroup;

    private Function<String, Class<? extends Record>> getClassOfRecordFromKey;
    
    /**
     * Is there a leader node currently.
     */
    private volatile boolean hasLeader = false;
    
    public PersistentServiceProcessor(ProtocolManager protocolManager, String kvStorageBaseDir,
                                      String raftGroup,
                                      Function<String, Class<? extends Record>> getClassOfRecordFromKey)
            throws Exception {
        super(kvStorageBaseDir, raftGroup, getClassOfRecordFromKey);
        this.raftGroup = raftGroup;
        this.protocol = protocolManager.getCpProtocol();
        this.getClassOfRecordFromKey = getClassOfRecordFromKey;
    }
    
    @Override
    public void afterConstruct() {
        super.afterConstruct();
        this.protocol.protocolMetaData().subscribe(raftGroup, MetadataKey.LEADER_META_DATA, o -> {
            if (!(o instanceof ProtocolMetaData.ValueItem)) {
                return;
            }
            Object leader = ((ProtocolMetaData.ValueItem) o).getData();
            hasLeader =  StringUtils.isNotEmpty((String) leader);
            Loggers.RAFT.info("Raft group {} has leader {}", raftGroup, leader);
        });
        this.protocol.addRequestProcessors(Collections.singletonList(this));
        waitLeader();
    }
    
    private void waitLeader() {
        while (!hasLeader && !hasError) {
            Loggers.RAFT.info("Waiting Jraft leader vote ...");
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
    }
    
    @Override
    public void put(String key, Record value) throws MmqException {
        final BatchWriteRequest req = new BatchWriteRequest();
        Datum datum = Datum.createDatum(key, value);
        req.append(ByteUtils.toBytes(key), serializer.serialize(datum));
        final WriteRequest request = WriteRequest.newBuilder().setData(ByteString.copyFrom(serializer.serialize(req)))
                .setGroup(this.raftGroup).setOperation(Op.Write.desc).build();
        try {
            protocol.write(request);
        } catch (Exception e) {
            throw new MmqException(ErrorCode.ProtoSubmitError.getCode(), e.getMessage());
        }
    }

    @Override
    public void remove(String key) throws MmqException {
        final BatchWriteRequest req = new BatchWriteRequest();
        req.append(ByteUtils.toBytes(key), ByteUtils.EMPTY);
        final WriteRequest request = WriteRequest.newBuilder().setData(ByteString.copyFrom(serializer.serialize(req)))
                .setGroup(this.raftGroup).setOperation(Op.Delete.desc).build();
        try {
            protocol.write(request);
        } catch (Exception e) {
            throw new MmqException(ErrorCode.ProtoSubmitError.getCode(), e.getMessage());
        }
    }
    
    @Override
    public Datum get(String key) throws MmqException {
        final List<byte[]> keys = new ArrayList<>(1);
        keys.add(ByteUtils.toBytes(key));
        final ReadRequest req = ReadRequest.newBuilder().setGroup(this.raftGroup)
                .setData(ByteString.copyFrom(serializer.serialize(keys))).build();
        try {
            Response resp = protocol.getData(req);
            if (resp.getSuccess()) {
                BatchReadResponse response = serializer
                        .deserialize(resp.getData().toByteArray(), BatchReadResponse.class);
                final List<byte[]> rValues = response.getValues();
                return rValues.isEmpty() ? null : serializer.deserialize(rValues.get(0), getDatumTypeFromKey(key));
            }
            throw new MmqException(ErrorCode.ProtoReadError.getCode(), resp.getErrMsg());
        } catch (Throwable e) {
            throw new MmqException(ErrorCode.ProtoReadError.getCode(), e.getMessage());
        }
    }

    protected Type getDatumTypeFromKey(String key) {
        return TypeUtils.parameterize(Datum.class, getClassOfRecordFromKey.apply(key));
    }
    
    @Override
    public void listen(String key, RecordListener listener) throws MmqException {
        notifier.registerListener(key, listener);
        notifierAllServiceMeta(listener);
    }
    
    @Override
    public void unListen(String key, RecordListener listener) throws MmqException {
        notifier.deregisterListener(key, listener);
    }
    
    @Override
    public boolean isAvailable() {
        return hasLeader && !hasError;
    }
    
    @Override
    public Optional<String> getErrorMsg() {
        String errorMsg;
        if (hasLeader && hasError) {
            errorMsg = "The raft peer is in error: " + jRaftErrorMsg;
        } else if (hasLeader && !hasError) {
            errorMsg = null;
        } else if (!hasLeader && hasError) {
            errorMsg = "Could not find leader! And the raft peer is in error: " + jRaftErrorMsg;
        } else {
            errorMsg = "Could not find leader!";
        }
        return Optional.ofNullable(errorMsg);
    }
}
