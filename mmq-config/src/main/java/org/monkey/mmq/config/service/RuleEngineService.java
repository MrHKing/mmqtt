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
package org.monkey.mmq.config.service;

import org.monkey.mmq.config.config.Loggers;
import org.monkey.mmq.config.driver.DriverFactory;
import org.monkey.mmq.config.driver.MysqlDriver;
import org.monkey.mmq.config.matedata.KeyBuilder;
import org.monkey.mmq.config.matedata.RuleEngineMateData;
import org.monkey.mmq.config.matedata.UtilsAndCommons;
import org.monkey.mmq.core.consistency.matedata.RecordListener;
import org.monkey.mmq.core.consistency.persistent.ConsistencyService;
import org.monkey.mmq.core.exception.MmqException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * RuleEngine Service
 *
 * @author solley
 */
@Service
public class RuleEngineService implements RecordListener<RuleEngineMateData> {

    @Autowired
    MysqlDriver mysqlDriver;

    Map<String, RuleEngineMateData> ruleEngineMateDataMap = new HashMap<>();

    @Resource(name = "configPersistentConsistencyServiceDelegate")
    private ConsistencyService consistencyService;

    /**
     * Init
     */
    @PostConstruct
    public void init() {
        try {
            consistencyService.listen(KeyBuilder.getRuleEngineKey(), this);
        } catch (MmqException e) {
            Loggers.CONFIG_SERVER.error("listen ruleEngine service failed.", e);
        }
    }

    public void save(String ruleId, RuleEngineMateData ruleEngineMateData) {
        try {
            consistencyService.put(UtilsAndCommons.RULE_ENGINE_STORE + ruleId, ruleEngineMateData);
        } catch (MmqException e) {
            Loggers.CONFIG_SERVER.error("save ruleEngine failed.", e);
        }
    }

    public void delete(String ruleId) {
        try {
            consistencyService.remove(UtilsAndCommons.RULE_ENGINE_STORE + ruleId);
        } catch (MmqException e) {
            Loggers.CONFIG_SERVER.error("delete ruleEngine failed.", e);
        }
    }

    public Map<String, RuleEngineMateData> getAllRuleEngine() {
        return ruleEngineMateDataMap;
    }

    public RuleEngineMateData getRuleEngineByRuleId(String ruleId) {
        return ruleEngineMateDataMap.get(ruleId);
    }

    @Override
    public boolean interests(String key) {
        return KeyBuilder.matchRuleEngineKey(key);
    }

    @Override
    public boolean matchUnlistenKey(String key) {
        return KeyBuilder.matchRuleEngineKey(key);
    }

    @Override
    public void onChange(String key, RuleEngineMateData value) throws Exception {
        ruleEngineMateDataMap.put(key, value);
    }

    @Override
    public void onDelete(String key) throws Exception {
        ruleEngineMateDataMap.remove(key);
    }
}
