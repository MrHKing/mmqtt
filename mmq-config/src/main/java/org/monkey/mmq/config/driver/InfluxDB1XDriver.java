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

import com.alibaba.fastjson.JSON;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.monkey.mmq.config.config.Constants.*;

/**
 * @author solley
 */
@Component
public class InfluxDB1XDriver implements ResourceDriver<InfluxDB> {

    private ConcurrentHashMap<String, InfluxDB> dataSources = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ST> stringSTConcurrentHashMap = new ConcurrentHashMap<>();

    private static final String retentionPolicyName = "autogen";

    @Override
    public void addDriver(String resourceId, Map resource) {
        InfluxDB client = dataSources.get(resourceId);
        if (client != null) {
            client.close();
            dataSources.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get(IP).toString())) return;
        if (StringUtils.isEmpty(resource.get(PORT).toString())) return;
        if (StringUtils.isEmpty(resource.get(USERNAME).toString())) return;
        if (StringUtils.isEmpty(resource.get(PASSWORD).toString())) return;
        if (StringUtils.isEmpty(resource.get(DATABASE_NAME).toString())) return;

        try {
            client = InfluxDBFactory.connect(String.format("http://%s:%s",
                    resource.get(IP).toString(),
                    resource.get(PORT).toString()),
                    resource.get(USERNAME).toString(),
                    resource.get(PASSWORD).toString());
            client.setDatabase(resource.get(DATABASE_NAME).toString());
            if (resource.get(RETENTIOM_POLICY) != null && !StringUtils.isEmpty(resource.get(RETENTIOM_POLICY).toString())) {
                client.setRetentionPolicy(resource.get(RETENTIOM_POLICY).toString());
            } else {
                client.setRetentionPolicy(retentionPolicyName);
            }
            client.enableBatch(
                    BatchOptions.DEFAULTS
                            .threadFactory(runnable -> {
                                Thread thread = new Thread(runnable);
                                thread.setDaemon(true);
                                return thread;
                            })
            );
            dataSources.put(resourceId, client);
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void deleteDriver(String resourceId) {
        InfluxDB client = dataSources.get(resourceId);
        client.close();
        dataSources.remove(resourceId);
    }

    @Override
    public InfluxDB getDriver(String resourceId) throws Exception {
        if (dataSources == null) return null;
        if (dataSources.get(resourceId) == null) return null;
        return dataSources.get(resourceId);
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        try {
            InfluxDB client =InfluxDBFactory.connect(String.format("http://%s:%s",
                    resourcesMateData.getResource().get(IP).toString(),
                    resourcesMateData.getResource().get(PORT).toString()),
                    resourcesMateData.getResource().get(USERNAME).toString(),
                    resourcesMateData.getResource().get(PASSWORD).toString());
            client.query(new Query("CREATE DATABASE " + resourcesMateData.getResource().get(DATABASE_NAME).toString()));
            client.setDatabase(resourcesMateData.getResource().get(DATABASE_NAME).toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void handle(Map property, ResourcesMateData resourcesMateData,
                       String topic, int qos, String address, String username) throws MmqException {
        InfluxDB client = null;
        try {
            client = this.getDriver(resourcesMateData.getResourceID());
            if (client != null && resourcesMateData.getResource().get(SQL) != null) {
                DriverFactory.setProperty(property, topic, username);
                String payload = resourcesMateData.getResource().get(SQL).toString();
                ST content = stringSTConcurrentHashMap.get(payload);
                if (content == null) {
                    content = new ST(payload);
                    stringSTConcurrentHashMap.put(payload, content);
                }
                content.remove("json");
                content.add("json", property);
                List<Map> maps = JSON.parseArray(content.render(), Map.class);
                for (Map map : maps) {
                    // Write points to InfluxDB.
                    Point.Builder builder = Point.measurement(map.get("measurement").toString())
                            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                    for (Object tag : ((Map)map.get("tags")).entrySet()) {
                        builder.tag(((Map.Entry)tag).getKey().toString(), ((Map.Entry)tag).getValue().toString());
                    }
                    for (Object tag : ((Map)map.get("fields")).entrySet()) {
                        builder.addField(((Map.Entry)tag).getKey().toString(), ((Map.Entry)tag).getValue().toString());
                    }
                    client.write(builder.build());
                }
            }
        } catch (Exception e) {
            throw new MmqException(e.hashCode(), e.getMessage());
        }
    }
}
