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
import org.monkey.mmq.config.matedata.ModelEnum;
import org.monkey.mmq.config.modules.BaseModule;
import org.monkey.mmq.config.modules.ModelMateData;
import org.monkey.mmq.config.service.ModulesService;
import org.monkey.mmq.core.exception.MmqException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

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

    List<AclParam> aclParams;

    public List<AclParam> listAllAclParams() {
        return aclParams;
    }

    public void addAclParam(AclParam aclParam) {
        if (aclParam == null) return;
        aclParam.setId(UUID.randomUUID().toString());
        aclParams.add(aclParam);
        modulesService.update(modelMateData);
    }

    public void deleteAclParam(String id) {
        aclParams.removeIf(x-> x.getId().equals(id));
        modulesService.update(modelMateData);
    }

    public void updateAclParam(AclParam aclParam) {
        Optional<AclParam> aclParamOptional = aclParams.stream().filter(x->x.getId().equals(aclParam.getId())).findFirst();
        if (aclParamOptional.isPresent()) {
            aclParamOptional.get().setAccess(aclParam.getAccess());
            aclParamOptional.get().setAllow(aclParam.getAllow());
            aclParamOptional.get().setIpaddr(aclParam.getIpaddr());
            aclParamOptional.get().setUsername(aclParam.getUsername());
            aclParamOptional.get().setTopic(aclParam.getTopic());
            aclParamOptional.get().setClientId(aclParam.getClientId());
        }
        modulesService.update(modelMateData);
    }

    public AclModule() {
        modelMateData = new ModelMateData();
        modelMateData.setKey("ACL-MODULE");
        modelMateData.setModelEnum(ModelEnum.ACL);
        Map<String, Object> map = new HashMap<>();
        aclParams = new ArrayList<>();
        map.put("data", aclParams);
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
