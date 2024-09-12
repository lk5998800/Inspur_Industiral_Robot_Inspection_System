package com.inspur.industrialinspection.thread;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.aliyun.eventbridge.models.CloudEvent;
import com.aliyun.eventbridge.util.EventBuilder;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.AiAgentService;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.RoomParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author kliu
 * @description 分析检测项结果，调用aiagent或者比对阈值
 * @date 2022/4/15 11:33
 */
@Slf4j
@Service
public class AnalyseDetectionResult{

    public static Queue<TaskDetectionResult> queue = new LinkedList<TaskDetectionResult>();

    @Value("${aiagent.service.url}")
    private String aiagentUrl;
    @Value("${aiagent.service.alarmlight.url}")
    private String aiagentAlarmLightUrl;
    @Value("${aiagent.service.infrared.url}")
    private String aiagentInfraredUrl;
    @Value("${sms.source}")
    private String source;
    @Value("${sms.subject}")
    private String subject;
    @Value("${sms.busName}")
    private String busName;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private WarnInfoDao warnInfoDao;
    @Autowired
    private TaskDetectionSumService taskDetectionSumService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private WarnSmsDao warnSmsDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private DetectionInfoDao detectionInfoDao;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    private AiAgentService aiAgentService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;

    /**
     * 初始化分析检测项结果线程，类初始化时调用，往线程池中添加一个一直执行的线程
     * @author kliu
     * @date 2022/5/27 8:41
     */
    @PostConstruct
    public void initThreadPool(){
        executor.submit(() -> {
            try {
                startAnalyseDetectionThread();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 开始分析检测项结果，每隔10s遍历队列，实现分析，报错后记录log，后续有cron分析
     * @author kliu
     * @date 2022/5/27 8:42
     */
    public void startAnalyseDetectionThread() throws InterruptedException {
        TaskDetectionResult taskDetectionResult = null;
        while (true){
            if(queue.isEmpty()){
                Thread.sleep(10000);
                continue;
            }

            //poll() - 返回并删除队列的开头。 如果队列为空，则返回null。
            taskDetectionResult = queue.poll();
            if(taskDetectionResult!=null){
                try {
                    analyseDetectionResult(taskDetectionResult);
                }catch (Exception e){
                    log.error("分析检测项结果异常:"+e.getMessage());
                }
            }
        }
    }

    /**
     * 检测项结果分析
     * @param taskDetectionResult
     * @return void
     * @author kliu
     * @date 2022/5/27 8:50
     */
    public void analyseDetectionResult(TaskDetectionResult taskDetectionResult){
        try {
            //传感器
            if(!StringUtils.isEmpty(taskDetectionResult.getSensor())){
                try {
                    sensorDataAnalyse(taskDetectionResult);
                } catch (Exception e) {
                    log.error("分析传感器数据【"+taskDetectionResult.getSensor()+"】异常："+e.getMessage());
                }
            }

            //报警灯
            if(!StringUtils.isEmpty(taskDetectionResult.getAlarmLight())){
                try {
                    alarmLightDataAnalyse(taskDetectionResult);
                } catch(Exception e) {
                    log.error("分析报警灯数据【"+taskDetectionResult.getAlarmLight()+"】异常："+e.getMessage());
                }
            }

            //红外测温
            if(!StringUtils.isEmpty(taskDetectionResult.getInfrared())){
                try {
                    infraredDataAnalyse(taskDetectionResult);
                } catch (Exception e) {
                    log.error("分析红外测温数据【"+taskDetectionResult.getInfrared()+"】异常："+e.getMessage());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        //按任务汇总数据
        executor.submit(() -> {
            try {
                taskDetectionSumService.taskDetectionSum(taskDetectionResult);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 环境检测数据比对分析
     * @param taskDetectionResult
     * @return void
     * @author kliu
     * @date 2022/5/27 8:51
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    public void sensorDataAnalyse(TaskDetectionResult taskDetectionResult){
        long instanceId = taskDetectionResult.getInstanceId();
        String pointName = taskDetectionResult.getPointName();

        WarnInfo warnInfo = new WarnInfo();

        //依据taskId获取机房id
        long taskId = taskInstanceDao.getDetlById(instanceId).getTaskId();
        TaskInfo taskInfo = taskInfoDao.getDetlById(taskId);
        long roomId = taskInfo.getRoomId();
        long inspectTypeId = taskInfo.getInspectTypeId();

        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);

        //根据实例id和点位获取检测项结果，每次从库里获取
        TaskDetectionResult dbTaskDetectionResult = taskDetectionResultDao.getDetlByInstanceIdAndPointName(instanceId, pointName);
        String sensorStr = dbTaskDetectionResult.getSensor();
        JSONObject sensorObject = new JSONObject(sensorStr);
        Object temperature = sensorObject.get("temperature");
        //该数据未处理
        if (temperature instanceof Number) {

        } else {
            return;
        }

        JSONArray detectionThresholdArray = new JSONArray();
        if (roomParamObject.containsKey("inspect_setting")){
            JSONObject inspectSettingObject = roomParamObject.getJSONObject("inspect_setting");
            String combinationCode = "";
            if (inspectSettingObject.containsKey("inspect_type")) {
                JSONArray inspectTypeArr = inspectSettingObject.getJSONArray("inspect_type");
                JSONObject jsonObject;
                for (int i = 0; i < inspectTypeArr.size(); i++) {
                    if (!StringUtils.isEmpty(combinationCode)){
                        break;
                    }
                    jsonObject = inspectTypeArr.getJSONObject(i);
                    Long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
                    if (inspectTypeId1 == inspectTypeId){
                        JSONArray inspectDetectionCombinationArr = jsonObject.getJSONArray("inspect_detection_combination");
                        for (int j = 0; j < inspectDetectionCombinationArr.size(); j++) {
                            if (!StringUtils.isEmpty(combinationCode)){
                                break;
                            }
                            jsonObject = inspectDetectionCombinationArr.getJSONObject(j);
                            JSONArray pointNameArr = jsonObject.getJSONArray("point_names");
                            String tempCombinationCode = jsonObject.getStr("combination_code");
                            for (int i1 = 0; i1 < pointNameArr.size(); i1++) {
                                if (pointNameArr.getStr(i1).equals(pointName)) {
                                    combinationCode = tempCombinationCode;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (inspectSettingObject.containsKey("detection_combination")) {
                JSONArray detectionCombinationArr = inspectSettingObject.getJSONArray("detection_combination");
                JSONObject jsonObject;
                for (int i = 0; i < detectionCombinationArr.size(); i++) {
                    jsonObject = detectionCombinationArr.getJSONObject(i);
                    if (jsonObject.getStr("combination_code").equals(combinationCode)) {
                        detectionThresholdArray = jsonObject.getJSONArray("detection_para");
                        break;
                    }
                }
            }
        }

        JSONObject detectionFileObject, resultObject;
        String threshold;

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            //阈值配置现在根据检测项组合来，这样解决了相同检测项不同检测阈值的问题
            Set<String> strings = sensorObject.keySet();
            Iterator<String> iterator = strings.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                //要比较的值
                Double compareValue = sensorObject.getDouble(key);

                if (key.equals(Detection.SMOKE)){
                    //烟雾单独处理，因为烟雾只返回01 0 为无  1 为有
                    resultObject = new JSONObject();
                    resultObject.set("value", compareValue);
                    resultObject.set("status", DetectionResult.NORMAL);
                    resultObject.set("thresholdLevel", "预警");
                    resultObject.set("threshold", "0");

                    if(compareValue>0){
                        resultObject.set("status", DetectionResult.ABNORMAL);
                        warnInfo.setTaskLogId(dbTaskDetectionResult.getTaskLogId());
                        warnInfo.setPointName(dbTaskDetectionResult.getPointName());
                        warnInfo.setDetectionId(key);
                        warnInfo.setWarnTime(sensorObject.getStr("timestamp"));

                        //将数据写入告警信息表
                        if(warnInfoDao.checkExist(warnInfo)){
                            warnInfoDao.update(warnInfo);
                        }else{
                            warnInfoDao.add(warnInfo);
                        }

                        //整合阈值匹配结果与现有数据，以便更新
                        resultObject.set("status", DetectionResult.ABNORMAL);
                        //发送短信告知异常
                        detectionResultExceptionSendSms(taskId, pointName, roomId, key);
                    }
                    sensorObject.set(key, resultObject);
                    continue;
                }

                for (int i = 0; i < detectionThresholdArray.size(); i++) {
                    detectionFileObject = detectionThresholdArray.getJSONObject(i);
                    String arrDetectionId = detectionFileObject.getStr("detection_id");
                    threshold = detectionFileObject.getStr("threshold");
                    if (arrDetectionId.equals(key)) {
                        double lower = Double.NEGATIVE_INFINITY;
                        double upper;

                        resultObject = new JSONObject();
                        resultObject.set("value", compareValue);
                        resultObject.set("status", DetectionResult.NORMAL);
                        resultObject.set("thresholdLevel", "预警");
                        resultObject.set("threshold", threshold);

                        //是否为高低双阈值
                        if (StrUtil.count(threshold,"-")>0){
                            //双阈值 小于小的  大于大的
                            String[] split = threshold.split("-");
                            lower = Double.parseDouble(split[0]);
                            upper = Double.parseDouble(split[1]);
                        }else{
                            //单阈值，小于即可
                            upper = Double.parseDouble(threshold);
                        }

                        //判断是否小于lower 或者大于 upper
                        if (compareValue < lower || compareValue > upper){
                            resultObject.set("status", DetectionResult.ABNORMAL);
                            warnInfo.setTaskLogId(dbTaskDetectionResult.getTaskLogId());
                            warnInfo.setPointName(dbTaskDetectionResult.getPointName());
                            warnInfo.setDetectionId(key);
                            warnInfo.setWarnTime(sensorObject.getStr("timestamp"));

                            //将数据写入告警信息表
                            if(warnInfoDao.checkExist(warnInfo)){
                                warnInfoDao.update(warnInfo);
                            }else{
                                warnInfoDao.add(warnInfo);
                            }

                            //整合阈值匹配结果与现有数据，以便更新
                            resultObject.set("status", DetectionResult.ABNORMAL);
                            //发送短信告知异常
                            detectionResultExceptionSendSms(taskId, pointName, roomId, key);
                        }

                        sensorObject.set(key, resultObject);
                        break;
                    }

                }
            }
            taskDetectionResult.setSensor(sensorObject.toString());
            taskDetectionResultDao.updateSensor(taskDetectionResult);
            dataSourceTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            dataSourceTransactionManager.rollback(transactionStatus);
            throw new RuntimeException(e.getMessage());
        } finally{
            detectionThresholdArray = null;
            dbTaskDetectionResult = null;
            sensorObject = null;
            resultObject = null;
        }
    }


    /**
     * @param taskDetectionResult
     * @author kliu
     * @description 报警灯检测比对分析
     * @date 2022/4/12 13:49
     */
    public void alarmLightDataAnalyse(TaskDetectionResult taskDetectionResult){
        long instanceId = taskDetectionResult.getInstanceId();
        String pointName = taskDetectionResult.getPointName();

        //依据taskId获取机房id
        long taskId = taskInstanceDao.getDetlById(instanceId).getTaskId();
        long roomId = taskInfoDao.getDetlById(taskId).getRoomId();

        TaskDetectionResult dbTaskDetectionResult = taskDetectionResultDao.getDetlByInstanceIdAndPointName(instanceId, pointName);
        String alarmLightStr = dbTaskDetectionResult.getAlarmLight();
        JSONObject alarmLightObject = new JSONObject(alarmLightStr);
        //含status说明已经处理过
        if(alarmLightObject.containsKey(ParaKey.STATUS)){
            return;
        }

        String status = DetectionResult.NORMAL;
        //调用aiservice
        String url = aiagentUrl+aiagentAlarmLightUrl;
        JSONObject serviceObject = new JSONObject();
        serviceObject.set("image_url", alarmLightObject.getJSONArray("path"));
        JSONObject serviceResult = aiAgentService.invokeHttp(url, serviceObject.toString());
        JSONArray redLightCount = serviceResult.getJSONArray("red_light_count");

        String alarmLightMergeUrl = serviceResult.getStr("alarm_light_merge_url");

        int redLightCountSum = 0;
        for (int i = 0; i < redLightCount.size(); i++) {
            redLightCountSum+=redLightCount.getInt(i);
        }

        if(redLightCountSum>0){
            status = DetectionResult.ABNORMAL;
        }

        alarmLightObject.set("alarm_light_merge_url", alarmLightMergeUrl);
        alarmLightObject.set("status", status);
        alarmLightObject.set("red_light_count", redLightCount);

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

        try {
            taskDetectionResult.setAlarmLight(alarmLightObject.toString());
            taskDetectionResultDao.updateAlarmLight(taskDetectionResult);

            if(redLightCountSum>0){
                WarnInfo warnInfo = new WarnInfo();
                warnInfo.setTaskLogId(dbTaskDetectionResult.getTaskLogId());
                warnInfo.setPointName(dbTaskDetectionResult.getPointName());
                warnInfo.setDetectionId(Detection.ALARMLIGHT);
                warnInfo.setWarnTime(alarmLightObject.getStr("timestamp"));
                if(warnInfoDao.checkExist(warnInfo)){
                    warnInfoDao.update(warnInfo);
                }else{
                    warnInfoDao.add(warnInfo);
                }

                //发送短信告知异常
                detectionResultExceptionSendSms(taskId, pointName, roomId, Detection.ALARMLIGHT);
            }

            dataSourceTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            dataSourceTransactionManager.rollback(transactionStatus);
            throw new RuntimeException(e.getMessage());
        } finally {
            dbTaskDetectionResult = null;
            alarmLightObject = null;
        }
    }

    /**
     * @param taskDetectionResult
     * @author kliu
     * @description 红外测温比对分析
     * @date 2022/4/12 14:14
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    public void infraredDataAnalyse(TaskDetectionResult taskDetectionResult){
        long instanceId = taskDetectionResult.getInstanceId();
        String pointName = taskDetectionResult.getPointName();

        //依据taskId获取机房id
        long taskId = taskInstanceDao.getDetlById(instanceId).getTaskId();
        TaskInfo taskInfo = taskInfoDao.getDetlById(taskId);
        long roomId = taskInfo.getRoomId();
        long inspectTypeId = taskInfo.getInspectTypeId();

        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);

        //锁表操作，防止检测项结果上传时，导致数据丢失
        TaskDetectionResult dbTaskDetectionResult = taskDetectionResultDao.getDetlByInstanceIdAndPointNameForUpdate(instanceId, pointName);
        String infraredStr = dbTaskDetectionResult.getInfrared();
        JSONArray arrays = new JSONArray(infraredStr);
        JSONObject jsonObject;
        JSONArray pathTempArr;

        //数据是否分析
        boolean dataAnalyseFlag=false;
        boolean aiFrontFlag = false;
        //红外照片需要全部匹配，要进行整合，4月份版本，以点位作为异常数量进行计算，如一个点位3个红外异常，也认为是一个异常
        for (int i = 0; i < arrays.size(); i++) {
            jsonObject = arrays.getJSONObject(i);
            //包含status说明已比对完成
            if (jsonObject.containsKey("status")) {
                dataAnalyseFlag = true;
            }else{
                //如果不含status而包含max说明是ai前移后的保存数据
                if (jsonObject.containsKey("max")){
                    aiFrontFlag = true;
                }
                dataAnalyseFlag = false;
                break;
            }
        }

        //已经比对完成不再进行比对
        if (dataAnalyseFlag) {
            return;
        }

        JSONArray pathArray = new JSONArray();
        if (!dataAnalyseFlag) {
            for (int i = 0; i < arrays.size(); i++) {
                jsonObject = arrays.getJSONObject(i);
                //包含status说明已比对完成
                pathTempArr = jsonObject.getJSONArray("path");
                if(pathTempArr == null){
                    log.error("未获取到红外测温数据，请检查上传数据");
                    continue;
                }
                if(pathTempArr.size() == 1){
                    pathArray.add(pathTempArr.getStr(0));
                    continue;
                }else if(pathTempArr.size() == 0){
                    //ai前移有时候没有图片
                    continue;
                }
                log.error("目前红外测温仅支持一张图片上传");
                return;
            }
        }

        JSONArray detectionThresholdArray = new JSONArray();

        JSONObject resultObject, detectionObject;

        if (roomParamObject.containsKey("inspect_setting")){
            JSONObject inspectSettingObject = roomParamObject.getJSONObject("inspect_setting");
            String combinationCode = "";
            if (inspectSettingObject.containsKey("inspect_type")) {
                JSONArray inspectTypeArr = inspectSettingObject.getJSONArray("inspect_type");
                for (int i = 0; i < inspectTypeArr.size(); i++) {
                    if (!StringUtils.isEmpty(combinationCode)){
                        break;
                    }
                    jsonObject = inspectTypeArr.getJSONObject(i);
                    Long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
                    if (inspectTypeId1 == inspectTypeId){
                        JSONArray inspectDetectionCombinationArr = jsonObject.getJSONArray("inspect_detection_combination");
                        for (int j = 0; j < inspectDetectionCombinationArr.size(); j++) {
                            if (!StringUtils.isEmpty(combinationCode)){
                                break;
                            }
                            jsonObject = inspectDetectionCombinationArr.getJSONObject(j);
                            JSONArray pointNameArr = jsonObject.getJSONArray("point_names");
                            String tempCombinationCode = jsonObject.getStr("combination_code");
                            for (int i1 = 0; i1 < pointNameArr.size(); i1++) {
                                if (pointNameArr.getStr(i1).equals(pointName)) {
                                    combinationCode = tempCombinationCode;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (inspectSettingObject.containsKey("detection_combination")) {
                JSONArray detectionCombinationArr = inspectSettingObject.getJSONArray("detection_combination");
                for (int i = 0; i < detectionCombinationArr.size(); i++) {
                    jsonObject = detectionCombinationArr.getJSONObject(i);
                    if (jsonObject.getStr("combination_code").equals(combinationCode)) {
                        detectionThresholdArray = jsonObject.getJSONArray("detection_para");
                    }
                }
            }
        }

        //红外测温支持传入阈值最低值进行超阈值标识，计算最低阈值
        float threshold = 30f;
        for (int i = 0; i < detectionThresholdArray.size(); i++) {
            detectionObject = detectionThresholdArray.getJSONObject(i);
            String arrDetectionId = detectionObject.getStr("detection_id");
            if (Detection.INFRARED.equals(arrDetectionId)) {
                String thresholdStr = detectionObject.getStr("threshold");
                if (StrUtil.count(thresholdStr, "-")>0){
                    thresholdStr = thresholdStr.split("-")[1];
                }

                threshold = Float.parseFloat(thresholdStr);
                break;
            }
        }


        JSONObject serviceResult = new JSONObject();
        if (aiFrontFlag){
            JSONArray temperatureMinArr = new JSONArray();
            JSONArray temperatureMaxArr = new JSONArray();
            JSONObject tempObject;
            for (int i = 0; i < arrays.size(); i++) {
                tempObject = arrays.getJSONObject(i);
                Double max = tempObject.getDouble("max");
                Double min = tempObject.getDouble("min");
                if (i==0){
                    String infraredMergeUrl = tempObject.getStr("infrared_merge_url");
                    serviceResult.set("infrared_merge_url", infraredMergeUrl);
                }
                temperatureMinArr.add(min);
                temperatureMaxArr.add(max);
            }
            serviceResult.set("temperature_min", temperatureMinArr);
            serviceResult.set("temperature_max", temperatureMaxArr);
        }else{
            //调用aiservice进行检测和合并
            String url = aiagentUrl+aiagentInfraredUrl;
            JSONObject serviceObject = new JSONObject();
            serviceObject.set("image_url", pathArray);
            serviceObject.set("infrared_draw_thresh", threshold);
            serviceResult = aiAgentService.invokeHttp(url, serviceObject.toString());
        }

        JSONArray temperatureMin = serviceResult.getJSONArray("temperature_min");
        JSONArray temperatureMax = serviceResult.getJSONArray("temperature_max");
        String infraredMergeUrl = serviceResult.getStr("infrared_merge_url");

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

        try {
            for (int i = 0; i < detectionThresholdArray.size(); i++) {
                detectionObject = detectionThresholdArray.getJSONObject(i);
                String arrDetectionId = detectionObject.getStr("detection_id");
                if(arrDetectionId.indexOf(Detection.INFRARED)>-1){
                    String thresholdStr = detectionObject.getStr("threshold");

                    double lower = Double.NEGATIVE_INFINITY;
                    double upper;
                    //是否为高低双阈值
                    if (StrUtil.count(thresholdStr,"-")>0){
                        //双阈值 小于小的  大于大的
                        String[] split = thresholdStr.split("-");
                        lower = Double.parseDouble(split[0]);
                        upper = Double.parseDouble(split[1]);
                    }else{
                        //单阈值，小于即可
                        upper = Double.parseDouble(thresholdStr);
                    }

                    for (int p = 0; p < temperatureMax.size(); p++) {
                        //要比较的值
                        double compareValueMax = temperatureMax.getDouble(p);
                        double compareValueMin = temperatureMin.getDouble(p);
                        resultObject = arrays.getJSONObject(p);
                        resultObject.set("max", commonService.getFormatValue(compareValueMax));
                        //value取最大值
                        resultObject.set("value", commonService.getFormatValue(compareValueMax));
                        resultObject.set("min", commonService.getFormatValue(compareValueMin));
                        resultObject.set("status", DetectionResult.NORMAL);
                        resultObject.set("threshold", thresholdStr);
                        if(p == 0){
                            resultObject.set("infrared_merge_url", infraredMergeUrl);
                        }

                        //判断是否小于lower 或者大于 upper
                        //红外有最小值 匹配最小值小于
                        if (compareValueMin < lower || compareValueMax > upper){
                            if (compareValueMin < lower){
                                resultObject.set("value", commonService.getFormatValue(compareValueMin));
                            }

                            //如果红外都超过阈值，认为最大值有意义
                            if (compareValueMax > upper){
                                resultObject.set("value", commonService.getFormatValue(compareValueMax));
                            }
                            WarnInfo warnInfo = new WarnInfo();
                            warnInfo.setTaskLogId(dbTaskDetectionResult.getTaskLogId());
                            warnInfo.setPointName(dbTaskDetectionResult.getPointName());
                            warnInfo.setDetectionId(Detection.INFRARED);
                            warnInfo.setWarnTime(arrays.getJSONObject(p).getStr("timestamp"));

                            if(warnInfoDao.checkExist(warnInfo)){
                                warnInfoDao.update(warnInfo);
                            }else{
                                warnInfoDao.add(warnInfo);
                            }

                            //发送短信告知异常
                            detectionResultExceptionSendSms(taskId, pointName, roomId, Detection.INFRARED);

                            //告警数据保存服务
                            resultObject.set("status", DetectionResult.ABNORMAL);
                            arrays.set(p, resultObject);
                        }
                    }
                    taskDetectionResult.setInfrared(arrays.toString());
                    //防止出现并发问题，添加二次判断，如果数据不一致，则丢弃数据
                    //比对现在数据库中的数据与之前数据库中的数据
                    TaskDetectionResult nowDdbTaskDetectionResult = taskDetectionResultDao.getDetlByInstanceIdAndPointNameForUpdate(instanceId, pointName);
                    String infrared = nowDdbTaskDetectionResult.getInfrared();
                    if (!infrared.equals(dbTaskDetectionResult.getInfrared())){
                        throw new RuntimeException("检测项结果分析，出现并发问题，将丢弃该次数据");
                    }

                    taskDetectionResultDao.updateInfrared(taskDetectionResult);
                    break;
                }
            }
            dataSourceTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            dataSourceTransactionManager.rollback(transactionStatus);
            throw new RuntimeException(e.getMessage());
        } finally {
            dbTaskDetectionResult = null;
            resultObject = null;
            detectionObject = null;
        }
    }

    /**
     * 检测项结果异常，发送短信，相同任务相同点位仅发一次告警
     * @param taskId
     * @param pointName
     * @param roomId
     * @author kliu
     * @date 2022/4/29 10:03
     */
    @SuppressWarnings("AlibabaRemoveCommentedCode")
    private void detectionResultExceptionSendSms(long taskId, String pointName, long roomId, String detectionId){
        //任务、点位已经发送过短信，不再重复发送
        if (warnSmsDao.checkExist(taskId, pointName)) {
            return;
        }

        //根据roomid获取用户信息
        UserInfo userInfo = userInfoDao.getUserByRoomId(roomId);
        if (userInfo == null) {
            log.error("当前机房id【"+roomId+"】未配置用户信息，请检查");
            return;
        }
        String userTel = userInfo.getUserTel();
        String userName = userInfo.getUserName();
        long userId = userInfo.getUserId();

        DetectionInfo detectionInfo = detectionInfoDao.getDetlById(detectionId);
        String detectionName = detectionInfo.getDetectionName();

        WarnSms warnSms = new WarnSms();
        warnSms.setUserId(userId);
        warnSms.setPointName(pointName);
        warnSms.setTaskId(taskId);

        pointName = "机柜["+pointName+"]";
        detectionName = detectionName+"检测项";

        try {
            JSONObject object = new JSONObject();
            object.set("number", userTel);
            object.set("name", userName);
            object.set("treeName", pointName);
            object.set("skillName", detectionName);
            List<CloudEvent> cloudEventList = new ArrayList<>();
            cloudEventList.add(EventBuilder.builder()
                    .withId(IdUtil.simpleUUID())
                    .withSource(URI.create(source))
                    .withType("ui:Created:PostObject")
                    .withSubject(subject)
                    .withTime(new Date())
                    .withJsonStringData(object.toString())
                    .withAliyunEventBus(busName)
                    .build());
//            PutEventsResponse putEventsResponse = eventBridge.putEvents(cloudEventList);
//            log.info("短信发送结果"+new Gson().toJson(putEventsResponse));
            warnSmsDao.add(warnSms);
        }catch (Exception e){
            log.error("调用短信接口异常，异常信息"+e.getMessage());
        }
    }
}
