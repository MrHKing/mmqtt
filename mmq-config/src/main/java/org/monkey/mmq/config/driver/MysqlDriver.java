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
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author solley
 */
@Component
public class MysqlDriver {
    private static DruidDataSource dataSource;
    /*8.X版本连接器写法*/
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // 初始化连接池
    public void initDataSource(String ip, int port, String databaseName, String username, String password) {
        try {
            if (StringUtils.isEmpty(ip)) return;
            if (StringUtils.isEmpty(databaseName)) return;
            if (StringUtils.isEmpty(username)) return;
            if (StringUtils.isEmpty(password)) return;
            dataSource = new DruidDataSource(); // 创建Druid连接池
            dataSource.setDriverClassName(JDBC_DRIVER); // 设置连接池的数据库驱动
            dataSource.setUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT", ip, port, databaseName)); // 设置数据库的连接地址
            dataSource.setUsername(username); // 数据库的用户名
            dataSource.setPassword(password); // 数据库的密码
            dataSource.setInitialSize(8); // 设置连接池的初始大小
            dataSource.setMinIdle(1); // 设置连接池大小的下限
            dataSource.setMaxActive(20); // 设置连接池大小的上限
        }catch (Exception e){
            System.out.println(e.toString());
        }
    }
    //从连接池里面获取一个连接对象
    public Connection getConnect() throws Exception{
        if (dataSource == null) return null;
        return dataSource.getConnection();
    }
}
