package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.dao.TaskDetectionSumDao;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.dao.WarnInfoDao;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.entity.WarnInfo;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.TaskReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @projectName: platform_app_service
 * @package: com.inspur.industrial_robot.service.impl
 * @className: DetectionReportServiceImpl
 * @author: kliu
 * @description: 任务报告服务
 * @date: 2022/4/12 16:50
 * @version: 1.0
 */
@Service
public class TaskReportServiceImpl implements TaskReportService {
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private TaskDetectionSumDao taskDetectionSumDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private WarnInfoDao warnInfoDao;
    @Autowired
    private CommonService commonService;

    @Override
    public HashMap getTaskReportOverview(long instanceId) {
        String startTime = "";
        String endTime = "";
        //巡检时长
        int inspectionMinute = 0;
        String inspectionHourStr = "";
        long timeDiff = 0;


        TaskInstance taskInstance = taskInstanceDao.getDetlById(instanceId);
        String dbStartTime = taskInstance.getStartTime();
        String dbEndTime = taskInstance.getEndTime();
        if(StringUtils.isEmpty(startTime)){
            startTime = dbStartTime;
        }

        if(StringUtils.isEmpty(endTime)){
            endTime = dbEndTime;
        }

        if (StringUtils.isEmpty(dbEndTime)) {
            dbEndTime = DateUtil.now();
        }
        timeDiff += DateUtil.parseDateTime(dbEndTime).getTime() - DateUtil.parseDateTime(dbStartTime).getTime();

        //分钟
        inspectionMinute = (int) (timeDiff / 60000);

        int inspectionHour = inspectionMinute/60;
        inspectionMinute = inspectionMinute%60;

        if (inspectionHour>0){
            inspectionHourStr+=inspectionHour+"小时";
        }
        if (inspectionMinute>0){
            inspectionHourStr+=inspectionMinute+"分钟";
        }

        int abnormalCount = 0;
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.list(instanceId);
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

        if (StringUtils.isEmpty(endTime)) {
            endTime = "-";
        }
        
        HashMap hashMap = new HashMap(6);
        hashMap.put("startTime", startTime);
        hashMap.put("endTime", endTime);
        hashMap.put("inspectionHour", inspectionHourStr);
        hashMap.put("inspectionCount", 1);
        hashMap.put("inspectCount", taskDetectionResults.size());
        hashMap.put("abnormalCount", warnInfos.size());
        taskDetectionResults = null;
        warnInfos = null;
        return hashMap;
    }

    @Override
    public List getTaskReportDetectionOverview(long instanceId) {
        return taskDetectionSumDao.list(instanceId);
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public List getTaskDetectionDetlInfo(long instanceId) {
        List list = new ArrayList();
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.list(instanceId);
        JSONObject jsonObject, tempObject, detectedObject;
        JSONArray tempArray;
        for (TaskDetectionResult taskDetectionResult : taskDetectionResults) {
            String pointName = taskDetectionResult.getPointName();
            String sensor = taskDetectionResult.getSensor();
            String alarmLight = taskDetectionResult.getAlarmLight();
            String infrared = taskDetectionResult.getInfrared();
            jsonObject = new JSONObject();
            jsonObject.set("pointName", pointName.replace("上","").replace("下",""));
            if(!StringUtils.isEmpty(sensor)){
                detectedObject = JSONUtil.parseObj(sensor);
                Object o;
                if (detectedObject.containsKey(Detection.TEMPERATURE)) {
                    o = detectedObject.get(Detection.TEMPERATURE);
                }else{
                    o = 0;
                }

                tempObject = new JSONObject();
                if(o instanceof Number){
                    tempObject.set("value", o);
                    tempObject.set("status", DetectionResult.NORMAL);
                    jsonObject.set(Detection.TEMPERATURE, tempObject);
                }else{
                    tempObject.set("value", ((JSONObject)o).getDouble("value"));
                    tempObject.set("status", ((JSONObject)o).getStr("status"));
                    tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
                    jsonObject.set(Detection.TEMPERATURE, tempObject);
                }

                if (detectedObject.containsKey(Detection.HUMIDITY)) {
                    o = detectedObject.get(Detection.HUMIDITY);
                }else{
                    o = 0;
                }

                tempObject = new JSONObject();
                if(o instanceof Number){
                    tempObject.set("value", o);
                    tempObject.set("status", DetectionResult.NORMAL);
                    jsonObject.set(Detection.HUMIDITY, tempObject);
                }else{
                    tempObject.set("value", ((JSONObject)o).getDouble("value"));
                    tempObject.set("status", ((JSONObject)o).getStr("status"));
                    tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
                    jsonObject.set(Detection.HUMIDITY, tempObject);
                }

                if (detectedObject.containsKey(Detection.NOISE)) {
                    o = detectedObject.get(Detection.NOISE);
                }else{
                    o = 0;
                }

                tempObject = new JSONObject();
                if(o instanceof Number){
                    tempObject.set("value", o);
                    tempObject.set("status", DetectionResult.NORMAL);
                    jsonObject.set(Detection.NOISE, tempObject);
                }else{
                    tempObject.set("value", ((JSONObject)o).getDouble("value"));
                    tempObject.set("status", ((JSONObject)o).getStr("status"));
                    tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
                    jsonObject.set(Detection.NOISE, tempObject);
                }

                if (detectedObject.containsKey(Detection.PM2P5)) {
                    o = detectedObject.get(Detection.PM2P5);
                }else{
                    o = 0;
                }

                tempObject = new JSONObject();
                if(o instanceof Number){
                    tempObject.set("value", o);
                    tempObject.set("status", DetectionResult.NORMAL);
                    jsonObject.set(Detection.PM2P5, tempObject);
                }else{
                    tempObject.set("value", ((JSONObject)o).getDouble("value"));
                    tempObject.set("status", ((JSONObject)o).getStr("status"));
                    tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
                    jsonObject.set(Detection.PM2P5, tempObject);
                }

                if (detectedObject.containsKey(Detection.SMOKE)) {
                    o = detectedObject.get(Detection.SMOKE);
                }else{
                    o = 0;
                }
                tempObject = new JSONObject();
                if(o instanceof Number){
                    tempObject.set("value", o);
                    tempObject.set("status", DetectionResult.NORMAL);
                    jsonObject.set(Detection.SMOKE, tempObject);
                }else{
                    tempObject.set("value", ((JSONObject)o).getDouble("value"));
                    tempObject.set("status", ((JSONObject)o).getStr("status"));
                    tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
                    jsonObject.set(Detection.SMOKE, tempObject);
                }
            }

            if(!StringUtils.isEmpty(alarmLight)){
                String alarmLightMergeUrl = null;
                detectedObject = JSONUtil.parseObj(alarmLight);
                tempObject = new JSONObject();
                if(!detectedObject.containsKey("status")){
                    tempObject.set("status", DetectionResult.NORMAL);
                    jsonObject.set(Detection.ALARMLIGHT, tempObject);
                }else{
                    if(detectedObject.containsKey("alarm_light_merge_url")){
                        alarmLightMergeUrl = detectedObject.getStr("alarm_light_merge_url");
                        alarmLightMergeUrl = commonService.url2Https(alarmLightMergeUrl);
                        tempObject.set("alarmLightMergeUrl", alarmLightMergeUrl);
                    }
                    tempObject.set("status", detectedObject.getStr("status"));
                    jsonObject.set(Detection.ALARMLIGHT, tempObject);
                }
            }

            if(!StringUtils.isEmpty(infrared)){
                double value = 0;
                String infraredMergeUrl = null;
                tempArray = JSONUtil.parseArray(infrared);
                boolean abnormalFlag = false;
                for (int i = 0; i < tempArray.size(); i++) {
                    tempObject = tempArray.getJSONObject(i);
                    if(tempObject.containsKey("status")){
                        if(DetectionResult.ABNORMAL.equals(tempObject.getStr("status"))){
                            abnormalFlag = true;
                        }
                        if(i==0){
                            if(tempObject.containsKey("infrared_merge_url")){
                                infraredMergeUrl = tempObject.getStr("infrared_merge_url");
                                infraredMergeUrl = commonService.url2Https(infraredMergeUrl);
                            }
                        }

                        if(tempObject.getDouble("max")>value){
                            value = tempObject.getDouble("max");
                        }
                    }
                }
                tempObject = new JSONObject();
                tempObject.set("infraredMergeUrl", infraredMergeUrl);
                if(value == 0){
                    tempObject.set("value", "-");
                }else{
                    tempObject.set("value", value);
                }
                if(abnormalFlag){
                    tempObject.set("status", DetectionResult.ABNORMAL);
                    jsonObject.set(Detection.INFRARED, tempObject);
                }else{
                    tempObject.set("status", DetectionResult.NORMAL);
                    jsonObject.set(Detection.INFRARED, tempObject);
                }
            }

            list.add(jsonObject);
        }

        taskDetectionResults = null;
        return list;
    }

    @Override
    public HashMap getTaskReport(long instanceId) {
        HashMap overViewHm = getTaskReportOverview(instanceId);
        List detectionDetlList = getTaskDetectionDetlInfo(instanceId);
        List detectionList = getTaskReportDetectionOverview(instanceId);

        HashMap returnHm = new HashMap(3);
        returnHm.put("overview", overViewHm);
        returnHm.put("detectionDetlList", detectionDetlList);
        returnHm.put("detectionList", detectionList);

        overViewHm = null;
        detectionDetlList = null;
        detectionList = null;

        return returnHm;
    }
}
