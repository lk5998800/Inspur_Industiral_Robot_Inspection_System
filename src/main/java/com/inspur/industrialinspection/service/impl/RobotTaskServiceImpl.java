package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.RobotStatusService;
import com.inspur.industrialinspection.service.RobotTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 机器人任务服务
 * @author kliu
 * @date 2022/6/7 15:31
 */
@SuppressWarnings("AlibabaMethodTooLong")
@Service
public class RobotTaskServiceImpl implements RobotTaskService {

    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private RobotInfoDao robotInfoDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TaskDetectionSumDao taskDetectionSumDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private WarnInfoDao warnInfoDao;
    @Autowired
    private DetectionInfoDao detectionInfoDao;
    @Autowired
    private RobotStatusService robotStatusService;
    @Autowired
    private CommonService commonService;

    @Override
    public HashMap getRecentTaskBasic(long roomId){
        HashMap returnHm = new HashMap(8);
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        RobotInfo robotInfo = robotInfoDao.getDetlById(robotId);
        String robotName = robotInfo.getRobotName();
        RoomInfo roomInfo = roomInfoDao.getDetlById(roomId);
        String roomName = roomInfo.getRoomName();

        //判断机房是否有任务
        if(!taskDao.checkTaskExistByRoomId(roomId)){
            returnHm.put("robotName", robotName);
            returnHm.put("startTime", "-");
            returnHm.put("endTime", "-");
            returnHm.put("inspectionPosition", roomName);
            returnHm.put("infraredCount", "-");
            returnHm.put("alarmCount", "-");
            returnHm.put("temperatureAvg", "-");
            returnHm.put("humidityAvg", "-");
            return returnHm;
        }

        //获取机房下最新一次巡检任务
        TaskInstance taskInstance = taskDao.getRecentTaskByRoomId(roomId);
        String startTime = taskInstance.getStartTime();
        String endTime = taskInstance.getEndTime();
        if(StringUtils.isEmpty(endTime)){
            endTime = "-";
        }
        long instanceId = taskInstance.getInstanceId();
        //根据该次任务获取该次任务的执行异常情况
        //获取各检测项异常数量
        long infraredCount = 0;
        long alarmCount = 0;

        BigDecimal temperatureAvg = new BigDecimal(0);
        BigDecimal humidityAvg = new BigDecimal(0);

        List<TaskDetectionSum> list = taskDetectionSumDao.list(instanceId);
        for (TaskDetectionSum taskDetectionSum : list) {
            if (Detection.INFRARED.equals(taskDetectionSum.getDetectionId())){
                infraredCount=taskDetectionSum.getAbnormalCount();
            }
            if (Detection.ALARMLIGHT.equals(taskDetectionSum.getDetectionId())){
                alarmCount=taskDetectionSum.getAbnormalCount();
            }
            if (Detection.TEMPERATURE.equals(taskDetectionSum.getDetectionId())){
                temperatureAvg=taskDetectionSum.getAvg();
            }
            if (Detection.HUMIDITY.equals(taskDetectionSum.getDetectionId())){
                humidityAvg=taskDetectionSum.getAvg();
            }
        }

        returnHm.put("robotName", robotName);
        returnHm.put("startTime", startTime);
        returnHm.put("endTime", endTime);
        returnHm.put("inspectionPosition", roomName);
        returnHm.put("infraredCount", infraredCount+"");
        returnHm.put("alarmCount", alarmCount+"");
        returnHm.put("temperatureAvg", temperatureAvg.doubleValue()+"");
        returnHm.put("humidityAvg", humidityAvg.doubleValue()+"");
        return returnHm;
    }

    @Override
    public HashMap getAbnormalCountRecentDays7(long roomId){
        String day6Before = DateUtil.offset(DateUtil.date(), DateField.DAY_OF_MONTH, -6).toString("yyyy-MM-dd");
        List list = taskDetectionSumDao.getRecentAbnormalData(roomId, day6Before);

        int xArrLength = 7;

        String[] dateArr = new String[xArrLength];

        long[] unnormalArr = new long[xArrLength];

        for (int i = 0; i < xArrLength; i++) {
            dateArr[i] = DateUtil.offset(DateUtil.date(), DateField.DAY_OF_MONTH, -(xArrLength-1-i)).toString("yyyy-MM-dd");
        }

        Map map;
        for (int i = 0; i < dateArr.length; i++) {
            String dateStr = dateArr[i];
            long abnormalCount = 0;
            for (int j = 0; j < list.size(); j++) {
                map = (LinkedCaseInsensitiveMap) list.get(j);
                if(dateStr.equals(map.get("detection_date").toString())){
                    abnormalCount = (long) map.get("abnormal_count");
                }
            }
            unnormalArr[i] = abnormalCount;
        }
        
        HashMap returnHm = new HashMap(2);
        returnHm.put("dateArr", dateArr);
        returnHm.put("unnormalArr", unnormalArr);
        return returnHm;
    }

    @Override
    public HashMap getRecentTaskCabinetsInfo(long roomId) {
        TaskInstance taskInstance = taskDao.getRecentTaskByRoomId(roomId);
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.list(taskInstance.getInstanceId());

        String sensor, alarmLight, infrared;

        JSONObject jsonObject = null;

        ArrayList inspectsInfo = new ArrayList();
        HashMap inspectDetlHm = null;
        HashMap cabinetHm = null;
        HashMap inspectHm = null;
        JSONArray infraredArray = null;
        JSONObject tempObject, infraredObject = null;

        for (TaskDetectionResult taskDetectionResult : taskDetectionResults) {
            String pointNameStatus = DetectionResult.NORMAL;

            cabinetHm = new HashMap(3);
            inspectHm = new HashMap(4);
            cabinetHm.put("no", taskDetectionResult.getPointName());

            sensor = taskDetectionResult.getSensor();
            alarmLight = taskDetectionResult.getAlarmLight();
            infrared = taskDetectionResult.getInfrared();

            if(!StringUtils.isEmpty(sensor)){
                jsonObject = JSONUtil.parseObj(sensor);
                Object temperature = jsonObject.getObj(Detection.TEMPERATURE);
                inspectDetlHm = new HashMap(2);
                if(temperature instanceof Number){
                    inspectDetlHm.put("value", temperature);
                    inspectDetlHm.put("status", DetectionResult.NORMAL);
                    inspectHm.put(Detection.TEMPERATURE, inspectDetlHm);
                }else{
                    tempObject = JSONUtil.parseObj(temperature);
                    if(DetectionResult.ABNORMAL.equals(tempObject.getStr("status"))){
                        pointNameStatus = DetectionResult.ABNORMAL;
                    }
                    inspectDetlHm.put("value", tempObject.get("value"));
                    inspectDetlHm.put("status", tempObject.getStr("status"));
                    inspectHm.put(Detection.TEMPERATURE, inspectDetlHm);
                }

                Object humidty = jsonObject.getObj(Detection.HUMIDITY);
                inspectDetlHm = new HashMap(2);
                if(humidty instanceof Number){
                    inspectDetlHm.put("value", humidty);
                    inspectDetlHm.put("status", DetectionResult.NORMAL);
                    inspectHm.put(Detection.HUMIDITY, inspectDetlHm);
                }else{
                    tempObject = JSONUtil.parseObj(humidty);
                    if(DetectionResult.ABNORMAL.equals(tempObject.getStr("status"))){
                        pointNameStatus = DetectionResult.ABNORMAL;
                    }
                    inspectDetlHm.put("value", tempObject.get("value"));
                    inspectDetlHm.put("status", tempObject.getStr("status"));
                    inspectHm.put(Detection.HUMIDITY, inspectDetlHm);
                }

                Object noise = jsonObject.getObj(Detection.NOISE);
                inspectDetlHm = new HashMap(2);
                if(noise instanceof Number){
                    inspectDetlHm.put("value", noise);
                    inspectDetlHm.put("status", DetectionResult.NORMAL);
                    inspectHm.put(Detection.NOISE, inspectDetlHm);
                }else{
                    tempObject = JSONUtil.parseObj(noise);
                    if(DetectionResult.ABNORMAL.equals(tempObject.getStr("status"))){
                        pointNameStatus = DetectionResult.ABNORMAL;
                    }
                    inspectDetlHm.put("value", tempObject.get("value"));
                    inspectDetlHm.put("status", tempObject.getStr("status"));
                    inspectHm.put(Detection.NOISE, inspectDetlHm);
                }

                Object pm2p5 = jsonObject.getObj(Detection.PM2P5);
                inspectDetlHm = new HashMap(2);
                if(pm2p5 instanceof Number){
                    inspectDetlHm.put("value", pm2p5);
                    inspectDetlHm.put("status", DetectionResult.NORMAL);
                    inspectHm.put(Detection.PM2P5, inspectDetlHm);
                }else{
                    tempObject = JSONUtil.parseObj(pm2p5);
                    if(DetectionResult.ABNORMAL.equals(tempObject.getStr("status"))){
                        pointNameStatus = DetectionResult.ABNORMAL;
                    }
                    inspectDetlHm.put("value", tempObject.get("value"));
                    inspectDetlHm.put("status", tempObject.getStr("status"));
                    inspectHm.put(Detection.PM2P5, inspectDetlHm);
                }

                Object smoke = jsonObject.getObj(Detection.SMOKE);
                inspectDetlHm = new HashMap(2);
                if(smoke instanceof Number){
                    inspectDetlHm.put("value", smoke);
                    inspectDetlHm.put("status", DetectionResult.NORMAL);
                    inspectHm.put(Detection.SMOKE, inspectDetlHm);
                }else{
                    tempObject = JSONUtil.parseObj(smoke);
                    if(DetectionResult.ABNORMAL.equals(tempObject.getStr("status"))){
                        pointNameStatus = DetectionResult.ABNORMAL;
                    }
                    inspectDetlHm.put("value", tempObject.get("value"));
                    inspectDetlHm.put("status", tempObject.getStr("status"));
                    inspectHm.put(Detection.SMOKE, inspectDetlHm);
                }

            }

            if (!StringUtils.isEmpty(alarmLight)) {
                String alarmLightStatus = DetectionResult.NORMAL;
                tempObject = JSONUtil.parseObj(alarmLight);
                if(tempObject.containsKey("status")){
                    String status = tempObject.getStr("status");
                    if(DetectionResult.ABNORMAL.equals(status)){
                        pointNameStatus = DetectionResult.ABNORMAL;
                        alarmLightStatus = DetectionResult.ABNORMAL;
                    }
                }

                inspectDetlHm = new HashMap(1);
                inspectDetlHm.put("value", "-");
                inspectDetlHm.put("status", alarmLightStatus);
                inspectHm.put(Detection.ALARMLIGHT, inspectDetlHm);
            }

            if (!StringUtils.isEmpty(infrared)) {
                String infraredStatus = DetectionResult.NORMAL;
                infraredArray = JSONUtil.parseArray(infrared);
                double value = 0.0;
                for (int i = 0; i < infraredArray.size(); i++) {
                    infraredObject = infraredArray.getJSONObject(i);
                    if(infraredObject.containsKey("status")){
                        String status = infraredObject.getStr("status");
                        if(DetectionResult.ABNORMAL.equals(status)){
                            pointNameStatus = DetectionResult.ABNORMAL;
                            infraredStatus = DetectionResult.ABNORMAL;
                        }
                        if (infraredObject.getDouble("value")>value){
                            value = infraredObject.getDouble("value");
                        }
                    }else{
                        if (infraredObject.containsKey("max")){
                            if (infraredObject.getDouble("max")>value){
                                value = infraredObject.getDouble("max");
                            }
                        }
                    }
                }

                inspectDetlHm = new HashMap(1);
                inspectDetlHm.put("status", infraredStatus);
                inspectDetlHm.put("value", commonService.getFormatValue(value, 1));
                inspectHm.put(Detection.INFRARED, inspectDetlHm);
            }

            cabinetHm.put("status", pointNameStatus);
            cabinetHm.put("inspectInfo", inspectHm);
            inspectsInfo.add(cabinetHm);
        }

        //如果任务已经结束，则机器人位置返回00
        HashMap robotHm = new HashMap(1);
        robotHm.put("position", "00");
        //任务未结束
        if(StringUtils.isEmpty(taskInstance.getEndTime())){
            //更新时间最近的一条记录
            TaskDetectionResult taskDetectionResult = taskDetectionResultDao.getDetlByMaxUpdateTime(taskInstance.getInstanceId());
            if(taskDetectionResult != null){
                robotHm.put("position", taskDetectionResult.getPointName());
            }
            taskDetectionResult = null;
        }

        HashMap returnHm = new HashMap(2);

        returnHm.put("inspectsInfo", inspectsInfo);
        returnHm.put("robotInfo", robotHm);

        taskDetectionResults = null;
        inspectsInfo = null;
        inspectDetlHm = null;
        cabinetHm = null;
        inspectHm = null;
        infraredArray = null;
        tempObject = null;
        infraredObject = null;
        robotHm = null;
        return returnHm;
    }

    @Override
    public HashMap getRecentTaskWarnInfo(long roomId) {
        TaskInstance taskInstance = taskDao.getRecentTaskByRoomId(roomId);
        long instanceId = taskInstance.getInstanceId();
        List warningInfo = new ArrayList();
        HashMap warnDetl;
        List<WarnInfo> warnInfos = warnInfoDao.listByInstanceId(instanceId);
        for (WarnInfo warnInfo : warnInfos) {
            warnDetl = new HashMap(4);
            warnDetl.put("no", warnInfo.getPointName());
            warnDetl.put("type", detectionInfoDao.getDetlById(warnInfo.getDetectionId()).getDetectionName());
            warnDetl.put("level", warnInfo.getLevel());
            warnDetl.put("time", warnInfo.getWarnTime());
            warningInfo.add(warnDetl);
        }

        HashMap returnHm = new HashMap(1);

        returnHm.put("warningInfo", warningInfo);

        return returnHm;
    }

    @Override
    public HashMap getRobotRunStatusInfo(long roomId) {
        HashMap returnHm = new HashMap(7);
        int runRobotCount = 1;
        int offlineRobotCount = 0;
        int chargeRobotCount = 0;

        int runHour = 0;
        int detectionPointCount = 0;
        int alarmCount = 0;

        try {
            TaskInstance taskInstance = taskDao.getRecentTaskByRoomId(roomId);
            long instanceId = taskInstance.getInstanceId();

            long timeDiff = 0;
            //任务运行中对应结束时间为空，则运行机器人为1，充电机器人为0，离线机器人一直认为是0
            String endTime = taskInstance.getEndTime();
            String startTime = taskInstance.getStartTime();
            if(StringUtils.isEmpty(endTime)){
                runRobotCount = 1;
                chargeRobotCount = 0;
                timeDiff += DateUtil.date().getTime() - DateUtil.parseDateTime(startTime).getTime();
            }else{
                runRobotCount = 0;
                chargeRobotCount = 1;
                timeDiff += DateUtil.parseDateTime(endTime).getTime() - DateUtil.parseDateTime(startTime).getTime();
            }

            //检测点位
            detectionPointCount = taskDetectionResultDao.count(instanceId);
            //报警点位
            List<WarnInfo> warnInfos = warnInfoDao.listByInstanceId(instanceId);
            for (int i = 0; i < warnInfos.size(); i++) {
                long taskLogIdi = warnInfos.get(i).getTaskLogId();
                for (int j = i+1; j < warnInfos.size(); j++) {
                    long taskLogIdj = warnInfos.get(j).getTaskLogId();
                    if (taskLogIdi == taskLogIdj) {
                        warnInfos.remove(j);
                        j--;
                    }
                }
            }

            alarmCount = warnInfos.size();
        }catch(Exception e){

        }

        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        String robotPower = robotStatusService.getRobotPower(robotId);

        JSONObject robotStatusObject = robotStatusService.getRobotStatus(robotId);

        //noinspection AlibabaUndefineMagicConstant
        if (robotStatusObject.containsKey("robotStatus")){
            //noinspection AlibabaUndefineMagicConstant
            if ("online".equals(robotStatusObject.getStr("robotStatus"))){
                if (robotStatusObject.containsKey(ParaKey.CHARGING)) {
                    if (robotStatusObject.getBool(ParaKey.CHARGING)) {
                        //在充电
                        offlineRobotCount = 0;
                        runRobotCount = 0;
                        chargeRobotCount = 1;
                    }else{
                        //不在充电
                        offlineRobotCount = 0;
                        runRobotCount = 1;
                        chargeRobotCount = 0;
                    }
                }else{
                    if (StrUtil.DASHED.equals(robotPower)) {
                        offlineRobotCount = 1;
                        runRobotCount = 0;
                        chargeRobotCount = 0;
                    }
                }
            }else{
                offlineRobotCount = 1;
                runRobotCount = 0;
                chargeRobotCount = 0;
            }
        }

        returnHm.put("runRobotCount", "00"+runRobotCount);
        returnHm.put("offlineRobotCount", "00"+offlineRobotCount);
        returnHm.put("chargeRobotCount", "00"+chargeRobotCount);

        returnHm.put("runHour", runHour);
        returnHm.put("detectionPointCount", detectionPointCount);
        returnHm.put("alarmCount", alarmCount);
        returnHm.put("robotPower", robotPower);
        return returnHm;
    }
}
