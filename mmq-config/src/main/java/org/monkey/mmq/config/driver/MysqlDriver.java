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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author solley
 */
@Component
public class MysqlDriver implements ResourceDriver<Connection> {

    private ConcurrentHashMap<String, DruidDataSource> dataSources = new ConcurrentHashMap<>();

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    static final String IP = "ip";

    static final String DATABASE_NAME = "databaseName";

    static final String USERNAME = "username";

    static final String PASSWORD = "password";

    static final String PORT = "port";

    @Override
    public void addDriver(String resourceId, Map<String, Object> resource) {
        DruidDataSource druidDataSource = dataSources.get(resourceId);
        if (druidDataSource != null) {
            druidDataSource.close();
            dataSources.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get(IP).toString())) return;
        if (StringUtils.isEmpty(resource.get(DATABASE_NAME).toString())) return;
        if (StringUtils.isEmpty(resource.get(USERNAME).toString())) return;
        if (StringUtils.isEmpty(resource.get(PASSWORD).toString())) return;

        DruidDataSource dataSource = new DruidDataSource(); // 创建Druid连接池
        dataSource.setDriverClassName(JDBC_DRIVER); // 设置连接池的数据库驱动
        dataSource.setUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT",
                resource.get(IP).toString(),
                resource.get(PORT).toString(),
                resource.get(DATABASE_NAME).toString())); // 设置数据库的连接地址
        dataSource.setUsername(resource.get(USERNAME).toString()); // 数据库的用户名
        dataSource.setPassword(resource.get(PASSWORD).toString()); // 数据库的密码
        dataSource.setInitialSize(8); // 设置连接池的初始大小
        dataSource.setMinIdle(1); // 设置连接池大小的下限
        dataSource.setMaxActive(20); // 设置连接池大小的上限
        try {
            dataSource.getConnection();
        } catch (SQLException throwables) {
            return;
        }
        dataSources.put(resourceId, dataSource);
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
        if (!dataSources.get(resourceId).isInited()) return null;
        return dataSources.get(resourceId).getConnection();
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        try {
            Class.forName(JDBC_DRIVER);
            DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT",
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
            connection = this.getDriver(resourcesMateData.getResourceID());
            if (connection != null) {
                DriverFactory.setProperty(property, topic, username);
                String sql = resourcesMateData.getResource().get("sql").toString();
                ExpressionParser parser = new SpelExpressionParser();
                TemplateParserContext parserContext = new TemplateParserContext();
                String content = parser.parseExpression(sql, parserContext).getValue(property, String.class);
                connection.createStatement().execute(content);
                connection.close();
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
