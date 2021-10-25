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
import org.springframework.stereotype.Component;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author solley
 */
@Component
public class TDengineDriver implements ResourceDriver {

    private ConcurrentHashMap<String, DruidDataSource> dataSources = new ConcurrentHashMap<>();

    static final String JDBC_DRIVER = "com.taosdata.jdbc.rs.RestfulDriver";

    @Override
    public void addDriver(String resourceId, Map resource) {
        DruidDataSource druidDataSource = dataSources.get(resourceId);
        if (druidDataSource != null) {
            druidDataSource.close();
            dataSources.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get("ip").toString())) return;
        if (StringUtils.isEmpty(resource.get("databaseName").toString())) return;
        if (StringUtils.isEmpty(resource.get("username").toString())) return;
        if (StringUtils.isEmpty(resource.get("password").toString())) return;
        DruidDataSource dataSource = new DruidDataSource();
        // jdbc properties
        dataSource.setDriverClassName(JDBC_DRIVER);
        dataSource.setUrl(String.format("jdbc:TAOS-RS://%s:%s/%s",
                resource.get("ip").toString(),
                resource.get("port").toString(),
                resource.get("databaseName").toString())); // 设置数据库的连接地址
        dataSource.setUsername(resource.get("username").toString());
        dataSource.setPassword(resource.get("password").toString());
        // pool configurations
        dataSource.setInitialSize(10);
        dataSource.setMinIdle(10);
        dataSource.setMaxActive(10);
        dataSource.setMaxWait(30000);
        dataSource.setValidationQuery("select server_status()");
    }

    @Override
    public void deleteDriver(String resourceId) {
        dataSources.remove(resourceId);
    }

    @Override
    public Object getDriver(String resourceId) throws Exception {
        if (dataSources == null) return null;
        if (dataSources.get(resourceId) == null) return null;
        if (!dataSources.get(resourceId).isInited()) return null;
        return dataSources.get(resourceId).getConnection();
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        try {
            Class.forName(JDBC_DRIVER);
            DriverManager.getConnection(String.format("jdbc:TAOS-RS://%s:%s/%s",
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
}
