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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author solley
 */
@Component
public class SqlServerDriver implements ResourceDriver<Connection>{

    private ConcurrentHashMap<String, DruidDataSource> dataSources = new ConcurrentHashMap<>();

    static final String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    @Override
    public void addDriver(String resourceId, Map<String, Object> resource) {
        DruidDataSource druidDataSource = dataSources.get(resourceId);
        if (druidDataSource != null) {
            druidDataSource.close();
            dataSources.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get("ip").toString())) return;
        if (StringUtils.isEmpty(resource.get("databaseName").toString())) return;
        if (StringUtils.isEmpty(resource.get("username").toString())) return;
        if (StringUtils.isEmpty(resource.get("password").toString())) return;

        DruidDataSource dataSource = new DruidDataSource(); // 创建Druid连接池
        dataSource.setDriverClassName(JDBC_DRIVER); // 设置连接池的数据库驱动
        dataSource.setUrl(String.format("jdbc:sqlserver://%s:%s;DatabaseName=%s",
                resource.get("ip").toString(),
                resource.get("port").toString(),
                resource.get("databaseName").toString())); // 设置数据库的连接地址
        dataSource.setUsername(resource.get("username").toString()); // 数据库的用户名
        dataSource.setPassword(resource.get("password").toString()); // 数据库的密码
        dataSource.setValidationQuery("select 'x'");
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
            DriverManager.getConnection(String.format("jdbc:sqlserver://%s:%s;DatabaseName=%s",
                    resourcesMateData.getResource().get("ip").toString(),
                    resourcesMateData.getResource().get("port").toString(),
                    resourcesMateData.getResource().get("databaseName").toString()),
                    resourcesMateData.getResource().get("username").toString(),
                    resourcesMateData.getResource().get("password").toString());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void handle(Map property, ResourcesMateData resourcesMateData,
                       String topic, int qos, String address) throws Exception{
        Connection connection = (Connection)this.getDriver(resourcesMateData.getResourceID());
        if (connection != null) {
            DriverFactory.setProperty(property, topic);
            String sql = resourcesMateData.getResource().get("sql").toString();
            ExpressionParser parser = new SpelExpressionParser();
            TemplateParserContext parserContext = new TemplateParserContext();
            String content = parser.parseExpression(sql, parserContext).getValue(property, String.class);
            connection.createStatement().execute(content);
            connection.close();
        }
    }

}
