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
package org.monkey.mmq.notifier;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.monkey.mmq.config.Loggers;
import org.monkey.mmq.config.driver.DriverFactory;
import org.monkey.mmq.config.driver.MysqlDriver;
import org.monkey.mmq.config.matedata.RuleEngineMateData;
import org.monkey.mmq.config.service.RuleEngineService;
import org.monkey.mmq.core.consistency.notifier.ValueChangeEvent;
import org.monkey.mmq.core.entity.InternalMessage;
import org.monkey.mmq.core.notify.Event;
import org.monkey.mmq.core.notify.NotifyCenter;
import org.monkey.mmq.core.notify.listener.Subscriber;
import org.monkey.mmq.core.utils.JacksonUtils;
import org.monkey.mmq.metadata.message.ClientMateData;
import org.monkey.mmq.protocol.Connect;
import org.monkey.mmq.rule.engine.ReactorQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.monkey.mmq.core.common.Constants.RULE_ENGINE;

/**
 * @author solley
 */
@Component
public final class RuleEngineManager extends Subscriber<RuleEngineEvent> {

    @Autowired
    MysqlDriver mysqlDriver;

    /**
     * 处理线程池
     */
    private static ExecutorService executor = Executors.newCachedThreadPool();

    private final int queueMaxSize = 65539;

    @Autowired
    RuleEngineService ruleEngineService;

    @PostConstruct
    public void init() {
        NotifyCenter.registerToPublisher(RuleEngineEvent.class, queueMaxSize);
        NotifyCenter.registerSubscriber(this);
    }

    @Override
    public void onEvent(RuleEngineEvent event) {
        //List<RuleEngineMateData> ruleEngineMateDataList = ruleEngineService.getAllRuleEngine().values().stream().filter(x->x.getEnable()).collect(Collectors.toList());
        Collection<RuleEngineMateData> ruleEngineMateDataList = ruleEngineService.getAllRuleEngine().values();
        if (ruleEngineMateDataList.size() == 0) return;
//
            ruleEngineMateDataList.forEach(rule -> {

                // 获得引擎的SQL，进行处理
                ReactorQL.builder()
                        .sql(rule.getSql())
                        .build()
                        .start(name -> isTopic(event.getMessage().getTopic(), name) ?
                                Flux.just((new ObjectMapper().convertValue(JacksonUtils.toObj(new String(event.getMessage().getMessageBytes().toByteArray())),Map.class))) : Flux.just())
                        .doOnNext(map -> {
                            // 如果不为空则触发响应
                            if (map != null && rule.getResourcesMateDatas().size() != 0) {
                                // 根据规则获得规则的响应
                                rule.getResourcesMateDatas().forEach(resource -> {
                                    DriverEvent driverEvent = new DriverEvent();
                                    driverEvent.setProperty(map);
                                    driverEvent.setResourcesMateData(resource);
                                    driverEvent.setRuleEngineEvent(event);
                                    NotifyCenter.publishEvent(driverEvent);
                                });
                            }
                        }).subscribe();
            });
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return RuleEngineEvent.class;
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
}
