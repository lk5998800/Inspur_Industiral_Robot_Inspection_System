package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.ParaKey;
import com.inspur.code.TaskStatus;
import com.inspur.cron.TaskExecuteCron;
import com.inspur.industrialinspection.dao.ParticularPointInspectionTaskInstanceDao;
import com.inspur.industrialinspection.dao.ParticularPointInspectionTaskResultDao;
import com.inspur.industrialinspection.dao.PointInfoDao;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.ParticularPointInspectionService;
import com.inspur.industrialinspection.thread.AsynParticularDealInfraredToJpg;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: kliu
 * @description: 特定点巡检
 * @date: 2022/9/7 16:54
 */
@Service
@Slf4j
public class ParticularPointInspectionServiceImpl implements ParticularPointInspectionService {

    @Autowired
    private ParticularPointInspectionTaskInstanceDao particularPointInspectionTaskInstanceDao;
    @Autowired
    private ParticularPointInspectionTaskResultDao particularPointInspectionTaskResultDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private CommonService commonService;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private PointInfoDao pointInfoDao;
    @Autowired
    private TaskExecuteCron taskExecuteCron;
    @Autowired
    private AsynParticularDealInfraredToJpg asynParticularDealInfraredToJpg;

    @Override
    public void add(ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance) {
        long robotId = robotRoomDao.getRobotIdByRoomId(particularPointInspectionTaskInstance.getRoomId());
        particularPointInspectionTaskInstance.setRobotId(robotId);

        String detection = particularPointInspectionTaskInstance.getDetection();
        String[] split = detection.split(",");
        JSONArray detectionArray = new JSONArray();
        for (int i = 0; i < split.length; i++) {
            detectionArray.add(split[i]);
        }

        particularPointInspectionTaskInstance.setDetection(detectionArray.toString());

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            particularPointInspectionTaskInstance.setStartTime(DateUtil.now());
            long instanceId = particularPointInspectionTaskInstanceDao.addAndReturnId(particularPointInspectionTaskInstance);
            particularPointInspectionTaskInstance.setInstanceId(instanceId);
            //生成特定点巡检任务
            String taskJson = getTaskJson(particularPointInspectionTaskInstance);
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;
            String issuedStr = commonService.gzipCompress(taskJson).replace("\n", "").replace("\r", "");
            mqttPushClient.publish("industrial_robot_issued/"+robotId, issuedStr);
        } catch (Exception e) {
            if (transactionStatus != null) {
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void receiveTaskResult(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        JSONObject dataObject = (JSONObject) jsonObject.get("data");
        long instanceId = dataObject.getLong("task_id");
        long robotId = dataObject.getLong("robot_id");
        String startTime = dataObject.getStr("start_time");
        String endTime = dataObject.getStr("end_time");
        String uuid = dataObject.getStr("uuid");

        if (!particularPointInspectionTaskInstanceDao.checkExist(instanceId)){
            throw new RuntimeException("任务id不存在，请检查传入的数据");
        }

        JSONObject detectionObject = dataObject.getJSONObject("detection_data");
        JSONObject detectionDetlObject, detectionDataObject;
        String pointName = detectionObject.getStr("point_name");

        ParticularPointInspectionTaskResult particularPointInspectionTaskResult = new ParticularPointInspectionTaskResult();
        particularPointInspectionTaskResult.setInstanceId(instanceId);
        particularPointInspectionTaskResult.setPointName(pointName);

        if (detectionObject.containsKey("front_picture")){
            detectionDetlObject = detectionObject.getJSONObject("front_picture");
            String code = detectionDetlObject.getStr("code");
            String successCode = "0";
            if (!successCode.equals(code)) {
                log.error("机器人前置拍照数据异常，异常原因：" + detectionDetlObject.getStr("message"));
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            if (!detectionDetlObject.containsKey(ParaKey.DATA)){
                log.error("机器人前置拍照数据异常，异常原因：不存在data");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            detectionDataObject = detectionDetlObject.getJSONObject("data");
            if (!detectionDataObject.containsKey(ParaKey.PATH)) {
                log.error("机器人前置拍照数据异常，异常原因：不存在前置拍照图片");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            JSONArray pathArr = detectionDataObject.getJSONArray("path");
            if (pathArr.size() != 1) {
                log.error("机器人前置拍照数据异常，异常原因：前置拍照数据有【"+pathArr.size()+"】张");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            String imgUrl = pathArr.getStr(0);
            particularPointInspectionTaskResult.setFront(imgUrl);
        }
        if (detectionObject.containsKey("after_picture")){
            detectionDetlObject = detectionObject.getJSONObject("after_picture");
            String code = detectionDetlObject.getStr("code");
            String successCode = "0";
            if (!successCode.equals(code)) {
                log.error("机器人后置拍照数据异常，异常原因：" + detectionDetlObject.getStr("message"));
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            if (!detectionDetlObject.containsKey(ParaKey.DATA)){
                log.error("机器人后置拍照数据异常，异常原因：不存在data");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            detectionDataObject = detectionDetlObject.getJSONObject("data");
            if (!detectionDataObject.containsKey(ParaKey.PATH)) {
                log.error("机器人后置拍照数据异常，异常原因：不存在前置拍照图片");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            JSONArray pathArr = detectionDataObject.getJSONArray("path");
            if (pathArr.size() != 1) {
                log.error("机器人后置拍照数据异常，异常原因：前置拍照数据有【"+pathArr.size()+"】张");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            String imgUrl = pathArr.getStr(0);
            particularPointInspectionTaskResult.setAfter(imgUrl);
        }

        if (detectionObject.containsKey(ParaKey.SENSOR)) {
            detectionDetlObject = (JSONObject) detectionObject.get("sensor");
            String code = detectionDetlObject.getStr("code");
            String successCode = "0";
            if (!successCode.equals(code)) {
                log.error("获取传感器数据异常，异常原因：" + detectionDetlObject.getStr("message"));
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }

            detectionDataObject = detectionDetlObject.getJSONObject("data");
            particularPointInspectionTaskResult.setSensor(detectionDataObject.toString().toLowerCase());
        }

        if (detectionObject.containsKey(ParaKey.ALARM_LIGHTS)) {
            detectionDetlObject = (JSONObject) detectionObject.get(ParaKey.ALARM_LIGHTS);
            String code = detectionDetlObject.getStr("code");
            String successCode = "0";
            if (!successCode.equals(code)) {
                log.error("机器人上报报警灯数据异常，异常原因：" + detectionDetlObject.getStr("message"));
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            if (!detectionDetlObject.containsKey(ParaKey.DATA)){
                log.error("机器人上报报警灯数据异常，异常原因：不存在data");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            detectionDataObject = detectionDetlObject.getJSONObject("data");
            if (!detectionDataObject.containsKey(ParaKey.PATH)) {
                log.error("机器人上报报警灯数据异常，异常原因：不存在报警灯图片");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            particularPointInspectionTaskResult.setAlarmLight(detectionDataObject.toString());
        }

        if (detectionObject.containsKey(Detection.INFRARED)) {
            detectionDetlObject = (JSONObject) detectionObject.get(Detection.INFRARED);
            String code = detectionDetlObject.getStr("code");
            String successCode = "0";
            if (!successCode.equals(code)) {
                log.error("获取红外测温数据异常，异常原因：" + detectionDetlObject.getStr("message"));
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }

            detectionDataObject = detectionDetlObject.getJSONObject("data");
            if (!detectionDataObject.containsKey(ParaKey.PATH)) {
                log.error("机器人上报红外测温数据异常，异常原因：不存在红外测温图片数据");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }

            String path = detectionDataObject.getJSONArray("path").getStr(0);
            particularPointInspectionTaskResult.setInfrared(path);
        }

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            if (!particularPointInspectionTaskResultDao.checkExist(particularPointInspectionTaskResult)) {
                particularPointInspectionTaskResultDao.add(particularPointInspectionTaskResult);
            }else{
                particularPointInspectionTaskResultDao.update(particularPointInspectionTaskResult);
            }

            //计算图片个数，更新进instance表中
            particularPointInspectionTaskResult = particularPointInspectionTaskResultDao.getDetlById(instanceId);
            int picCount = 0;
            if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getInfrared())){
                picCount++;
            }
            if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
                picCount++;
            }
            if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
                picCount++;
            }
            if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
                String alarmLight = particularPointInspectionTaskResult.getAlarmLight();
                JSONObject alarmLightObject = new JSONObject(alarmLight);
                picCount+=alarmLightObject.getJSONArray("path").size();
            }

            particularPointInspectionTaskInstanceDao.updatePicCount(instanceId, picCount);

            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;
            //发送数据保存成功标志
            mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
            String infrared = particularPointInspectionTaskResult.getInfrared();
            if (!StringUtils.isEmpty(infrared)){
                if (infrared.endsWith(".tiff")){
                    asynParticularDealInfraredToJpg.asynParticularDealInfraredToJpg(instanceId, infrared);
                }
            }
        } catch (TransactionException e) {
            if (transactionStatus != null){
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException("保存特定点巡检结果数据异常"+e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void industrialRobotEndTask(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        Long instanceId = jsonObject.getLong("task_id");
        String endTime = jsonObject.getStr("end_time");
        ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance = particularPointInspectionTaskInstanceDao.getDetlById(instanceId);
        //仅running状态下更新结束时间，防止终止任务后任务状态也变为已结束
        if (TaskStatus.RUNNING.equals(particularPointInspectionTaskInstance.getExecStatus())){
            particularPointInspectionTaskInstanceDao.updateEndTime(instanceId, endTime);
        }
    }

    /**
     * 获取任务json
     * @param particularPointInspectionTaskInstance
     * @return java.lang.String
     * @author kliu
     * @date 2022/9/7 18:00
     */
    private String getTaskJson(ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance){
        JSONArray pointActionArray = new JSONArray();

        String pointName = particularPointInspectionTaskInstance.getPointName();
        PointInfo pointInfo = new PointInfo();
        pointInfo.setRoomId(particularPointInspectionTaskInstance.getRoomId());
        pointInfo.setPointName(pointName);
        pointInfo = pointInfoDao.getDetlById(pointInfo);

        List<PointInfo> points = new ArrayList();
        points.add(pointInfo);
        JSONObject postureObject = taskExecuteCron.getPosture(points, pointName);


        String detection = particularPointInspectionTaskInstance.getDetection();
        JSONArray detectionArray = new JSONArray(detection);
        JSONObject actionObject;
        JSONArray detectionItemArray = new JSONArray();

        actionObject = new JSONObject();
        actionObject.set("action", "navigation");
        actionObject.set("param", postureObject);
        detectionItemArray.add(actionObject);

        for (int i = 0; i < detectionArray.size(); i++) {
            String detectionStr = detectionArray.getStr(i);
            String action = "";
            if ("front_camera".equals(detectionStr)){
                action = Detection.FRONTPICTURE;
            }else if ("after_camera".equals(detectionStr)){
                action = Detection.AFTERPICTURE;
            }else if ("industry_camera".equals(detectionStr)){
                action = Detection.ALARMLIGHT;
            }else if ("infrared_camera".equals(detectionStr)){
                action = Detection.INFRARED;
            }else{
                throw new RuntimeException("不支持的检测项"+detectionStr);
            }
            actionObject = new JSONObject();
            actionObject.set("action", action);
            detectionItemArray.add(actionObject);
        }

        JSONObject pointObject = new JSONObject();
        pointObject.set("point_name", particularPointInspectionTaskInstance.getPointName());
        pointObject.set("detection_item", detectionItemArray);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(pointObject);
        pointActionArray.add(jsonArray);

        long instanceId = particularPointInspectionTaskInstance.getInstanceId();
        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", particularPointInspectionTaskInstance.getStartTime());
        map.put("task_id", instanceId);
        map.put("robot_id", particularPointInspectionTaskInstance.getRobotId());
        map.put("run_mode", "particular_point_inspection");
        map.put("point_action_list", pointActionArray);

        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        String json = new JSONObject(returnMap).toString();
        log.info(json);
        return json;
    }
}
