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
package org.monkey.mmq.config.matedata;

import org.monkey.mmq.config.matedata.ResourceEnum;
import org.monkey.mmq.core.consistency.matedata.Record;

import java.io.Serializable;
import java.util.Map;

/**
 * Health Controller.
 *
 * @author solley
 */
public class ResourcesMateData<T> implements Record, Serializable {
    private static final long serialVersionUID = 1276156087085594264L;

    private String resourceID;

    private String resourceName;

    private String description;

    private ResourceEnum type;

    private Map<String, Object> resource;

    public ResourceEnum getType() {
        return type;
    }

    public void setType(ResourceEnum type) {
        this.type = type;
    }

    public Map<String, Object> getResource() {
        return resource;
    }

    public void setResource(Map<String, Object> resource) {
        this.resource = resource;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
