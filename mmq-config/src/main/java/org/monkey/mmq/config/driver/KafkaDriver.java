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
package org.monkey.mmq.config.driver;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author solley
 */
@Component
public class KafkaDriver implements ResourceDriver<Producer<String, String>> {
    private ConcurrentHashMap<String, Producer<String, String>> producers = new ConcurrentHashMap<>();

    @Override
    public void addDriver(String resourceId, Map resource) {
        Producer<String, String> producer = producers.get(resourceId);
        if (producer != null) {
            producer.close();
            producers.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get("servers").toString())) return;

        Properties prop = new Properties();
        prop.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, resource.get("servers"));
        prop.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        prop.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producers.put(resourceId, new KafkaProducer<>(prop));
    }

    @Override
    public void deleteDriver(String resourceId) {
        producers.remove(resourceId);
    }

    @Override
    public Producer<String, String> getDriver(String resourceId) throws Exception {
        return producers.get(resourceId);
    }
}
