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
public class GatingMqttSubClient {

    public GatingMqttSubClient(GatingMqttPushClient gatingMqttPushClient){
        subScribeDataPublishTopic();
    }

    /**
     * 订阅主题
     * @author kliu
     * @date 2022/5/24 18:04
     */
    private void subScribeDataPublishTopic(){
        subscribe("inspur/#");
        subscribe("topic/rpc-cloud-back/inspur");
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
            MqttClient client = GatingMqttPushClient.getClient();
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
