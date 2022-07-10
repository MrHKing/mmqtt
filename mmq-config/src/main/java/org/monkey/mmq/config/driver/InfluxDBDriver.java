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

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.monkey.mmq.config.config.Constants.*;

/**
 * @author solley
 */
@Component
public class InfluxDBDriver implements ResourceDriver<InfluxDBClient> {

    private ConcurrentHashMap<String, InfluxDBClient> dataSources = new ConcurrentHashMap<>();

    @Override
    public void addDriver(String resourceId, Map resource) {
        InfluxDBClient client = dataSources.get(resourceId);
        if (client != null) {
            client.close();
            dataSources.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get(IP).toString())) return;
        if (StringUtils.isEmpty(resource.get(PORT).toString())) return;
        if (StringUtils.isEmpty(resource.get(TOKEN).toString())) return;

        try {
            client = InfluxDBClientFactory.create(String.format("http://%s:%s",
                    resource.get(IP).toString(),
                    resource.get(PORT).toString()),
                    resource.get(TOKEN).toString().toCharArray());
            dataSources.put(resourceId, client);
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void deleteDriver(String resourceId) {
        InfluxDBClient client = dataSources.get(resourceId);
        client.close();
        dataSources.remove(resourceId);
    }

    @Override
    public InfluxDBClient getDriver(String resourceId) throws Exception {
        if (dataSources == null) return null;
        if (dataSources.get(resourceId) == null) return null;
        return dataSources.get(resourceId);
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        try {
            InfluxDBClientFactory.create(String.format("http://%s:%s",
                    resourcesMateData.getResource().get(IP).toString(),
                    resourcesMateData.getResource().get(PORT).toString()),
                    resourcesMateData.getResource().get(TOKEN).toString().toCharArray());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void handle(Map property, ResourcesMateData resourcesMateData,
                       String topic, int qos, String address, String username) throws MmqException {
        InfluxDBClient client = null;
        try {
            if (StringUtils.isEmpty(resourcesMateData.getResource().get(INFLUXDB_ORG).toString())) return;
            if (StringUtils.isEmpty(resourcesMateData.getResource().get(INFLUXDB_BUCKET).toString())) return;
            client = this.getDriver(resourcesMateData.getResourceID());
            if (client != null) {
                DriverFactory.setProperty(property, topic, username);

                String sql = resourcesMateData.getResource().get("sql").toString();
                ExpressionParser parser = new SpelExpressionParser();
                TemplateParserContext parserContext = new TemplateParserContext();
                String content = parser.parseExpression(sql, parserContext).getValue(property, String.class);

                WriteApiBlocking writeApi = client.getWriteApiBlocking();
                writeApi.writeRecord(resourcesMateData.getResource().get(INFLUXDB_BUCKET).toString(),
                        resourcesMateData.getResource().get(INFLUXDB_ORG).toString(), WritePrecision.NS, content);
            }
        } catch (Exception e) {
            throw new MmqException(e.hashCode(), e.getMessage());
        }
    }
}
