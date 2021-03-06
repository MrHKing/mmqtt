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
package org.monkey.mmq.core.actor.metadata.message;

import org.monkey.mmq.core.consistency.matedata.Record;

import java.io.Serializable;
import java.util.Date;

/**
 * Health Controller.
 *
 * @author solley
 */
public class PublishMateData implements Record, Serializable {

    private static final long serialVersionUID = 5209539791996944490L;

    private String outInType;

    private int bytes;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }


    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public String getOutInType() {
        return outInType;
    }

    public void setOutInType(String outInType) {
        this.outInType = outInType;
    }
}
