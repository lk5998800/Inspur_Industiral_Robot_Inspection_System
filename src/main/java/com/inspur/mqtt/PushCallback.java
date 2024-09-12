package com.inspur.mqtt;

import cn.hutool.core.util.IdUtil;
import com.inspur.code.Topic;
import com.inspur.industrialinspection.service.PointInfoService;
import com.inspur.industrialinspection.service.RobotStatusService;
import com.inspur.industrialinspection.service.TaskDetectionResultService;
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
public class PushCallback implements MqttCallback {
    @Autowired
    private MqttConfiguration mqttConfiguration;
    @Autowired
    private TaskDetectionResultService taskDetectionResultService;
    @Autowired
    private PointInfoService pointInfoService;
    @Autowired
    private RobotStatusService robotStatusService;
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
        MqttPushClient mqttPushClient;
        while (true) {
            try {
                mqttPushClient = mqttConfiguration.getMqttPushClient();
                if (null != mqttPushClient) {
                    mqttPushClient.connect(mqttConfiguration.getHost(), mqttConfiguration.getClientid()+IdUtil.simpleUUID(),
                            mqttConfiguration.getUsername(), mqttConfiguration.getPassword(), Integer.parseInt(mqttConfiguration.getTimeout()),
                            Integer.parseInt(mqttConfiguration.getKeepAlive()));
                    log.info("门控mqtt已重连");
                    break;
                }
            } catch (Exception e) {
                log.error("门控mqtt重连失败："+e.getMessage());
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
        log.info("接收消息主题 : " + topic+"|接收消息内容 : " + new String(message.getPayload()));
        //机器人上传数据
        if(Topic.INDUSTRIALROBOTUPLOAD.equals(topic)){
            try {
                taskDetectionResultService.add(new String(message.getPayload()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }else if (topic.indexOf("industrial_robot/fireextinguisherpara")>-1 && topic.indexOf("robotserver")>-1){
            try {
                pointInfoService.receiveFireExtinguisherPara(new String(message.getPayload()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }else if (topic.indexOf("industrial_robot/receivedetectionresult")>-1){
            try {
                taskDetectionResultService.add(new String(message.getPayload()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }else if (topic.indexOf("industrial_robot/receiverobotstatus")>-1){
            try {
                robotStatusService.receiveRobotStatus(new String(message.getPayload()));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}