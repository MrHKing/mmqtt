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
import org.monkey.mmq.core.consistency.DataOperation;
import org.monkey.mmq.core.consistency.SerializeFactory;
import org.monkey.mmq.core.consistency.Serializer;
import org.monkey.mmq.core.consistency.cp.RequestProcessor4CP;
import org.monkey.mmq.core.consistency.matedata.Datum;
import org.monkey.mmq.core.consistency.matedata.Record;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.consistency.notifier.PersistentNotifier;
import org.monkey.mmq.core.consistency.notifier.ValueChangeEvent;
import org.monkey.mmq.core.consistency.snapshot.SnapshotOperation;
import org.monkey.mmq.core.entity.ReadRequest;
import org.monkey.mmq.core.entity.Response;
import org.monkey.mmq.core.entity.WriteRequest;
import org.monkey.mmq.core.exception.KvStorageException;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.exception.runtime.MmqRuntimeException;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.storage.kv.KvStorage;
import org.monkey.mmq.core.utils.ByteUtils;
import org.monkey.mmq.core.utils.Loggers;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * New service data persistence handler.
 *
 * @author solley
 */
public abstract class BasePersistentServiceProcessor extends RequestProcessor4CP
        implements PersistentConsistencyService {
    
    enum Op {
        /**
         * write ops.
         */
        Write("Write"),
        
        /**
         * read ops.
         */
        Read("Read"),
        
        /**
         * delete ops.
         */
        Delete("Delete");
        
        protected final String desc;
        
        Op(String desc) {
            this.desc = desc;
        }
    }
    
    protected final MmqKvStorage kvStorage;
    
    protected final Serializer serializer;

    private final String raftGroup;
    
    /**
     * Whether an unrecoverable error occurred.
     */
    protected volatile boolean hasError = false;
    
    protected volatile String jRaftErrorMsg;
    
    /**
     * During snapshot processing, the processing of other requests needs to be paused.
     */
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    protected final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    private Function<String, Class<? extends Record>> getClassOfRecordFromKey;
    
    protected final PersistentNotifier notifier;
    
    protected final int queueMaxSize = 16384;
    
    public BasePersistentServiceProcessor(String kvStorageBaseDir, String raftGroup, Function<String, Class<? extends Record>> getClassOfRecordFromKey) throws Exception {
        this.kvStorage = new MmqKvStorage(kvStorageBaseDir);
        this.raftGroup = raftGroup;
        this.serializer = SerializeFactory.getSerializer("JSON");
        this.getClassOfRecordFromKey = getClassOfRecordFromKey;
        this.notifier = new PersistentNotifier(key -> {
            try {
                byte[] data = kvStorage.get(ByteUtils.toBytes(key));
                Datum datum = serializer.deserialize(data, getDatumTypeFromKey(key));
                return null != datum ? datum.value : null;
            } catch (KvStorageException ex) {
                throw new MmqRuntimeException(ex.getErrCode(), ex.getErrMsg());
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    public void afterConstruct() {
        NotifyCenter.registerToPublisher(ValueChangeEvent.class, queueMaxSize);
        NotifyCenter.registerSubscriber(notifier);
    }
    
    @Override
    public Response onRequest(ReadRequest request) {
        final List<byte[]> keys = serializer
                .deserialize(request.getData().toByteArray(), TypeUtils.parameterize(List.class, byte[].class));
        final Lock lock = readLock;
        lock.lock();
        try {
            final Map<byte[], byte[]> result = kvStorage.batchGet(keys);
            final BatchReadResponse response = new BatchReadResponse();
            result.forEach(response::append);
            return Response.newBuilder().setSuccess(true).setData(ByteString.copyFrom(serializer.serialize(response)))
                    .build();
        } catch (KvStorageException e) {
            return Response.newBuilder().setSuccess(false).setErrMsg(e.getErrMsg()).build();
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public Response onApply(WriteRequest request) {
        final byte[] data = request.getData().toByteArray();
        final BatchWriteRequest bwRequest = serializer.deserialize(data, BatchWriteRequest.class);
        final Op op = Op.valueOf(request.getOperation());
        final Lock lock = readLock;
        lock.lock();
        try {
            switch (op) {
                case Write:
                    kvStorage.batchPut(bwRequest.getKeys(), bwRequest.getValues());
                    break;
                case Delete:
                    kvStorage.batchDelete(bwRequest.getKeys());
                    break;
                default:
                    return Response.newBuilder().setSuccess(false).setErrMsg("unsupport operation : " + op).build();
            }
            publishValueChangeEvent(op, bwRequest);
            return Response.newBuilder().setSuccess(true).build();
        } catch (KvStorageException e) {
            return Response.newBuilder().setSuccess(false).setErrMsg(e.getErrMsg()).build();
        } finally {
            lock.unlock();
        }
    }
    
    private void publishValueChangeEvent(final Op op, final BatchWriteRequest request) {
        final List<byte[]> keys = request.getKeys();
        final List<byte[]> values = request.getValues();
        for (int i = 0; i < keys.size(); i++) {
            final String key = new String(keys.get(i));
            final Datum datum = serializer.deserialize(values.get(i), getDatumTypeFromKey(key));
            final Record value = null != datum ? datum.value : null;
            final ValueChangeEvent event = ValueChangeEvent.builder().key(key).value(value)
                    .action(getDataOperationByOp(op)).build();
            NotifyCenter.publishEvent(event);
        }
    }

    protected Type getDatumTypeFromKey(String key) {
        return TypeUtils.parameterize(Datum.class, getClassOfRecordFromKey.apply(key));
    }

    private  DataOperation getDataOperationByOp(Op op) {
        switch (op) {
            case Write:
                return DataOperation.CHANGE;
            case Delete:
                return DataOperation.DELETE;
            default:
                return DataOperation.CHANGE;
        }
    }
    
    @Override
    public String group() {
        return this.raftGroup;
    }
    
    @Override
    public List<SnapshotOperation> loadSnapshotOperate() {
            return Collections.singletonList(new MmqSnapshotOperation(this.kvStorage, lock));
    }
    
    @Override
    public void onError(Throwable error) {
        super.onError(error);
        hasError = true;
        jRaftErrorMsg = error.getMessage();
    }

    protected void notifierDatumIfAbsent(String key, RecordListener listener) throws MmqException {
        Datum datum = get(key);
        if (null != datum) {
            notifierDatum(key, datum, listener);
        }
    }
    
    /**
     * This notify should only notify once during startup.
     */
    protected void notifierAllServiceMeta(RecordListener listener) throws MmqException {
        for (byte[] each : kvStorage.allKeys()) {
            String key = new String(each);
            if (listener.interests(key)) {
                Datum datum = get(key);
                if (null != datum) {
                    notifierDatum(key, datum, listener);
                }
            }
        }
    }
    
    private void notifierDatum(String key, Datum datum, RecordListener listener) {
        try {
            listener.onChange(key, datum.value);
        } catch (Exception e) {
            Loggers.RAFT.error("MMQ-RAFT failed to notify listener", e);
        }
    }
}
