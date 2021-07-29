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

package org.monkey.mmq.persistent;


import com.google.protobuf.ByteString;
import org.monkey.mmq.core.common.Constants;
import org.monkey.mmq.core.consistency.ProtocolMetaData;
import org.monkey.mmq.core.consistency.cp.CPProtocol;
import org.monkey.mmq.core.consistency.cp.MetadataKey;
import org.monkey.mmq.core.distributed.ProtocolManager;
import org.monkey.mmq.core.entity.ReadRequest;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.entity.WriteRequest;
import org.monkey.mmq.core.env.EnvUtil;
import org.monkey.mmq.core.exception.ErrorCode;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.utils.ByteUtils;
import org.monkey.mmq.core.utils.Loggers;
import org.monkey.mmq.core.utils.StringUtils;
import org.monkey.mmq.metadata.Datum;
import org.monkey.mmq.metadata.Record;
import org.monkey.mmq.metadata.RecordListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * In cluster mode, start the Raft protocol.
 *
 * @author solley
 */
@SuppressWarnings("PMD.ServiceOrDaoClassShouldEndWithImplRule")
public class PersistentServiceProcessor extends BasePersistentServiceProcessor {
    
    private final CPProtocol protocol;
    
    /**
     * Is there a leader node currently.
     */
    private volatile boolean hasLeader = false;
    
    public PersistentServiceProcessor(ProtocolManager protocolManager)
            throws Exception {
        this.protocol = protocolManager.getCpProtocol();
    }
    
    @Override
    public void afterConstruct() {
        super.afterConstruct();
        String raftGroup = Constants.MQTT_PERSISTENT_SERVICE_GROUP;
        this.protocol.protocolMetaData().subscribe(raftGroup, MetadataKey.LEADER_META_DATA, o -> {
            if (!(o instanceof ProtocolMetaData.ValueItem)) {
                return;
            }
            Object leader = ((ProtocolMetaData.ValueItem) o).getData();
            hasLeader = StringUtils.isNotBlank(String.valueOf(leader));
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
                .setGroup(Constants.MQTT_PERSISTENT_SERVICE_GROUP).setOperation(Op.Write.desc).build();
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
                .setGroup(Constants.MQTT_PERSISTENT_SERVICE_GROUP).setOperation(Op.Delete.desc).build();
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
        final ReadRequest req = ReadRequest.newBuilder().setGroup(Constants.MQTT_PERSISTENT_SERVICE_GROUP)
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
    
    @Override
    public void listen(String key, RecordListener listener) throws MmqException {
        notifier.registerListener(key, listener);
        notifierDatumIfAbsent(key, listener);
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
