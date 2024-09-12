package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskInstance;
import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskResult;
import com.inspur.industrialinspection.entity.RemoteControlTaskInstance;
import com.inspur.industrialinspection.entity.RemoteControlTaskResult;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.RemoteControlService;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.mqtt.MqttPushClient;
import com.inspur.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kliu
 * @description: 远程控制服务
 * @date: 2022/8/11 15:55
 */
@Service
@Slf4j
public class RemoteControlServiceImpl implements RemoteControlService {
    private volatile static ConcurrentHashMap<Long, String> liftMap = new ConcurrentHashMap<Long, String>();
    private volatile static ConcurrentHashMap<Long, Boolean> tempTaskMap = new ConcurrentHashMap<Long, Boolean>();
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private CommonService commonService;
    @Autowired
    private RequestService requestService;
    @Autowired
    private ParticularPointInspectionTaskInstanceDao particularPointInspectionTaskInstanceDao;
    @Autowired
    private ParticularPointInspectionTaskResultDao particularPointInspectionTaskResultDao;
    @Autowired
    private RemoteControlTaskInstanceDao remoteControlTaskInstanceDao;
    @Autowired
    private RemoteControlTaskResultDao remoteControlTaskResultDao;

    @Value("${remotecontrol.websocket}")
    private String remoteControlWebsocket;

    private long checkRightAndReturnRobotId(long robotId, long roomId){
        if(robotId == 0){
            robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        }
        long userId = requestService.getUserIdByToken();
        if (WebSocketServer.userHasRight(userId, robotId)) {
            if (roomId == 0 && robotId == 0){
                throw new RuntimeException("机房id与机器人id不能同时为空");
            }
            return robotId;
        }
        throw new RuntimeException("当前机器人正在被远程控制，请等待他人释放后再进行控制");
    }

    @Override
    public void move(long robotId, long roomId, double v, double w) {
        //move的权限校验交由websocket自己校验
        String paramStr = "("+v+","+w+")";
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("param", paramStr);
        jsonObject.set("act", "vw");
        mqttPushClient.publish("industrial_robot_remote_control/" + robotId, jsonObject.toString());
    }

    @Override
    public void reboot(long robotId, long roomId) {
        robotId = checkRightAndReturnRobotId(robotId, roomId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("param", "");
        jsonObject.set("act", "reboot");
        mqttPushClient.publish("industrial_robot_remote_control/" + robotId, jsonObject.toString());
    }

    @Override
    public void rebootCan(long robotId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("param", "");
        jsonObject.set("act", "reboot_can");
        mqttPushClient.publish("industrial_robot_remote_control/" + robotId, jsonObject.toString());
    }

    @Override
    public void relocalization(long robotId, long roomId) {
        robotId = checkRightAndReturnRobotId(robotId, roomId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("param", "");
        jsonObject.set("act", "relocalization");
        mqttPushClient.publish("industrial_robot_remote_control/" + robotId, jsonObject.toString());
    }

    @Override
    public void lifter(long robotId, long roomId, String position) {
        robotId = checkRightAndReturnRobotId(robotId, roomId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("param", position);
        jsonObject.set("act", "lifter");
        mqttPushClient.publish("industrial_robot_remote_control/" + robotId, jsonObject.toString());
        int number = 0;
        int count = 30;
        while (number < count) {
            if (!StringUtils.isEmpty(liftMap.get(robotId))) {
                String value = liftMap.get(robotId);
                liftMap.remove(robotId);
                if (value.equals("true")) {
                    log.info("升降杆反馈调用成功");
                } else {
                    throw new RuntimeException("升降杆调用失败，请联系管理员");
                }
                break;
            } else {
                try {
                    number++;
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void emergencyStop(long robotId, long roomId) {
        robotId = checkRightAndReturnRobotId(robotId, roomId);
        String paramStr = "(" + 0 + "," + 0 + ")";
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("param", paramStr);
        jsonObject.set("act", "vw");
        mqttPushClient.publish("industrial_robot_remote_control/" + robotId, jsonObject.toString());
    }

    @Override
    public void backChargingPile(long robotId, long roomId) throws InterruptedException {
        long userId = requestService.getUserIdByToken();
        robotId = checkRightAndReturnRobotId(robotId, roomId);
        JSONArray pointActionArray = new JSONArray();

        JSONObject actionObject = new JSONObject();
        actionObject.set("action", "back_charging_pile");

        JSONArray detectionItemArray = new JSONArray();
        detectionItemArray.add(actionObject);

        JSONObject pointObject = new JSONObject();
        pointObject.set("point_name", "A00");
        pointObject.set("detection_item", detectionItemArray);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(pointObject);
        pointActionArray.add(jsonArray);

        long instanceId = remoteControlTaskInstanceDao.getInstanceIdByUserId(userId);
        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", DateUtil.now());
        map.put("task_id", instanceId);
        map.put("robot_id", robotId);
        map.put("run_mode", "temp_task");
        map.put("point_action_list", pointActionArray);

        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        String json = new JSONObject(returnMap).toString();
        log.info(json);
        String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
        mqttPushClient.publish("industrial_robot_issued/"+robotId,issuedStr);

        tempTaskMap.put(instanceId, false);

        int i=0;
        int whileCount = 30;
        boolean executeSuccess = false;
        //10s无结果返回则报错
        while (i < whileCount){
            executeSuccess = tempTaskMap.get(instanceId);
            if(executeSuccess){
                tempTaskMap.remove(instanceId);
                break;
            }else{
                i++;
                Thread.sleep(1000);
            }
        }

        if (!executeSuccess){
            tempTaskMap.remove(instanceId);
            throw new RuntimeException("返航执行失败，请重新尝试");
        }
    }

    @Override
    public void frontPicture(long robotId, long roomId) throws InterruptedException {
        long userId = requestService.getUserIdByToken();
        robotId = checkRightAndReturnRobotId(robotId, roomId);
        JSONArray pointActionArray = new JSONArray();

        JSONObject actionObject = new JSONObject();
        actionObject.set("action", "front_picture");

        JSONArray detectionItemArray = new JSONArray();
        detectionItemArray.add(actionObject);

        JSONObject pointObject = new JSONObject();
        pointObject.set("point_name", "A00");
        pointObject.set("detection_item", detectionItemArray);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(pointObject);
        pointActionArray.add(jsonArray);

        long instanceId = remoteControlTaskInstanceDao.getInstanceIdByUserId(userId);
        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", DateUtil.now());
        map.put("task_id", instanceId);
        map.put("robot_id", robotId);
        map.put("run_mode", "temp_task");
        map.put("point_action_list", pointActionArray);

        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        String json = new JSONObject(returnMap).toString();
        log.info(json);
        String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
        mqttPushClient.publish("industrial_robot_issued/"+robotId,issuedStr);

        tempTaskMap.put(instanceId, false);

        int i=0;
        int whileCount = 30;
        boolean executeSuccess = false;
        //10s无结果返回则报错
        while (i < whileCount){
            executeSuccess = tempTaskMap.get(instanceId);
            if(executeSuccess){
                tempTaskMap.remove(instanceId);
                break;
            }else{
                i++;
                Thread.sleep(1000);
            }
        }

        if (!executeSuccess){
            tempTaskMap.remove(instanceId);
            throw new RuntimeException("前置拍照执行失败，请重新尝试");
        }
    }

    @Override
    public void afterPicture(long robotId, long roomId) throws InterruptedException {
        long userId = requestService.getUserIdByToken();
        robotId = checkRightAndReturnRobotId(robotId, roomId);
        JSONArray pointActionArray = new JSONArray();

        JSONObject actionObject = new JSONObject();
        actionObject.set("action", "after_picture");

        JSONArray detectionItemArray = new JSONArray();
        detectionItemArray.add(actionObject);

        JSONObject pointObject = new JSONObject();
        pointObject.set("point_name", "A00");
        pointObject.set("detection_item", detectionItemArray);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(pointObject);
        pointActionArray.add(jsonArray);

        long instanceId = remoteControlTaskInstanceDao.getInstanceIdByUserId(userId);
        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", DateUtil.now());
        map.put("task_id", instanceId);
        map.put("robot_id", robotId);
        map.put("run_mode", "temp_task");
        map.put("point_action_list", pointActionArray);

        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        String json = new JSONObject(returnMap).toString();
        log.info(json);
        String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
        mqttPushClient.publish("industrial_robot_issued/"+robotId,issuedStr);

        tempTaskMap.put(instanceId, false);

        int i=0;
        int whileCount = 30;
        boolean executeSuccess = false;
        //10s无结果返回则报错
        while (i < whileCount){
            executeSuccess = tempTaskMap.get(instanceId);
            if(executeSuccess){
                tempTaskMap.remove(instanceId);
                break;
            }else{
                i++;
                Thread.sleep(1000);
            }
        }

        if (!executeSuccess){
            tempTaskMap.remove(instanceId);
            throw new RuntimeException("后置拍照执行失败，请重新尝试");
        }
    }

    @Override
    public JSONArray picTaskList(long roomId) {
        JSONArray jsonArray = new JSONArray();
        List<RemoteControlTaskInstance> remoteControlTaskInstances = remoteControlTaskInstanceDao.list(roomId);
        List<ParticularPointInspectionTaskInstance> particularPointInspectionTaskInstances = particularPointInspectionTaskInstanceDao.list(roomId);

        JSONObject jsonObject;
        long instanceId;
        //远程遥控任务和单点巡检任务的实例id可能重复，此时在其前添加前缀
        //远程遥控任务中 前缀为1  单点巡检任务前缀为2
        for (RemoteControlTaskInstance remoteControlTaskInstance : remoteControlTaskInstances) {
            jsonObject = new JSONObject();
            instanceId = remoteControlTaskInstance.getInstanceId();
            instanceId = Long.parseLong("1"+instanceId);
            String startTime = remoteControlTaskInstance.getStartTime();
            jsonObject.set("taskTime", DateUtil.parse(startTime).toString("MM/dd HH:mm"));
            jsonObject.set("taskType", "远程遥控任务");
            jsonObject.set("position", remoteControlTaskInstance.getRoomName());
            jsonObject.set("picCount", remoteControlTaskInstance.getPicCount());
            jsonObject.set("instanceId", instanceId);
            jsonArray.add(jsonObject);
        }

        for (ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance : particularPointInspectionTaskInstances) {
            jsonObject = new JSONObject();
            instanceId = particularPointInspectionTaskInstance.getInstanceId();
            instanceId = Long.parseLong("2"+instanceId);
            String startTime = particularPointInspectionTaskInstance.getStartTime();
            jsonObject.set("taskTime", DateUtil.parse(startTime).toString("MM/dd HH:mm"));
            jsonObject.set("taskType", "单点巡检任务");
            jsonObject.set("position", particularPointInspectionTaskInstance.getRoomName()+" "+particularPointInspectionTaskInstance.getPointName());
            jsonObject.set("picCount", particularPointInspectionTaskInstance.getPicCount());
            jsonObject.set("instanceId", instanceId);
            jsonArray.add(jsonObject);
        }

        jsonArray.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String taskTime1 = ((JSONObject) o1).getStr("taskTime");
                String taskTime2 = ((JSONObject) o2).getStr("taskTime");
                return taskTime2.compareTo(taskTime1);
            }
        });

        return jsonArray;
    }

    @Override
    public JSONArray picTaskDetl(String taskType, long instanceId) {
        JSONArray jsonArray = new JSONArray();
        instanceId = Long.parseLong((""+instanceId).substring(1));
        if("单点巡检任务".equals(taskType)){
            ParticularPointInspectionTaskResult particularPointInspectionTaskResult = particularPointInspectionTaskResultDao.getDetlById(instanceId);
            if (particularPointInspectionTaskResult == null){
                return new JSONArray();
            }
            String infrared = particularPointInspectionTaskResult.getInfrared();
            if (!StringUtils.isEmpty(infrared)){
                jsonArray.add(commonService.url2Https(infrared));
            }
            if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
                JSONArray tempArr = new JSONObject(particularPointInspectionTaskResult.getAlarmLight()).getJSONArray("path");
                for (int i = 0; i < tempArr.size(); i++) {
                    jsonArray.add(commonService.url2Https(tempArr.getStr(i)));
                }
            }
            if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
                jsonArray.add(commonService.url2Https(particularPointInspectionTaskResult.getFront()));
            }
            if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
                jsonArray.add(commonService.url2Https(particularPointInspectionTaskResult.getAfter()));
            }
        }else{
            List<RemoteControlTaskResult> list = remoteControlTaskResultDao.list(instanceId);
            for (RemoteControlTaskResult remoteControlTaskResult : list) {
                jsonArray.add(commonService.url2Https(remoteControlTaskResult.getImgUrl()));
            }
        }
        return jsonArray;
    }

    @Override
    public void liftingLeverResults(Long robotId, String value) {
        //反馈结果存入
        liftMap.put(robotId, value);
    }

    @Override
    public void tempTaskEnd(Long instanceId) {
        tempTaskMap.put(instanceId, true);
    }

    @Override
    public String getWebsocketUrl() {
        return remoteControlWebsocket;
    }
}
