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
package org.monkey.mmq.config.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.monkey.mmq.config.matedata.DriverMessage;
import org.monkey.mmq.config.matedata.RuleEngineMessage;
import org.monkey.mmq.config.matedata.RuleEngineMateData;
import org.monkey.mmq.config.matedata.UpdateRuleEngineMessage;
import org.monkey.mmq.core.actor.ActorMsg;
import org.monkey.mmq.core.utils.JacksonUtils;
import org.monkey.mmq.rule.engine.ReactorQL;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * @author solley
 */
public final class RuleEngineActor extends AbstractActor {

    ActorSystem actorSystem;

    RuleEngineMateData ruleEngineMateData;

    public RuleEngineActor(RuleEngineMateData ruleEngineMateData, ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        this.ruleEngineMateData = ruleEngineMateData;
    }

    private boolean isTopic(String topic, String topicFilter) {
        if (StrUtil.split(topic, '/').size() >= StrUtil.split(topicFilter, '/').size()) {
            List<String> splitTopics = StrUtil.split(topic, '/');//a
            List<String> spliteTopicFilters = StrUtil.split(topicFilter, '/');//#
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
            if (topicFilter.equals(newTopicFilter)) {
                return true;
            }
        } else if (topicFilter.equals(topic)) {
            return true;
        }

        return false;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ActorMsg.class, msg -> {

            switch (msg.getMsgType()) {
                case RULE_ENGINE:
                    ruleProcess((RuleEngineMessage) msg);
                    break;
                case UPDATE_RULE_ENGINE:
                    updateProcess((UpdateRuleEngineMessage) msg);
                    break;

            }
        }).build();
    }

    protected void updateProcess(UpdateRuleEngineMessage updateRuleEngineMessage) {
        this.ruleEngineMateData = updateRuleEngineMessage.getRuleEngineMateData();
    }

    protected void ruleProcess(RuleEngineMessage msg) {
        if (!ruleEngineMateData.getEnable()) return;

        // 获得引擎的SQL，进行处理
        ReactorQL.builder()
                .sql(ruleEngineMateData.getSql())
                .build()
                .start(name -> isTopic(msg.getMessage().getTopic(), name) ?
                        Flux.just((new ObjectMapper().convertValue(JacksonUtils.toObj(new String(msg.getMessage().getMessageBytes().toByteArray())),Map.class))) : Flux.just())
                .doOnNext(map -> {
                    // 如果不为空则触发响应
                    if (map != null && ruleEngineMateData.getResourcesMateDatas().size() != 0) {
                        // 根据规则获得规则的响应
                        ruleEngineMateData.getResourcesMateDatas().forEach(resource -> {
                            DriverMessage driverMessage = new DriverMessage();
                            driverMessage.setProperty(map);
                            driverMessage.setResourcesMateData(resource);
                            driverMessage.setRuleEngineMessage(msg);
                            ActorSelection actorSelection = actorSystem.actorSelection("/user/" + resource.getResourceID());
                            actorSelection.tell(driverMessage, ActorRef.noSender());
                        });
                    }
                }).subscribe();
    }
}
