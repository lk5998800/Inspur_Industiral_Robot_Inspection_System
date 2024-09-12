package com.inspur.mqtt;

import cn.hutool.core.util.IdUtil;
import com.inspur.code.Topic;
import com.inspur.industrialinspection.service.GatingService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息订阅处理、断线重连处理、消息到达处理
 * @author kliu
 * @date 2022/5/27 8:38
 */
@Slf4j
@Component
public class GatingPushCallback implements MqttCallback {
    @Autowired
    private GatingMqttConfiguration gatingMqttConfiguration;
    @Autowired
    GatingService gatingService;
    /**
     * mqtt断线重连处理
     * @param cause
     * @return void
     * @author kliu
     * @date 2022/5/27 8:39
     */
    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
        GatingMqttPushClient gatingMqttPushClient;
        while (true) {
            try {
                gatingMqttPushClient = gatingMqttConfiguration.getGatingMqttPushClient();
                if (null != gatingMqttPushClient) {
                    gatingMqttPushClient.connect(gatingMqttConfiguration.getHost(), gatingMqttConfiguration.getClientid()+IdUtil.simpleUUID(),
                            gatingMqttConfiguration.getUsername(), gatingMqttConfiguration.getPassword(), Integer.parseInt(gatingMqttConfiguration.getTimeout()),
                            Integer.parseInt(gatingMqttConfiguration.getKeepAlive()));
                    log.info("主mqtt已重连");
                    break;
                }
            } catch (Exception e) {
                log.error("主mqtt重连失败："+e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }

        }
    }

    /**
     * 发送消息，消息到达后处理方法
     * @param token
     * @return void
     * @author kliu
     * @date 2022/5/27 8:39
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /**
     * 订阅主题接收到消息处理方法
     * @param topic
     * @param message
     * @return void
     * @author kliu
     * @date 2022/5/27 8:39
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        //机器人上传数据
        log.info("接收消息主题 : " + topic+"|接收消息内容 : " + new String(message.getPayload()));
        if (Topic.GATINGBACK.equals(topic)){
            try {
                gatingService.receiveMqttBack(new String(message.getPayload()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}