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
package org.monkey.mmq.web.controller;

import org.monkey.mmq.config.matedata.RuleEngineMateData;
import org.monkey.mmq.config.service.RuleEngineService;
import org.monkey.mmq.core.consistency.model.ResponsePage;
import org.monkey.mmq.core.consistency.model.RestResultUtils;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author solley
 */
@RestController
@RequestMapping({"/v1/ruleEngine"})
public class RuleEngineController {

    @Autowired
    RuleEngineService ruleEngineService;

    /**
     * Get ruleEngines.
     *
     * @return Current ruleEngines
     */
    @GetMapping("/ruleEngines")
    public ResponsePage<RuleEngineMateData> getRuleEngines(@RequestParam int pageNo, @RequestParam int pageSize,
                                                   @RequestParam(required = false, defaultValue = "") String clientId) {
        Map<String, RuleEngineMateData> datas = ruleEngineService.getAllRuleEngine();
        return new ResponsePage<>(pageSize, pageNo,
                datas.size() / pageSize,
                datas.size(),
                datas.values().stream().filter(x -> x.getRuleId().contains(clientId))
                        .skip(pageNo - 1).limit(pageSize).collect(Collectors.toList()));
    }

    @GetMapping
    public Object getRuleEngine(@RequestParam String ruleId) {
        return ruleEngineService.getRuleEngineByRuleId(ruleId);
    }

    /**
     * Delete an existed ruleEngines.
     *
     * @param ruleId
     * @return ok if deleted succeed
     */
    @DeleteMapping
    public Object deleteRuleEngine(@RequestParam String ruleId) {
        ruleEngineService.delete(ruleId);
        return RestResultUtils.success("delete resource ok!", null);
    }

    /**
     * Create a new ruleEngines.
     *
     * @param ruleEngineMateData
     * @return ok if create succeed
     */
    @PostMapping
    public Object saveRuleEngines(@RequestBody RuleEngineMateData ruleEngineMateData) {
        if (StringUtils.isEmpty(ruleEngineMateData.getRuleId())) ruleEngineMateData.setRuleId(getRuleId());
        ruleEngineService.save(ruleEngineMateData.getRuleId(), ruleEngineMateData);
        return RestResultUtils.success("create resource ok!", null);
    }

    /**
     * 获得规则id
     * @return
     */
    private String getRuleId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-","");
    }
}
