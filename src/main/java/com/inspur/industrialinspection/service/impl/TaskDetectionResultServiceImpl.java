package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.code.ParaKey;
import com.inspur.code.TaskStatus;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.dao.TaskInfoDao;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.entity.CabinetUbit;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.service.*;
import com.inspur.industrialinspection.thread.AnalyseDetectionResult;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
/**
 * 任务执行检测项结果服务实现
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Slf4j
@Service
@Scope("prototype")
public class TaskDetectionResultServiceImpl implements TaskDetectionResultService {
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RobotParamService robotParamService;
    @Autowired
    private RemoteControlTaskInstanceService remoteControlTaskInstanceService;
    @Autowired
    private ItAssetTaskInfoService itAssetTaskInfoService;
    @Autowired
    private ParticularPointInspectionService particularPointInspectionService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private CabinetUbitService cabinetUbitService;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private RoomParamService roomParamService;

    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public void add(String json){
        JSONObject robotParamObject;
        JSONObject jsonObject = JSONUtil.parseObj(json);
        JSONObject dataObject = (JSONObject) jsonObject.get("data");
        long instanceId = dataObject.getLong("task_id");
        long robotId = dataObject.getLong("robot_id");
        String startTime = dataObject.getStr("start_time");
        String endTime = dataObject.getStr("end_time");
        String uuid = dataObject.getStr("uuid");

        JSONObject detectionObject = dataObject.getJSONObject("detection_data");
        //1 普通巡检    2随工任务  3 资产盘点  4 返航等临时任务 5 特定点巡检
        int type= 0;
        if (dataObject.containsKey("type")){
            type = dataObject.getInt("type");
        }
        if (type == 4){
            remoteControlTaskInstanceService.receiveTaskResult(json);
            return;
        }else if (type == 5){
            particularPointInspectionService.receiveTaskResult(json);
            return;
        }

        String pointName = detectionObject.getStr("point_name");
        int lifterHeight = 0;
        //noinspection AlibabaUndefineMagicConstant
        if(detectionObject.containsKey("lifter_height")){
            lifterHeight = detectionObject.getInt("lifter_height");
        }
        //noinspection AlibabaUndefineMagicConstant
        if (detectionObject.containsKey("qr_code")){
            itAssetTaskInfoService.receiveItAssetResult(json);
            return;
        }

        TaskInstance taskInstance = taskInstanceDao.getDetlById(instanceId);
        String execStatus = taskInstance.getExecStatus();

        boolean updateTaskInstance = false;
        if (StringUtils.isEmpty(endTime)) {
            //正在运行
            if (TaskStatus.CREATE.equals(execStatus)) {
                taskInstance.setExecStatus(TaskStatus.RUNNING);
                updateTaskInstance = true;
            }
        } else {
            if (TaskStatus.RUNNING.equals(execStatus)) {
                taskInstance.setExecStatus(TaskStatus.END);
                taskInstance.setEndTime(endTime);
                updateTaskInstance = true;
            }
        }

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

        if (updateTaskInstance) {
            taskInstanceDao.update(taskInstance);
        }

        TaskDetectionResult taskDetectionResult = new TaskDetectionResult();
        taskDetectionResult.setInstanceId(instanceId);
        taskDetectionResult.setPointName(pointName);

        try {
            if (detectionObject.containsKey(ParaKey.SENSOR)) {
                JSONObject sensorObject = (JSONObject) detectionObject.get("sensor");
                String code = sensorObject.getStr("code");
                String successCode = "0";
                if (!successCode.equals(code)) {
                    log.error("获取传感器数据异常，异常原因：" + sensorObject.getStr("message"));
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }

                JSONObject data = sensorObject.getJSONObject("data");
                BigDecimal temperature = data.getBigDecimal(Detection.TEMPERATURE);
                BigDecimal humidity = data.getBigDecimal(Detection.HUMIDITY);
                BigDecimal noise = data.getBigDecimal(Detection.NOISE);
                BigDecimal smoke = data.getBigDecimal(Detection.SMOKE);

                robotParamObject = robotParamService.getRobotParam(robotId);
                if (robotParamObject.containsKey(ParaKey.SENSOR_DIFFERENCE)) {
                    JSONObject sensorDifferenceObject = robotParamObject.getJSONObject(ParaKey.SENSOR_DIFFERENCE);
                    //加法
                    if (sensorDifferenceObject.containsKey(ParaKey.ADD)){
                        JSONObject addObject = sensorDifferenceObject.getJSONObject(ParaKey.ADD);
                        Double temperatureDifference = addObject.getDouble(Detection.TEMPERATURE);
                        Double humidityDifference = addObject.getDouble(Detection.HUMIDITY);
                        Double noisesDifference = addObject.getDouble(Detection.NOISE);
                        Double smokeDifference = addObject.getDouble(Detection.SMOKE);

                        temperature = temperature.add(new BigDecimal(temperatureDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                        humidity = humidity.add(new BigDecimal(humidityDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                        noise = noise.add(new BigDecimal(noisesDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                        smoke = smoke.add(new BigDecimal(smokeDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                    }

                    //乘法
                    if (sensorDifferenceObject.containsKey(ParaKey.MULTIPLY)){
                        JSONObject multiplyObject = sensorDifferenceObject.getJSONObject(ParaKey.MULTIPLY);
                        Double temperatureDifference = multiplyObject.getDouble(Detection.TEMPERATURE);
                        Double humidityDifference = multiplyObject.getDouble(Detection.HUMIDITY);
                        Double noisesDifference = multiplyObject.getDouble(Detection.NOISE);
                        Double smokeDifference = multiplyObject.getDouble(Detection.SMOKE);

                        temperature = temperature.multiply(new BigDecimal(temperatureDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                        humidity = humidity.multiply(new BigDecimal(humidityDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                        noise = noise.multiply(new BigDecimal(noisesDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                        smoke = smoke.multiply(new BigDecimal(smokeDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);

                    }

                    data.set(Detection.TEMPERATURE, temperature);
                    data.set(Detection.HUMIDITY, humidity);
                    data.set(Detection.NOISE, noise);
                    data.set(Detection.SMOKE, smoke);
                    sensorDifferenceObject = null;
                }

                if (robotParamObject.containsKey(ParaKey.SENSOR_DIFFERENCE_POINT_NAME)) {
                    JSONArray sensorDifferencePointNameArr = robotParamObject.getJSONArray(ParaKey.SENSOR_DIFFERENCE_POINT_NAME);
                    JSONObject sensorDifferenceObject;
                    for (int i = 0; i < sensorDifferencePointNameArr.size(); i++) {
                        JSONArray pointNameArr = sensorDifferencePointNameArr.getJSONObject(i).getJSONArray("point_name");
                        for (int j = 0; j < pointNameArr.size(); j++) {
                            String tempPointName = pointNameArr.getStr(j);
                            if (tempPointName.equals(pointName)) {
                                //加法
                                sensorDifferenceObject = sensorDifferencePointNameArr.getJSONObject(i).getJSONObject("senor");
                                if (sensorDifferenceObject.containsKey(ParaKey.ADD)){
                                    JSONObject addObject = sensorDifferenceObject.getJSONObject(ParaKey.ADD);
                                    Double temperatureDifference = addObject.getDouble(Detection.TEMPERATURE);
                                    Double humidityDifference = addObject.getDouble(Detection.HUMIDITY);
                                    Double noisesDifference = addObject.getDouble(Detection.NOISE);
                                    Double smokeDifference = addObject.getDouble(Detection.SMOKE);

                                    temperature = temperature.add(new BigDecimal(temperatureDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                                    humidity = humidity.add(new BigDecimal(humidityDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                                    noise = noise.add(new BigDecimal(noisesDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                                    smoke = smoke.add(new BigDecimal(smokeDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                                }

                                //乘法
                                if (sensorDifferenceObject.containsKey(ParaKey.MULTIPLY)){
                                    JSONObject multiplyObject = sensorDifferenceObject.getJSONObject(ParaKey.MULTIPLY);
                                    Double temperatureDifference = multiplyObject.getDouble(Detection.TEMPERATURE);
                                    Double humidityDifference = multiplyObject.getDouble(Detection.HUMIDITY);
                                    Double noisesDifference = multiplyObject.getDouble(Detection.NOISE);
                                    Double smokeDifference = multiplyObject.getDouble(Detection.SMOKE);

                                    temperature = temperature.multiply(new BigDecimal(temperatureDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                                    humidity = humidity.multiply(new BigDecimal(humidityDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                                    noise = noise.multiply(new BigDecimal(noisesDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);
                                    smoke = smoke.multiply(new BigDecimal(smokeDifference)).setScale(2,BigDecimal.ROUND_HALF_UP);

                                }

                                data.set(Detection.TEMPERATURE, temperature);
                                data.set(Detection.HUMIDITY, humidity);
                                data.set(Detection.NOISE, noise);
                                data.set(Detection.SMOKE, smoke);
                                sensorDifferenceObject = null;
                                break;
                            }
                        }
                    }
                }

                taskDetectionResult.setSensor(data.toString().toLowerCase());
                saveTaskDetectionResult(taskDetectionResult, "sensor");
                sensorObject = null;
                data = null;
            }

            if (detectionObject.containsKey(ParaKey.ALARM_LIGHTS)) {
                JSONObject alarmObject = (JSONObject) detectionObject.get(ParaKey.ALARM_LIGHTS);
                String code = alarmObject.getStr("code");
                String successCode = "0";
                if (!successCode.equals(code)) {
                    log.error("机器人上报报警灯数据异常，异常原因：" + alarmObject.getStr("message"));
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }
                if (!alarmObject.containsKey(ParaKey.DATA)){
                    log.error("机器人上报报警灯数据异常，异常原因：不存在data");
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }
                JSONObject alarmDataObject = alarmObject.getJSONObject("data");
                if (!alarmDataObject.containsKey(ParaKey.PATH)) {
                    log.error("机器人上报报警灯数据异常，异常原因：不存在报警灯图片");
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }

                //包含报警u位时，代表是ai迁移的数据
                if (alarmDataObject.containsKey("red_light_count")){
                    JSONArray pathArr = alarmDataObject.getJSONArray("path");
                    String alarm_light_merge_url = "";
                    if (pathArr.size() == 0){
                        throw new RuntimeException("报警灯检测项上传图片长度不能为0");
                    }
                    alarm_light_merge_url = pathArr.getStr(0);
                    pathArr.remove(0);
                    alarmDataObject.set("alarm_light_merge_url", alarm_light_merge_url);

                    JSONArray redLightCountArr = alarmDataObject.getJSONArray("red_light_count");
                    int redLightCount = 0;
                    for (int i = 0; i < redLightCountArr.size(); i++) {
                        redLightCount+=redLightCountArr.getInt(i);
                    }
                    if (redLightCount>0){
                        alarmDataObject.set("status", DetectionResult.ABNORMAL);
                    }else{
                        alarmDataObject.set("status", DetectionResult.NORMAL);
                    }
                }


                //u位占用统计是依据这里进行的传参
                if(alarmDataObject.containsKey("occupied_u_num")){
                    //U位占用
                    Integer useUbit = alarmDataObject.getInt("occupied_u_num");
                    try {
                        long roomId = taskInfoDao.getDetlById(taskInstance.getTaskId()).getRoomId();
                        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
                        if (roomParamObject.containsKey("room_setting")) {
                            JSONObject roomSettingObject = roomParamObject.getJSONObject("room_setting");
                            if (roomSettingObject.containsKey("ubit")){
                                Integer ubit = roomSettingObject.getInt("ubit");
                                CabinetUbit cabinetUbit = new CabinetUbit();
                                cabinetUbit.setRoomId(roomId);
                                cabinetUbit.setPointName(pointName);
                                cabinetUbit.setUbit(ubit);
                                cabinetUbit.setUseUbit(useUbit);
                                int freeUbit = ubit - useUbit;
                                int usageRate = useUbit * 100 / ubit;
                                cabinetUbit.setUsageRate(usageRate);
                                cabinetUbit.setFreeUbit(freeUbit);
                                cabinetUbitService.add(cabinetUbit);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally{
                        alarmDataObject.remove("occupied_u_num");
                    }
                }

                taskDetectionResult.setAlarmLight(alarmDataObject.toString());
                saveTaskDetectionResult(taskDetectionResult, Detection.ALARMLIGHT);
                alarmObject = null;
            }

            //灭火器检测
            if (detectionObject.containsKey("fire_extinguisher")) {
                JSONObject fireExtinguisherObject = (JSONObject) detectionObject.get("fire_extinguisher");
                String code = fireExtinguisherObject.getStr("code");
                String successCode = "0";
                if (!successCode.equals(code)) {
                    log.error("机器人上报灭火器数据异常，异常原因：" + fireExtinguisherObject.getStr("message"));
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }
                if (!fireExtinguisherObject.containsKey(ParaKey.DATA)){
                    log.error("机器人上报灭火器数据异常，异常原因：不存在data");
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }
                JSONObject fireExtinguisherDataObject = fireExtinguisherObject.getJSONObject("data");
                if (!fireExtinguisherDataObject.containsKey(ParaKey.PATH)) {
                    log.error("机器人上报灭火器数据异常，异常原因：不存在灭火器图片");
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }
                Boolean isWarning = fireExtinguisherDataObject.getBool("is_warning", false);
                if (isWarning){
                    fireExtinguisherDataObject.set("status", DetectionResult.ABNORMAL);
                }else{
                    fireExtinguisherDataObject.set("status", DetectionResult.NORMAL);
                }

                taskDetectionResult.setFireExtinguisher(fireExtinguisherDataObject.toString());
                saveTaskDetectionResult(taskDetectionResult, Detection.FIREEXTINGUISHER);
                fireExtinguisherObject = null;
            }

            if (detectionObject.containsKey(Detection.INFRARED)) {
                JSONObject infraredObject = (JSONObject) detectionObject.get(Detection.INFRARED);
                String code = infraredObject.getStr("code");
                String successCode = "0";
                if (!successCode.equals(code)) {
                    log.error("获取红外测温数据异常，异常原因：" + infraredObject.getStr("message"));
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }

                JSONObject data = infraredObject.getJSONObject("data");
                if (!data.containsKey(ParaKey.PATH)) {
                    log.error("机器人上报红外测温数据异常，异常原因：不存在红外测温图片数据");
                    mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                    return;
                }
                data.set("lifter_height", lifterHeight);
                JSONArray infraredArray = new JSONArray();
                String timestamp = data.getStr("timestamp");
                if(taskDetectionResultDao.checkExist(taskDetectionResult)){
                    //红外，因为有不同的高度，所以需要将原有数据取出整合后进行保存
                    //需要对取出的值进行处理，添加 for update 防止出现并发问题
                    TaskDetectionResult dbTaskDetectionResult = taskDetectionResultDao.getDetlByInstanceIdAndPointNameForUpdate(taskDetectionResult.getInstanceId(), taskDetectionResult.getPointName());
                    //ai前移后
                    //红外的前移仅支持无原始图片的数据，如需要原始数据，则红外不前移
                    if (data.containsKey("temperature_min")){
                        //此处也存在对数据库中的数据与现有数据的整合
                        String infrared = dbTaskDetectionResult.getInfrared();
                        JSONArray dbInfraredArr = new JSONArray(infrared);
                        int dbInfraredArrSize = dbInfraredArr.size();

                        JSONArray pathArr = data.getJSONArray("path");
                        if (pathArr.size() == 0){
                            throw new RuntimeException("红外测温检测项上传图片长度不能为0");
                        }
                        String infraredMergeUrl = pathArr.getStr(0);
                        JSONArray temperatureMinArr = data.getJSONArray("temperature_min");
                        JSONArray temperatureMaxArr = data.getJSONArray("temperature_max");
                        //如新上传的数据比数据库中的数据多，则认为需要更新，否则不更新
                        if (temperatureMinArr.size() > dbInfraredArrSize){
                            JSONObject tempObject;
                            JSONArray pathTempJsonArray = new JSONArray();
                            for (int i = 0; i < temperatureMinArr.size(); i++) {
                                tempObject = new JSONObject();
                                tempObject.set("max", commonService.getFormatValue(temperatureMaxArr.getDouble(i)));
                                tempObject.set("min", commonService.getFormatValue(temperatureMinArr.getDouble(i)));
                                tempObject.set("path", pathTempJsonArray);
                                tempObject.set("timestamp", timestamp);
                                if (i==0){
                                    tempObject.set("lifter_height", lifterHeight);
                                    tempObject.set("infrared_merge_url", infraredMergeUrl);
                                }
                                infraredArray.add(tempObject);
                            }
                        }else{
                            infraredArray = dbInfraredArr;
                        }
                    }else{
                        String dbInfraredStr = dbTaskDetectionResult.getInfrared();
                        infraredArray = new JSONArray(dbInfraredStr);
                        for (int i = 0; i < infraredArray.size(); i++) {
                            int dbLifterHeight = infraredArray.getJSONObject(i).getInt("lifter_height");
                            //判断是否有该高度数据，有则更新
                            if(lifterHeight == dbLifterHeight){
                                infraredArray.remove(i);
                                break;
                            }
                        }

                        infraredObject = null;
                        infraredArray.add(data);
                    }
                    dbTaskDetectionResult = null;
                }else{
                    //ai前移后
                    if (data.containsKey("temperature_min")){
                        JSONArray pathArr = data.getJSONArray("path");
                        if (pathArr.size() == 0){
                            throw new RuntimeException("红外测温检测项上传图片长度不能为0");
                        }
                        String infraredMergeUrl = pathArr.getStr(0);
                        JSONArray temperatureMinArr = data.getJSONArray("temperature_min");
                        JSONArray temperatureMaxArr = data.getJSONArray("temperature_max");
                        JSONObject tempObject;
                        JSONArray pathTempJsonArray = new JSONArray();
                        for (int i = 0; i < temperatureMinArr.size(); i++) {
                            tempObject = new JSONObject();
                            tempObject.set("max", commonService.getFormatValue(temperatureMaxArr.getDouble(i)));
                            tempObject.set("min", commonService.getFormatValue(temperatureMinArr.getDouble(i)));
                            tempObject.set("path", pathTempJsonArray);
                            tempObject.set("timestamp", timestamp);
                            if (i==0){
                                tempObject.set("lifter_height", lifterHeight);
                                tempObject.set("infrared_merge_url", infraredMergeUrl);
                            }
                            infraredArray.add(tempObject);
                        }
                    }else{
                        infraredObject = null;
                        infraredArray.add(data);
                    }
                }

                taskDetectionResult.setInfrared(infraredArray.toString());
                saveTaskDetectionResult(taskDetectionResult, Detection.INFRARED);
            }
            dataSourceTransactionManager.commit(transactionStatus);
            //唤起检测结果分析逻辑
            //此处添加数据到队列中，由队列进行数据分析
            AnalyseDetectionResult.queue.offer(taskDetectionResult);

            //添加一条验证，解决数据是否保存成功的问题
            //发送数据保存成功标志
            mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
        } catch (Exception e) {
            log.error(e.getMessage());
            dataSourceTransactionManager.rollback(transactionStatus);
        }
        jsonObject = null;
        dataObject = null;
        detectionObject = null;
    }

    /**
     * 保存任务检测结果
     * @param taskDetectionResult
     * @param detection
     * @return void
     * @author kliu
     * @date 2022/7/14 15:25
     */
    private void saveTaskDetectionResult(TaskDetectionResult taskDetectionResult, String detection){
        if(taskDetectionResultDao.checkExist(taskDetectionResult)){
            if(ParaKey.SENSOR.equals(detection)){
                taskDetectionResultDao.updateSensor(taskDetectionResult);
            }else if(Detection.ALARMLIGHT.equals(detection)){
                taskDetectionResultDao.updateAlarmLight(taskDetectionResult);
            }else if(Detection.INFRARED.equals(detection)){
                taskDetectionResultDao.updateInfrared(taskDetectionResult);
            }else if(Detection.FIREEXTINGUISHER.equals(detection)){
                taskDetectionResultDao.updateFireExtinguisher(taskDetectionResult);
            }else{
                throw new RuntimeException("保存TaskDetectionResult异常，传入参数不合法【"+detection+"】");
            }
        }else{
            taskDetectionResultDao.add(taskDetectionResult);
        }
    }
}
