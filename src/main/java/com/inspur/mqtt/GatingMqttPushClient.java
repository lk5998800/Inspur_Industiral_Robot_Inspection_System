package com.inspur.mqtt;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * mqtt客户端
 * @author kliu
 * @date 2022/5/24 18:03
 */
@Slf4j
@Component
public class GatingMqttPushClient {

    @Autowired
    private GatingPushCallback gatingPushCallback;

    private static MqttClient client;

    public static void setClient(MqttClient client) {
        GatingMqttPushClient.client = client;
    }

    public static MqttClient getClient() {
        return client;
    }

    /**
     * 连接mqtt
     * @param host
     * @param clientId
     * @param username
     * @param password
     * @param timeout
     * @param keepalive
     * @author kliu
     * @date 2022/5/24 17:59
     */
    public void connect(String host, String clientId, String username, String password, int timeout, int keepalive) {
        MqttClient client;
        try {
            client = new MqttClient(host, clientId+IdUtil.simpleUUID(), new MemoryPersistence());
            // MQTT 连接选项
            MqttConnectOptions options = new MqttConnectOptions();
            // 保留会话
            options.setCleanSession(true);
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setConnectionTimeout(timeout);
            options.setKeepAliveInterval(keepalive);
            GatingMqttPushClient.setClient(client);
            //设置回调类
            client.setCallback(gatingPushCallback);
            IMqttToken iMqttToken = client.connectWithResult(options);
            boolean complete = iMqttToken.isComplete();
            log.info("Gating MQTT连接"+(complete?"成功":"失败"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * 发布消息
     * @param topic
     * @param pushMessage
     * @author kliu
     * @date 2022/5/24 17:59
     */
    public void publish(String topic, String pushMessage) {
        publish(0, false, topic, pushMessage);
    }

    /**
     * 发布消息
     * @param qos
     * @param retained
     * @param topic
     * @param pushMessage
     * @author kliu
     * @date 2022/5/24 18:00
     */
    public void publish(int qos, boolean retained, String topic, String pushMessage) {
        MqttMessage message = new MqttMessage();
        message.setQos(qos);
        message.setRetained(retained);
        message.setPayload(pushMessage.getBytes());
        MqttTopic mTopic = GatingMqttPushClient.getClient().getTopic(topic);
        if (null == mTopic) {
            log.error("主题不存在:{}",mTopic);
        }
        try {
            mTopic.publish(message);
        } catch (Exception e) {
            log.error("mqtt发送消息异常:",e);
            throw new RuntimeException("mqtt发送消息异常，异常信息："+e.getMessage());
        }
    }

}