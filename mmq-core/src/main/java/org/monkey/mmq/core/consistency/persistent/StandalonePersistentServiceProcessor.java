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
import org.monkey.mmq.core.consistency.matedata.Datum;
import org.monkey.mmq.core.consistency.matedata.Record;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.entity.ReadRequest;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.entity.WriteRequest;
import org.monkey.mmq.core.exception.ErrorCode;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.ByteUtils;


import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Persistent service manipulation layer in stand-alone mode.
 *
 * @author solley
 */
@SuppressWarnings("PMD.ServiceOrDaoClassShouldEndWithImplRule")
public class StandalonePersistentServiceProcessor extends BasePersistentServiceProcessor {

    private Function<String, Class<? extends Record>> getClassOfRecordFromKey;

    private final String raftGroup;

    public StandalonePersistentServiceProcessor(String kvStorageBaseDir, String raftGroup,
                                                Function<String, Class<? extends Record>> getClassOfRecordFromKey) throws Exception {
        super(kvStorageBaseDir, raftGroup, getClassOfRecordFromKey);
        super.afterConstruct();
        this.raftGroup = raftGroup;
        this.getClassOfRecordFromKey = getClassOfRecordFromKey;
    }
    
    @Override
    public void put(String key, Record value) throws MmqException {
        final BatchWriteRequest req = new BatchWriteRequest();
        Datum datum = Datum.createDatum(key, value);
        req.append(ByteUtils.toBytes(key), serializer.serialize(datum));
        final WriteRequest request = WriteRequest.newBuilder().setData(ByteString.copyFrom(serializer.serialize(req)))
                .setGroup(this.raftGroup).setOperation(Op.Write.desc).build();
        try {
            onApply(request);
        } catch (Exception e) {
            throw new MmqException(ErrorCode.ProtoSubmitError.getCode(), e.getMessage());
        }
    }

    @Override
    public Datum get(String key) throws MmqException {
        final List<byte[]> keys = Collections.singletonList(ByteUtils.toBytes(key));
        final ReadRequest req = ReadRequest.newBuilder().setGroup(this.raftGroup)
                .setData(ByteString.copyFrom(serializer.serialize(keys))).build();
        try {
            final Response resp = onRequest(req);
            if (resp.getSuccess()) {
                BatchReadResponse response = serializer
                        .deserialize(resp.getData().toByteArray(), BatchReadResponse.class);
                final List<byte[]> rValues = response.getValues();
                return rValues.isEmpty() ? null : serializer.deserialize(rValues.get(0),  getDatumTypeFromKey(key));
            }
            throw new MmqException(ErrorCode.ProtoReadError.getCode(), resp.getErrMsg());
        } catch (Throwable e) {
            throw new MmqException(ErrorCode.ProtoReadError.getCode(), e.getMessage());
        }
    }
    
    @Override
    public void remove(String key) throws MmqException {
        final BatchWriteRequest req = new BatchWriteRequest();
        req.append(ByteUtils.toBytes(key), ByteUtils.EMPTY);
        final WriteRequest request = WriteRequest.newBuilder().setData(ByteString.copyFrom(serializer.serialize(req)))
                .setGroup(this.raftGroup).setOperation(Op.Delete.desc).build();
        try {
            onApply(request);
        } catch (Exception e) {
            throw new MmqException(ErrorCode.ProtoSubmitError.getCode(), e.getMessage());
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
        return !hasError;
    }
    
    @Override
    public Optional<String> getErrorMsg() {
        String errorMsg;
        if (hasError) {
            errorMsg = "The raft peer is in error: " + jRaftErrorMsg;
        } else {
            errorMsg = null;
        }
        return Optional.ofNullable(errorMsg);
    }
}
