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
import com.taosdata.jdbc.TSDBDriver;
import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author solley
 */
@Component
public class TDengineDriver implements ResourceDriver {

    private ConcurrentHashMap<String, Connection> dataSources = new ConcurrentHashMap<>();

    static final String JDBC_DRIVER = "com.taosdata.jdbc.rs.RestfulDriver";

    static final String IP = "ip";

    static final String DATABASE_NAME = "databaseName";

    static final String USERNAME = "username";

    static final String PASSWORD = "password";

    static final String PORT = "port";

    @Override
    public void addDriver(String resourceId, Map resource) {
        Connection connection = dataSources.get(resourceId);
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            dataSources.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get(IP).toString())) return;
        if (StringUtils.isEmpty(resource.get(DATABASE_NAME).toString())) return;
        if (StringUtils.isEmpty(resource.get(USERNAME).toString())) return;
        if (StringUtils.isEmpty(resource.get(PASSWORD).toString())) return;
        String url = String.format("jdbc:TAOS-RS://%s:%s/%s",
                resource.get(IP).toString(),
                resource.get(PORT).toString(),
                resource.get(DATABASE_NAME).toString());
        try {
            Class.forName(JDBC_DRIVER);
            Properties connProps = new Properties();
            connProps.setProperty(TSDBDriver.PROPERTY_KEY_CHARSET, "UTF-8");
            connProps.setProperty(TSDBDriver.PROPERTY_KEY_LOCALE, "en_US.UTF-8");
            connProps.setProperty(TSDBDriver.PROPERTY_KEY_TIME_ZONE, "UTC-8");

            Connection conn = null;
            conn = DriverManager.getConnection(url, resource.get(USERNAME).toString(),
                    resource.get(PASSWORD).toString());
            dataSources.put(resourceId, conn);
        } catch (Exception throwables) {
            return;
        }


    }

    @Override
    public void deleteDriver(String resourceId) {
        Connection connection = dataSources.get(resourceId);
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            dataSources.remove(resourceId);
        }
    }

    @Override
    public Object getDriver(String resourceId) throws Exception {
        if (dataSources == null) return null;
        if (dataSources.get(resourceId) == null) return null;
        return dataSources.get(resourceId);
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        try {
            Class.forName(JDBC_DRIVER);
            DriverManager.getConnection(String.format("jdbc:TAOS-RS://%s:%s/%s",
                    resourcesMateData.getResource().get(IP).toString(),
                    resourcesMateData.getResource().get(PORT).toString(),
                    resourcesMateData.getResource().get(DATABASE_NAME).toString()),
                    resourcesMateData.getResource().get(USERNAME).toString(),
                    resourcesMateData.getResource().get(PASSWORD).toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void handle(Map property, ResourcesMateData resourcesMateData,
                       String topic, int qos, String address, String username) throws Exception{
        Connection connection = (Connection)this.getDriver(resourcesMateData.getResourceID());
        if (connection != null) {
            DriverFactory.setProperty(property, topic, username);
            String sql = resourcesMateData.getResource().get("sql").toString();
            ExpressionParser parser = new SpelExpressionParser();
            TemplateParserContext parserContext = new TemplateParserContext();
            String content = parser.parseExpression(sql, parserContext).getValue(property, String.class);
            connection.createStatement().execute(content);
        }
    }
}
