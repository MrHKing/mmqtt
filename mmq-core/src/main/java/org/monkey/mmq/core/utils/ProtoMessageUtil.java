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

import com.google.protobuf.Message;
import org.monkey.mmq.core.distributed.raft.exception.ConsistencyException;
import org.monkey.mmq.core.entity.GetRequest;
import org.monkey.mmq.core.entity.Log;
import org.monkey.mmq.core.entity.ReadRequest;
import org.monkey.mmq.core.entity.WriteRequest;

/**
 * protobuf message utils.
 *
 * @author solley
 */
public class ProtoMessageUtil {
    
    /**
     * Converts the byte array to a specific Protobuf object.
     * Internally, the protobuf new and old objects are compatible.
     *
     * @param bytes An array of bytes
     * @return Message
     */
    public static Message parse(byte[] bytes) {
        Message result;
        try {
            result = WriteRequest.parseFrom(bytes);
            return result;
        } catch (Throwable ignore) {
        }
        try {
            result = ReadRequest.parseFrom(bytes);
            return result;
        } catch (Throwable ignore) {
        }
        
        // old consistency entity, will be @Deprecated in future
        try {
            Log log = Log.parseFrom(bytes);
            return convertToWriteRequest(log);
        } catch (Throwable ignore) {
        }
        
        try {
            GetRequest request = GetRequest.parseFrom(bytes);
            return convertToReadRequest(request);
        } catch (Throwable ignore) {
        }
        
        throw new ConsistencyException("The current array cannot be serialized to the corresponding object");
    }
    
    /**
     * convert Log to WriteRequest.
     *
     * @param log log
     * @return {@link WriteRequest}
     */
    public static WriteRequest convertToWriteRequest(Log log) {
        return WriteRequest.newBuilder().setKey(log.getKey()).setGroup(log.getGroup())
                .setData(log.getData())
                .setType(log.getType())
                .setOperation(log.getOperation())
                .putAllExtendInfo(log.getExtendInfoMap())
                .build();
    }
    
    /**
     * convert Log to ReadRequest.
     *
     * @param request request
     * @return {@link ReadRequest}
     */
    public static ReadRequest convertToReadRequest(GetRequest request) {
        return ReadRequest.newBuilder()
                .setGroup(request.getGroup())
                .setData(request.getData())
                .putAllExtendInfo(request.getExtendInfoMap())
                .build();
    }
}
