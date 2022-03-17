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

import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.core.consistency.matedata.Record;

import java.io.Serializable;
import java.util.List;

/**
 * @author solley
 */
public class RuleEngineMateData implements Record, Serializable {

    private String ruleId;

    private String name;

    private String sql;

    private String description;

    private Boolean enable;

    private List<ResourcesMateData> resourcesMateDatas;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String id) {
        this.ruleId = id;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ResourcesMateData> getResourcesMateDatas() {
        return resourcesMateDatas;
    }

    public void setResourcesMateDatas(List<ResourcesMateData> resourcesMateDatas) {
        this.resourcesMateDatas = resourcesMateDatas;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
}
