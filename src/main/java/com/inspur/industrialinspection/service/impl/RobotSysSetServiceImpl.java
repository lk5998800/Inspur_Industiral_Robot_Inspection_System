package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.industrialinspection.service.RobotParamService;
import com.inspur.industrialinspection.service.RobotSysSetService;
import com.inspur.mqtt.MqttPushClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kliu
 * @description 机器人系统设置
 * @date 2022/5/6 17:07
 */
@Service
public class RobotSysSetServiceImpl implements RobotSysSetService {

    private volatile static ConcurrentHashMap<String, Boolean> sysSetHashMap = new ConcurrentHashMap<String, Boolean>();
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RobotParamService robotParamService;

    @Override
    public void issued(long robotId) throws IOException, InterruptedException {
        JSONObject robotParamObject = robotParamService.getRobotParam(robotId);
        double taskMinimumPower = robotParamObject.getDouble("task_minimum_power");
        double inspectRate = robotParamObject.getDouble("inspect_rate");
        JSONObject waitPointPose = robotParamObject.getJSONObject("wait_point_pose");


        JSONObject messageObject = new JSONObject();
        String uuid = IdUtil.simpleUUID();
        messageObject.set("uuid", uuid);
        messageObject.set("task_minimum_power", taskMinimumPower);
        messageObject.set("inspect_rate", inspectRate);
        messageObject.set("wait_point_pose", waitPointPose);
        mqttPushClient.publish("industrial_robot_sys_set/"+robotId, messageObject.toString());

        sysSetHashMap.put(uuid, false);
        int i=0;
        int whileCount = 50;
        while (i<whileCount){
            boolean flag = sysSetHashMap.get(uuid);
            if(!flag){
                i++;
                Thread.sleep(200);
            }else{
                sysSetHashMap.remove(uuid);
                break;
            }
        }

        if(sysSetHashMap.containsKey(uuid)){
            sysSetHashMap.remove(uuid);
            throw new RuntimeException("下发机器人系统设置信息异常，机器人未反馈调用结果");
        }
    }

    @Override
    public void receiveRobotSysSetResult(String robotSysSetJson) {
        JSONObject jsonObject = JSONUtil.parseObj(robotSysSetJson);
        String uuid = jsonObject.getStr("uuid");
        if(sysSetHashMap.containsKey(uuid)){
            sysSetHashMap.put(uuid, true);
        }
    }
}
