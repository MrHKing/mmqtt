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
@Component
public class SqlServerDriver extends AbstractJDBCDriver {

    @Override
    protected String getDriverClassName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    protected String getDriverUrl() {
        return "jdbc:sqlserver://%s:%s;DatabaseName=%s";
    }

    @Override
    protected void initDataSource(DruidDataSource dataSource) {
        dataSource.setValidationQuery("select 'x'");
    }
}
