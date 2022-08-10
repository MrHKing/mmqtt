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
import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import static org.monkey.mmq.config.config.Constants.*;

/**
 * @ClassName:RabbitMQDriver
 * @Auther: Solley
 * @Description:
 * @Date: 2022/6/26 20:46
 * @Version: v1.0
 */
@Component
public class RabbitMQDriver implements ResourceDriver<Channel> {

    private ConcurrentHashMap<String, Connection> dataSources = new ConcurrentHashMap<>();


    @Override
    public void addDriver(String resourceId, Map<String, Object> resource) {

        Connection connection = dataSources.get(resourceId);
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                return;
            }
            dataSources.remove(resourceId);
        }

        if (StringUtils.isEmpty(resource.get(IP).toString())) return;
        if (StringUtils.isEmpty(resource.get(VIRTUALHOST).toString())) return;
        if (StringUtils.isEmpty(resource.get(USERNAME).toString())) return;
        if (StringUtils.isEmpty(resource.get(PASSWORD).toString())) return;
        if (StringUtils.isEmpty(resource.get(PORT).toString())) return;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(resource.get(IP).toString());
        factory.setPort(Integer.parseInt(resource.get(PORT).toString()));
        factory.setVirtualHost(resource.get(VIRTUALHOST).toString());
        factory.setUsername(resource.get(USERNAME).toString());
        factory.setPassword(resource.get(PASSWORD).toString());
        try {
            connection = factory.newConnection();
            dataSources.put(resourceId, connection);
        } catch (IOException e) {
            return;
        } catch (TimeoutException e) {
            return;
        }
    }

    @Override
    public void deleteDriver(String resourceId) {
        Connection connection = dataSources.get(resourceId);
        try {
            connection.close();
        } catch (IOException e) {
            return;
        }
        dataSources.remove(resourceId);
    }

    @Override
    public Channel getDriver(String resourceId) throws Exception {
        if (dataSources == null) return null;
        if (dataSources.get(resourceId) == null) return null;
        return dataSources.get(resourceId).createChannel();
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(resourcesMateData.getResource().get(IP).toString());
        factory.setPort(Integer.parseInt(resourcesMateData.getResource().get(PORT).toString()));
        factory.setVirtualHost(resourcesMateData.getResource().get(VIRTUALHOST).toString());
        factory.setUsername(resourcesMateData.getResource().get(USERNAME).toString());
        factory.setPassword(resourcesMateData.getResource().get(PASSWORD).toString());
        try {
            Connection connection = factory.newConnection();
            if (connection.isOpen()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Override
    public void handle(Map property, ResourcesMateData resourcesMateData, String topic, int qos, String address, String username) throws MmqException {
        Channel channel = null;
        try {
            channel = (Channel) this.getDriver(resourcesMateData.getResourceID());
            if (channel != null && resourcesMateData.getResource().get(EXCHANGE) != null
                && resourcesMateData.getResource().get(QUEUE) != null
                && resourcesMateData.getResource().get(PAYLOAD) != null) {
                String content = JSON.toJSONString(property);
                if (!resourcesMateData.getResource().get(PAYLOAD).equals(PAYLOAD)) {
                    DriverFactory.setProperty(property, topic, username);
                    String template = resourcesMateData.getResource().get(PAYLOAD).toString();
                    ST st = new ST(template);
                    st.add("json", property);
                    content = st.render();
                }
                channel.basicPublish(resourcesMateData.getResource().get(EXCHANGE).toString(),
                        resourcesMateData.getResource().get(QUEUE).toString(), null, content.getBytes());
            }
        } catch (Exception e) {
            throw new MmqException(e.hashCode(), e.getMessage());
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                throw new MmqException(e.hashCode(), e.getMessage());
            } catch (TimeoutException e) {
                throw new MmqException(e.hashCode(), e.getMessage());
            }
        }
    }
}
