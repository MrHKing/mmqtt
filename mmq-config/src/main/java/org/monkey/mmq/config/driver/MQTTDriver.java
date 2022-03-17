package org.monkey.mmq.config.driver;

import com.alibaba.fastjson.JSON;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.monkey.mmq.config.matedata.ResourcesMateData;
import org.monkey.mmq.core.exception.MmqException;
import org.monkey.mmq.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassNameMQTTDriver
 * @Description
 * @Author Solley
 * @Date2021/11/23 15:51
 * @Version V1.0
 **/
@Component
public class MQTTDriver implements ResourceDriver<MqttClient> {

    private ConcurrentHashMap<String, MqttClient> mqttClientConcurrentHashMap = new ConcurrentHashMap<>();
    /**
     * 设置超时时间
     */
    Integer connectionTimeout = 10;
    /**
     * 会话心跳时间
     */
    Integer keepAliveInterval = 30;

    @Override
    public void addDriver(String resourceId, Map<String, Object> resource) {
        try {
            MqttClient mqttClient = mqttClientConcurrentHashMap.get(resourceId);
            if (mqttClient != null) {
                mqttClient.close();
                mqttClientConcurrentHashMap.remove(resourceId);
            }

            if (StringUtils.isEmpty(resource.get("server").toString())) return;
            if (StringUtils.isEmpty(resource.get("username").toString())) return;
            if (StringUtils.isEmpty(resource.get("password").toString())) return;
            mqttClient = new MqttClient(resource.get("server").toString(), UUID.randomUUID().toString(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            // 如果想要断线这段时间的数据，要设置成false，并且重连后不用再次订阅，否则不会得到断线时间的数据
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(resource.get("username").toString());
            // 设置连接的密码
            options.setPassword(resource.get("password").toString().toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(connectionTimeout);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(keepAliveInterval);
            // 连接服务器
            mqttClient.connect(options);
            mqttClientConcurrentHashMap.put(resourceId, mqttClient);
        } catch (MqttException e) {
            return;
        }
    }

    @Override
    public void deleteDriver(String resourceId) {
        MqttClient mqttClient = mqttClientConcurrentHashMap.get(resourceId);
        if (mqttClient != null) {
            try {
                mqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        mqttClientConcurrentHashMap.remove(resourceId);
    }

    @Override
    public MqttClient getDriver(String resourceId) throws Exception {
        if (mqttClientConcurrentHashMap == null) return null;
        if (mqttClientConcurrentHashMap.get(resourceId) == null) return null;
        if (!mqttClientConcurrentHashMap.get(resourceId).isConnected()) return null;
        return mqttClientConcurrentHashMap.get(resourceId);
    }

    @Override
    public boolean testConnect(ResourcesMateData resourcesMateData) {
        try {
            MqttClient mqttClient = new MqttClient(resourcesMateData.getResource().get("server").toString(), UUID.randomUUID().toString(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            mqttClient.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    try {
                        mqttClient.reconnect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws MqttException {

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token){
                }
            });
            // 如果想要断线这段时间的数据，要设置成false，并且重连后不用再次订阅，否则不会得到断线时间的数据
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(resourcesMateData.getResource().get("username").toString());
            // 设置连接的密码
            options.setPassword(resourcesMateData.getResource().get("password").toString().toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(1);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(keepAliveInterval);
            // 连接服务器
            mqttClient.connect(options);

            return true;
        } catch (MqttException e) {
            return false;
        }
    }

    @Override
    public void handle(Map property, ResourcesMateData resourcesMateData,
                       String topic, int qos, String address, String username) throws MmqException {
        try {
            MqttClient mqttClient = this.getDriver(resourcesMateData.getResourceID());
            if (mqttClient.isConnected()) {
                mqttClient.publish(topic,
                        JSON.toJSONString(property).getBytes(),
                        qos, false);
            } else {
                mqttClient.reconnect();
            }
        } catch (Exception e) {
            throw new MmqException(e.hashCode(), e.getMessage());
        }
    }
}
