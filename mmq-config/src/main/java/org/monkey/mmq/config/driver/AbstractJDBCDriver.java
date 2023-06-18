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
import org.stringtemplate.v4.ST;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.monkey.mmq.config.config.Constants.*;

/**
 * @author solley
 */
public abstract class AbstractJDBCDriver implements ResourceDriver<Connection>  {


    private ConcurrentHashMap<String, DruidDataSource> dataSources = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ST> stringSTConcurrentHashMap = new ConcurrentHashMap<>();

    protected abstract String getDriverClassName();

    protected abstract String getDriverUrl();

    protected abstract void initDataSource(DruidDataSource dataSource);

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
        dataSource.setDriverClassName(getDriverClassName()); // 设置连接池的数据库驱动
        dataSource.setUrl(String.format(getDriverUrl(),
                resource.get(IP).toString(),
                resource.get(PORT).toString(),
                resource.get(DATABASE_NAME).toString())); // 设置数据库的连接地址
        dataSource.setUsername(resource.get(USERNAME).toString()); // 数据库的用户名
        dataSource.setPassword(resource.get(PASSWORD).toString()); // 数据库的密码
        dataSource.setInitialSize(8); // 设置连接池的初始大小
        dataSource.setMinIdle(1); // 设置连接池大小的下限
        dataSource.setMaxActive(20); // 设置连接池大小的上限
        initDataSource(dataSource);
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
            Class.forName(getDriverClassName());
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
                String sql = resourcesMateData.getResource().get(SQL).toString();
                ST content = stringSTConcurrentHashMap.get(sql);
                if (content == null) {
                    content = new ST(sql);
                    stringSTConcurrentHashMap.put(sql, content);
                }
                content.remove("json");
                content.add("json", property);
                String[] sqlRet = content.render().split(";");
                if (sqlRet.length == 0) connection.close();
                for (String temp : sqlRet) {
                    connection.createStatement().execute(temp);
                }
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
