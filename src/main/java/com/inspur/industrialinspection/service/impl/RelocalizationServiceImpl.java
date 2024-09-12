package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.service.RelocalizationService;
import com.inspur.mqtt.MqttPushClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: kliu
 * @description: 重定位
 * @date: 2022/8/12 11:15
 */
@Service
public class RelocalizationServiceImpl implements RelocalizationService {

    @Autowired
    private MqttPushClient mqttPushClient;

    /**
     * 重定位成功
     * @param robotId
     * @return void
     * @author kliu
     * @date 2022/8/12 11:16
     */
    @Override
    public void success(long robotId) {
        mqttPushClient.publish("industrial_robot_positioning_status/"+robotId, "{\"positioning_status\":true}");
    }
}
