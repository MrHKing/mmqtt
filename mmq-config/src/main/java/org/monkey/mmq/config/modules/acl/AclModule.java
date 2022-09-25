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

package org.monkey.mmq.config.modules.acl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.monkey.mmq.config.config.Loggers;
import org.monkey.mmq.config.matedata.ModelEnum;
import org.monkey.mmq.config.modules.BaseModule;
import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.config.service.ModulesService;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.monkey.mmq.config.config.Constants.ALLOW;
import static org.monkey.mmq.config.config.Constants.SUBSCRIBE_AND_PUBLISH;

/**
 * @ClassName:AclModule
 * @Auther: Solley
 * @Description: Acl
 * @Date: 2022/7/28 20:01
 * @Version: v1.0
 */
@Component
public class AclModule extends BaseModule<AclParam> {

    @Autowired
    ModulesService modulesService;

    private static final String DATA = "data";

    public boolean ipAccess(String ip, String topic) {
        if (!modelMateData.getEnable()) return true;
        if (StringUtils.isEmpty(ip)) return false;
        if (StringUtils.isEmpty(topic)) return false;
        if (this.listAllAclParams().stream()
                .filter(x -> ip.equals(x.getIpaddr()) && this.containsTopic(x.getTopic(), topic)).count() <= 0) return true;
        return this.listAllAclParams().stream()
                .filter(x -> ip.equals(x.getIpaddr())
                        && x.getAllow() == ALLOW
                        && this.containsTopic(x.getTopic(), topic)).count() > 0;
    }

    public boolean clientIdAccess(String clientId, String topic) {
        if (!modelMateData.getEnable()) return true;
        if (StringUtils.isEmpty(clientId)) return false;
        if (StringUtils.isEmpty(topic)) return false;
        try {
            if (this.listAllAclParams().stream()
                    .filter(x -> clientId.equals(x.getClientId()) && this.containsTopic(x.getTopic(), topic)).count() <= 0) return true;
            return this.listAllAclParams().stream()
                    .filter(x -> clientId.equals(x.getClientId())
                            && x.getAllow() == ALLOW
                            && this.containsTopic(x.getTopic(), topic)).count() > 0;
        } catch (Exception e) {
            Loggers.CONFIG_SERVER.error(e.getMessage());
            return false;
        }
    }

    public boolean userNameAccess(String userName, String topic) {
        if (!modelMateData.getEnable()) return true;
        if (StringUtils.isEmpty(userName)) return false;
        if (StringUtils.isEmpty(topic)) return false;
        if (this.listAllAclParams().stream()
                .filter(x -> userName.equals(x.getUsername()) && this.containsTopic(x.getTopic(), topic)).count() <= 0) return true;
        return this.listAllAclParams().stream()
                .filter(x -> userName.equals(x.getUsername())
                        && x.getAllow() == ALLOW
                        && this.containsTopic(x.getTopic(), topic)).count() > 0;
    }

    private boolean containsTopic(String subTopic, String pubTopic) {
        if (pubTopic.equals(subTopic)) {
            return true;
        }
        if (StrUtil.split(pubTopic, '/').size() >= StrUtil.split(subTopic, '/').size()) {
            List<String> splitTopics = StrUtil.split(pubTopic, '/');//a
            List<String> spliteTopicFilters = StrUtil.split(subTopic, '/');//#
            String newTopicFilter = "";
            for (int i = 0; i < spliteTopicFilters.size(); i++) {
                String value = spliteTopicFilters.get(i);
                if (value.equals("+")) {
                    newTopicFilter = newTopicFilter + "+/";
                } else if (value.equals("#")) {
                    newTopicFilter = newTopicFilter + "#/";
                    break;
                } else {
                    newTopicFilter = newTopicFilter + splitTopics.get(i) + "/";
                }
            }
            newTopicFilter = StrUtil.removeSuffix(newTopicFilter, "/");
            if (subTopic.equals(newTopicFilter)) {
                return true;
            }
        }
        return false;
    }

    public List<AclParam> listAllAclParams() {
        return JSONObject.parseArray(JSONObject.toJSONString(modelMateData.getConfigs().get(DATA)), AclParam.class);
    }

    public void addAclParam(AclParam aclParam) {
        if (aclParam == null) return;
        aclParam.setId(UUID.randomUUID().toString());
        List<AclParam> aclParams = this.listAllAclParams();
        aclParams.add(aclParam);
        modelMateData.getConfigs().put(DATA, aclParams);
        modulesService.update(modelMateData);
    }

    public void deleteAclParam(String id) {
        List<AclParam> aclParams = this.listAllAclParams();
        aclParams.removeIf(x-> x.getId().equals(id));
        modelMateData.getConfigs().put(DATA, aclParams);
        modulesService.update(modelMateData);
    }

    public void updateAclParam(AclParam aclParam) {
        List<AclParam> aclParams = this.listAllAclParams();
        Optional<AclParam> aclParamOptional = aclParams.stream().filter(x->x.getId().equals(aclParam.getId())).findFirst();
        if (aclParamOptional.isPresent()) {
            aclParamOptional.get().setAccess(aclParam.getAccess());
            aclParamOptional.get().setAllow(aclParam.getAllow());
            aclParamOptional.get().setIpaddr(aclParam.getIpaddr());
            aclParamOptional.get().setUsername(aclParam.getUsername());
            aclParamOptional.get().setTopic(aclParam.getTopic());
            aclParamOptional.get().setClientId(aclParam.getClientId());
        }
        modelMateData.getConfigs().put(DATA, aclParams);
        modulesService.update(modelMateData);
    }

    public AclModule() {
        modelMateData = new ModelMateData();
        modelMateData.setKey("ACL-MODULE");
        modelMateData.setModelEnum(ModelEnum.ACL);
        Map<String, List<AclParam>> map = new HashMap<>();
        List<AclParam> aclParams = new ArrayList<>();
        map.put(DATA, aclParams);
        modelMateData.setConfigs(map);
        modelMateData.setDescription("发布订阅 ACL 指对 发布 (PUBLISH)/订阅 (SUBSCRIBE) 操作的 权限控制，例如拒绝用户名为 Anna 向 open/elsa/door 发布消息");
        modelMateData.setEnable(false);
        modelMateData.setIcon("acl-icon");
        modelMateData.setModuleName("ACL");
    }

    @Override
    public boolean handle(AclParam aclParam) throws MmqException {
        return false;
    }
}
