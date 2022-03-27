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
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author solley
 */
@Component
public class TDengineDriver implements ResourceDriver {

    private ConcurrentHashMap<String, DruidDataSource> dataSources = new ConcurrentHashMap<>();

    static final String JDBC_DRIVER = "com.taosdata.jdbc.rs.RestfulDriver";

    static final String IP = "ip";

    static final String DATABASE_NAME = "databaseName";

    static final String USERNAME = "username";

    static final String PASSWORD = "password";

    static final String PORT = "port";

    @Override
    public void addDriver(String resourceId, Map resource) {
        DruidDataSource druidDataSource = dataSources.get(resourceId);
        if (druidDataSource != null) {
            druidDataSource.close();
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

            // jdbc properties
            DruidDataSource dataSource = new DruidDataSource(); // 创建Druid连接池
            dataSource.setDriverClassName(JDBC_DRIVER);
            dataSource.setUrl(url); // 设置数据库的连接地址
            dataSource.setUsername(resource.get(USERNAME).toString());
            dataSource.setPassword(resource.get(PASSWORD).toString());
            // pool configurations
            dataSource.setInitialSize(8);
            dataSource.setMinIdle(1);
            dataSource.setMaxActive(20);
            dataSource.setValidationQuery("select server_status()");
            dataSources.put(resourceId, dataSource);
        } catch (Exception throwables) {
            return;
        }
    }

    @Override
    public void deleteDriver(String resourceId) {
        DruidDataSource druidDataSource = dataSources.get(resourceId);
        druidDataSource.close();
        dataSources.remove(resourceId);
    }

    @Override
    public Connection getDriver(String resourceId) throws Exception {
        if (dataSources == null) return null;
        if (dataSources.get(resourceId) == null) return null;
        //if (!dataSources.get(resourceId).isInited()) return null;
        return dataSources.get(resourceId).getConnection();
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
                       String topic, int qos, String address, String username) throws MmqException {
        Connection connection = null;
        try {
            connection = (Connection) this.getDriver(resourcesMateData.getResourceID());
            if (connection != null) {
                DriverFactory.setProperty(property, topic, username);
                String sql = resourcesMateData.getResource().get("sql").toString();
                ExpressionParser parser = new SpelExpressionParser();
                TemplateParserContext parserContext = new TemplateParserContext();
                String content = parser.parseExpression(sql, parserContext).getValue(property, String.class);
                connection.createStatement().execute(content);
            }
        } catch (Exception e) {
            throw new MmqException(e.hashCode(), e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throw new MmqException(throwables.hashCode(), throwables.getMessage());
            }
        }
    }
}
