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


import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Persistent service manipulation layer in stand-alone mode.
 *
 * @author solley
 */
@SuppressWarnings("PMD.ServiceOrDaoClassShouldEndWithImplRule")
public class StandalonePersistentServiceProcessor extends BasePersistentServiceProcessor {
    
    public StandalonePersistentServiceProcessor(String kvStorageBaseDir) throws Exception {
        super(kvStorageBaseDir);
        super.afterConstruct();

    }
    
    @Override
    public void put(String key, Record value) throws MmqException {
        final BatchWriteRequest req = new BatchWriteRequest();
        Datum datum = Datum.createDatum(key, value);
        req.append(ByteUtils.toBytes(key), serializer.serialize(datum));
        final WriteRequest request = WriteRequest.newBuilder().setData(ByteString.copyFrom(serializer.serialize(req)))
                .setGroup(Constants.MQTT_PERSISTENT_SERVICE_GROUP).setOperation(Op.Write.desc).build();
        try {
            onApply(request);
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
            onApply(request);
        } catch (Exception e) {
            throw new MmqException(ErrorCode.ProtoSubmitError.getCode(), e.getMessage());
        }
    }
    
    @Override
    public Datum get(String key) throws MmqException {
        final List<byte[]> keys = Collections.singletonList(ByteUtils.toBytes(key));
        final ReadRequest req = ReadRequest.newBuilder().setGroup(Constants.MQTT_PERSISTENT_SERVICE_GROUP)
                .setData(ByteString.copyFrom(serializer.serialize(keys))).build();
        try {
            final Response resp = onRequest(req);
            if (resp.getSuccess()) {
                BatchReadResponse response = serializer
                        .deserialize(resp.getData().toByteArray(), BatchReadResponse.class);
                final List<byte[]> rValues = response.getValues();
                return rValues.isEmpty() ? null : serializer.deserialize(rValues.get(0));
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
