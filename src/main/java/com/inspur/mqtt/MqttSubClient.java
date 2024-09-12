package com.inspur.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

/**
 * mqtt消息订阅
 * @author kliu
 * @date 2022/5/24 18:03
 */
@Slf4j
@Component
public class MqttSubClient {

    public MqttSubClient(MqttPushClient mqttPushClient){
        subScribeDataPublishTopic();
    }

    /**
     * 订阅主题
     * @author kliu
     * @date 2022/5/24 18:04
     */
    private void subScribeDataPublishTopic(){
        //订阅test_queue主题
        subscribe("industrial_robot_getposture/#");
        subscribe("industrial_robot_issued/#");
        subscribe("industrial_robot_detection_receve_success/#");
        subscribe("industrial_robot_stream/#");
        subscribe("industrial_robot_sys_set/#");
        subscribe("industrial_robot_positioning_status/#");
        subscribe("industrial_robot_terminate/#");
        subscribe("industrial_robot_remote_control/#");
        subscribe("industrial_robot/#");
    }

    /**
     * @param topic
     * @author kliu
     * @description 订阅主题
     * @date 2022/5/24 18:04
     */
    public void subscribe(String topic) {
        subscribe(topic, 0);
    }

    /**
     * @param topic
     * @param qos
     * @author kliu
     * @description 订阅某个主题
     * @date 2022/5/24 18:05
     */
    public void subscribe(String topic, int qos) {
        try {
            MqttClient client = MqttPushClient.getClient();
            if (client == null) {
                return;
            }
            client.subscribe(topic, qos);
            log.info("订阅主题:{}",topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
