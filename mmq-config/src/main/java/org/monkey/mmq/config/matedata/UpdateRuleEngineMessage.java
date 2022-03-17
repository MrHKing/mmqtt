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

import org.monkey.mmq.core.actor.ActorMsg;
import org.monkey.mmq.core.actor.MsgType;
import org.monkey.mmq.core.entity.InternalMessage;

/**
 * @author solley
 */
public class UpdateRuleEngineMessage implements ActorMsg {


    RuleEngineMateData ruleEngineMateData;

    public RuleEngineMateData getRuleEngineMateData() {
        return ruleEngineMateData;
    }

    public void setRuleEngineMateData(RuleEngineMateData ruleEngineMateData) {
        this.ruleEngineMateData = ruleEngineMateData;
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.UPDATE_RULE_ENGINE;
    }
}
