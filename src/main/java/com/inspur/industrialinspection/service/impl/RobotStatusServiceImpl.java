package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.ParaKey;
import com.inspur.code.TaskRunMode;
import com.inspur.code.TaskStatus;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.*;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kliu
 * @description 机器人状态
 * @date 2022/5/17 10:57
 */
@Service
public class RobotStatusServiceImpl implements RobotStatusService {

    private volatile static ConcurrentHashMap<Long, String> robotStatus = new ConcurrentHashMap();
    private volatile static ConcurrentHashMap<Long, String> robotPowerTime = new ConcurrentHashMap();

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private InspectTypeService inspectTypeService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private RobotPowerDao robotPowerDao;
    @Autowired
    private TaskInfoService taskInfoService;
    @Autowired
    private MobileMonitorConfigDao mobileMonitorConfigDao;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private SendSmsService sendSmsService;
    @Autowired
    private PhoneNoticeService phoneNoticeService;
    @Autowired
    private RobotWarnSmsDao robotWarnSmsDao;

    @Value("${sms.robot.exception.notifyPhoneNumber}")
    private String notifyPhoneNumber;
    @Value("${sms.robot.exception.betweenMinute}")
    private String betweenMinute;
    @Value("${sms.robot.exception.notNotifyRobot}")
    private String notNotifyRobot;
    @Value("${sms.robot.exception.lowerElectricQuantity}")
    private String lowerElectricQuantity;

    /**
     * 接收机器人状态服务
     *
     * @param robotStatusJson
     * @return void
     * @author kliu
     * @date 2022/6/1 20:23
     */
    @SuppressWarnings({"AlibabaUndefineMagicConstant", "AlibabaMethodTooLong"})
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveRobotStatus(String robotStatusJson) {
        JSONObject jsonObject = JSONUtil.parseObj(robotStatusJson);
        if (!jsonObject.containsKey(ParaKey.ROBOT_ID)) {
            throw new RuntimeException("机器人id不能为空");
        }

        Object abnormal = jsonObject.get("abnormal");
        if (abnormal == null || JSONNull.NULL.equals(abnormal)) {
            jsonObject.set("abnormal", new String[0]);
        }

        long robotId = jsonObject.getLong("robot_id");
        jsonObject.set("timestamp", System.currentTimeMillis());
        robotStatus.put(robotId, jsonObject.toString());

        //定位状态丢失，暂停所有任务执行
        if (jsonObject.getBool("relocalization")) {
            taskInfoDao.updateTaskStopExecute(robotId);
        }

        try {
            robotExceptionSendSms(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //机器人电量保存，隔一分钟保存一次，某一分钟内如果有多条，仅保留第一次的数据
        String currentTimeStr = DateUtil.now();
        String currentTimeMinuteStr = currentTimeStr.substring(11, 16);
        String day = currentTimeStr.substring(8, 10);
        if (robotPowerTime.containsKey(robotId)) {
            String cacheTimeMinuteStr = robotPowerTime.get(robotId);
            if (cacheTimeMinuteStr.equals(currentTimeMinuteStr)) {
                return;
            }
        }

        robotPowerTime.put(robotId, currentTimeMinuteStr);

        double electricQuantity = jsonObject.getDouble(ParaKey.ELECTRIC_QUANTITY);
        RobotPower robotPower = new RobotPower();
        robotPower.setRobotId(robotId);
        robotPower.setPower(electricQuantity);
        robotPower.setHeartTime(currentTimeMinuteStr);
        robotPower.setHeartDay(Integer.parseInt(day));
        if (robotPowerDao.checkExist(robotPower)) {
            robotPowerDao.update(robotPower);
        } else {
            robotPowerDao.save(robotPower);
        }

        robotPowerDao.deleteExpireData(robotId, currentTimeMinuteStr, Integer.parseInt(day));

        //此时添加一个额外的处理
        //当机器人上报没有任务时，对应的机器人的所有正在执行的任务都改为结束，结束时间为当前时间-仅限巡检任务
        //通过机器人上传的任务状态判断，无任务时，将机器人对应的云端正在执行的任务都改为结束状态，如存在多条任务，将任务的结束时间改为下一次任务的开始时间。有任务时，将不等于当前任务id的记录按照无任务时处理
        Boolean taskStatusFlag = jsonObject.getBool("task_status");
        if(taskStatusFlag == null){
            return;
        }
        if (!taskStatusFlag){
            List<TaskInstance> taskInstances = taskInstanceDao.getRunningTaskInstanceByRobotId(robotId);
            TaskInstance taskInstance;
            String nowTimeStr = DateUtil.now();
            int size = taskInstances.size();
            for (int i = 0; i < size; i++) {
                taskInstance = taskInstances.get(i);
                String startTime = taskInstance.getStartTime();
                String nextStartTime = nowTimeStr;
                int j = i + 1;
                if (j < size) {
                    nextStartTime = taskInstances.get(j).getStartTime();
                }
                if (System.currentTimeMillis() - DateUtil.parse(startTime).getTime() > 5 * 60 * 1000) {
                    taskInstance.setExecStatus(TaskStatus.END);
                    taskInstance.setEndTime(nextStartTime);
                    taskInstanceDao.update(taskInstance);
                }
            }
        } else {
            if (jsonObject.containsKey("task_id") && jsonObject.containsKey("task_type")) {
                Long taskId = jsonObject.getLong("task_id");
                String taskType = jsonObject.getStr("task_type");
                if (TaskRunMode.NORMAL.equals(taskType) || TaskRunMode.FAST.equals(taskType)) {
                    List<TaskInstance> taskInstances = taskInstanceDao.getRunningTaskInstanceByRobotId(robotId);
                    TaskInstance taskInstance;
                    String nowTimeStr = DateUtil.now();
                    int size = taskInstances.size();
                    for (int i = 0; i < taskInstances.size(); i++) {
                        taskInstance = taskInstances.get(i);
                        if (taskInstance.getInstanceId() == taskId) {
                            continue;
                        }
                        String startTime = taskInstance.getStartTime();
                        String nextStartTime = nowTimeStr;
                        int j = i + 1;
                        if (j < size) {
                            nextStartTime = taskInstances.get(j).getStartTime();
                        }
                        if (System.currentTimeMillis() - DateUtil.parse(startTime).getTime() > 5 * 60 * 1000) {
                            taskInstance.setExecStatus(TaskStatus.END);
                            taskInstance.setEndTime(nextStartTime);
                            taskInstanceDao.update(taskInstance);
                        }
                    }
                }
            }
        }
    }

    /**
     * 机器人异常发送短信
     * 定位异常，存在错误状态码等
     *
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/9/15 15:43
     */
    public void robotExceptionSendSms(JSONObject jsonObject) throws Exception {
        int hour = Integer.parseInt(DateUtil.date().toString("HH"));
        if (hour >= 21 || hour <= 7) {
            return;
        }
        long robotId = jsonObject.getLong("robot_id");
        if (notNotifyRobot.indexOf(robotId + "") > -1) {
            return;
        }
        //重定位
        Boolean relocalization = jsonObject.getBool("relocalization");
        //故障码
        JSONArray abnormal = jsonObject.getJSONArray("abnormal");
        //添加电量提示，电量低于10%，短信提醒
        Double electricQuantity = jsonObject.getDouble("electric_quantity");
        String message = "";

        if (relocalization || abnormal.size() > 0 || electricQuantity < Double.parseDouble(lowerElectricQuantity)) {
            //发短信通知机器人异常了
            if (relocalization) {
                message += "丢定位";
            }
            if (abnormal.size()>0){
                message += getAbnormalStr(abnormal);
            }

            if (electricQuantity < Double.parseDouble(lowerElectricQuantity)) {
                message += "电量低于" + lowerElectricQuantity;
            }


            //先判断能否发短信，依据配置的参数间隔时间，判断能否发短信
            String recentTime = robotWarnSmsDao.getRecentTime(robotId);
            if (!StringUtils.isEmpty(recentTime)) {
                long between = DateUtil.between(DateUtil.parse(recentTime, "yyyy-MM-dd HH:mm:ss"), DateUtil.date(), DateUnit.MINUTE);
                if (between < Long.parseLong(betweenMinute)) {
                    return;
                }
            }

            JSONObject paramObject = new JSONObject();
            paramObject.set("robotid", jsonObject.get("robot_id").toString());
            paramObject.set("warncontent", message);
            sendSmsService.sendSms(notifyPhoneNumber, "SMS_257678030", paramObject.toString());
            phoneNoticeService.phoneNotice(notifyPhoneNumber, "TTS_256355249", paramObject.toString());

            RobotWarnSms robotWarnSms = new RobotWarnSms();
            robotWarnSms.setRobotId(robotId);
            robotWarnSms.setSmsTime(DateUtil.now());
            if (robotWarnSmsDao.checkExist(robotWarnSms)) {
                robotWarnSmsDao.update(robotWarnSms);
            } else {
                robotWarnSmsDao.add(robotWarnSms);
            }

        }
    }

    /**
     * 获取机器人电量
     *
     * @param robotId
     * @return java.lang.String
     * @author kliu
     * @date 2022/6/1 20:24
     */
    @Override
    public String getRobotPower(long robotId) {
        if (robotStatus.containsKey(robotId)) {
            if (robotOnline(robotId)) {
                JSONObject jsonObject = getRobotStatus(robotId);
                if (jsonObject.containsKey(ParaKey.ELECTRIC_QUANTITY)) {
                    return jsonObject.getDouble(ParaKey.ELECTRIC_QUANTITY) + "";
                }
            }
        }
        return StrUtil.DASHED;
    }

    @Override
    public double getRobotPower(long roomId, long robotId) {
        if (robotId == 0) {
            robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        }
        String robotPower = getRobotPower(robotId);
        if ("-".equals(robotPower)) {
            return -1;
        }
        return Double.valueOf(robotPower);
    }

    /**
     * 机器人是否在线
     *
     * @param robotId
     * @return boolean
     * @author kliu
     * @date 2022/6/1 20:24
     */
    @Override
    public boolean robotOnline(long robotId) {
        if (robotStatus.containsKey(robotId)) {
            String robotStatus = getRobotStatus(robotId).getStr("robotStatus");
            //noinspection AlibabaUndefineMagicConstant
            if ("online".equals(robotStatus)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 获取机器人状态
     *
     * @param robotId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/8 8:51
     */
    @Override
    public JSONObject getRobotStatus(long robotId) {
        JSONObject jsonObject = new JSONObject();
        if (robotStatus.containsKey(robotId)) {
            jsonObject = JSONUtil.parseObj(robotStatus.get(robotId));
            Long timestamp = jsonObject.getLong("timestamp");
            long currentTimeMillis = System.currentTimeMillis();
            //1分钟
            long validMillis = 60000;
            jsonObject.set("lastHeartTime", DateUtil.date(timestamp).toString("yyyy-MM-dd HH:mm:ss"));
            if ((currentTimeMillis - timestamp) > validMillis) {
                jsonObject.set("robotStatus", "offline");
                //计算失联时间
                jsonObject.set("offlineMinute", commonService.getFormatValue((currentTimeMillis - timestamp) / 60000));
            } else {
                jsonObject.set("robotStatus", "online");
            }
        } else {
            //缓存中不存在数据，认为失联
            jsonObject.set("robotStatus", "offline");
            jsonObject.set("offlineMinute", "-");
        }
        return jsonObject;
    }

    private String getAbnormalStr(JSONArray abnormal){
        String abnormalStr = "";
        for (int i = 0; i < abnormal.size(); i++) {
            String str = abnormal.getStr(i);
            if ("E1x".equals(str)) {
                abnormalStr += "CAN总线通信故障,";
            } else if ("E11".equals(str)) {
                abnormalStr += "底盘速度反馈CAN通信故障,";
            } else if ("E12".equals(str)) {
                abnormalStr += "电池CAN总线通信故障,";
            } else if ("E2x".equals(str)) {
                abnormalStr += "485总线通信故障,";
            } else if ("E21".equals(str)) {
                abnormalStr += "舵机1通信故障,";
            } else if ("E22".equals(str)) {
                abnormalStr += "舵机2通信故障,";
            } else if ("E23".equals(str)) {
                abnormalStr += "舵机3通信故障,";
            } else if ("E24".equals(str)) {
                abnormalStr += "舵机4通信故障,";
            } else if ("E3x".equals(str)) {
                abnormalStr += "表示超声波通信故障,";
            } else if ("E31".equals(str)) {
                abnormalStr += "超声波组1通信故障,";
            } else if ("E32".equals(str)) {
                abnormalStr += "超声波组2通信故障,";
            } else if ("E33".equals(str)) {
                abnormalStr += "超声波组3通信故障,";
            } else if ("E34".equals(str)) {
                abnormalStr += "超声波组4通信故障,";
            } else if ("E4x".equals(str)) {
                abnormalStr += "激光雷达故障,";
            } else if ("E41".equals(str)) {
                abnormalStr += "激光雷达1故障,";
            } else if ("E42".equals(str)) {
                abnormalStr += "激光雷达2故障,";
            } else if ("E43".equals(str)) {
                abnormalStr += "激光雷达3故障,";
            } else if ("E44".equals(str)) {
                abnormalStr += "激光雷达4故障,";
            } else if ("E5".equals(str)) {
                abnormalStr += "IMU故障,";
            } else if ("E6x".equals(str)) {
                abnormalStr += "电机故障,";
            } else if ("E61".equals(str)) {
                abnormalStr += "左前轮电机故障,";
            } else if ("E62".equals(str)) {
                abnormalStr += "左后轮电机故障,";
            } else if ("E63".equals(str)) {
                abnormalStr += "右前轮电机故障,";
            } else if ("E64".equals(str)) {
                abnormalStr += "右后轮电机故障,";
            } else {
                abnormalStr += str;
            }
        }

        return abnormalStr;
    }

    /**
     * 获取机器人状态信息-增加相关任务信息-给手机监控用
     *
     * @param robotId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/4 10:41
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public JSONObject getRobotStatusWithTask(long robotId) {
        JSONObject jsonObject = getRobotStatus(robotId);
        String robotStatus = jsonObject.getStr("robotStatus");
        //noinspection AlibabaUndefineMagicConstant
        if ("offline".equals(robotStatus)) {
            jsonObject.set("task_status", "-");
            jsonObject.set("relocalization", "-");
            jsonObject.set("relocalization_status", "-");
            jsonObject.set("charging", "-");
            jsonObject.set("emergency_stop", "-");
            jsonObject.set("electric_quantity", "-");
            jsonObject.set("is_standby", "-");
            jsonObject.set("abnormal", "-");
            jsonObject.set("updating", "-");
            jsonObject.set("temperature", "-");
            jsonObject.set("bumper", "-");
            jsonObject.set("abnormal", "-");
        } else {
            //机器人在线时，将所有值改为字符串返回给前端
            JSONArray abnormal = jsonObject.getJSONArray("abnormal");
            String abnormalStr = getAbnormalStr(abnormal);

            if (StringUtils.isEmpty(abnormalStr)) {
                abnormalStr = "无";
            }

            jsonObject.set("abnormal", abnormalStr);

            Boolean emergencyStop = jsonObject.getBool("emergency_stop");
            if (emergencyStop) {
                jsonObject.set("emergency_stop", "触发");
            } else {
                jsonObject.set("emergency_stop", "未触发");
            }
            Boolean charging = jsonObject.getBool("charging");
            if (charging) {
                jsonObject.set("charging", "充电中");
            } else {
                jsonObject.set("charging", "未在充电");
            }
            Boolean relocalizationStatus = jsonObject.getBool("relocalization_status");
            if (relocalizationStatus) {
                jsonObject.set("relocalization_status", "重定位中");
            } else {
                jsonObject.set("relocalization_status", "非重定位状态");
            }
            Boolean relocalization = jsonObject.getBool("relocalization");
            if (relocalization) {
                jsonObject.set("relocalization", "定位异常");
            } else {
                jsonObject.set("relocalization", "定位正常");
            }
            Boolean bumper = jsonObject.getBool("bumper");
            if (bumper) {
                jsonObject.set("bumper", "触发防撞");
            } else {
                jsonObject.set("bumper", "未触发防撞");
            }
            Boolean updating = jsonObject.getBool("updating");
            if (updating) {
                jsonObject.set("updating", "OTA正在升级");
            } else {
                jsonObject.set("updating", "OTA未升级");
            }
            Boolean taskStatus = jsonObject.getBool("task_status");
            if (taskStatus) {
                jsonObject.set("task_status", "是");
            } else {
                jsonObject.set("task_status", "否");
            }
        }

        //添加当前正在运行的任务
        TaskInstance taskInstance = taskInstanceDao.getLatestTaskInstanceByRobotId(robotId);
        if (taskInstance == null) {
            jsonObject.set("inspectTypeName", "-");
            jsonObject.set("startTime", "-");
            jsonObject.set("execStatus", "-");
            jsonObject.set("endTime", "-");
            return jsonObject;
        }
        long inspectTypeId = taskInstance.getInspectTypeId();
        String execStatus = taskInstance.getExecStatus();
        String startTime = taskInstance.getStartTime();
        String endTime = taskInstance.getEndTime();
        long roomId = taskInstance.getRoomId();

        InspectType inspectType = inspectTypeService.getDetlById(roomId, inspectTypeId);
        String inspectTypeName = "-";
        if (inspectType != null) {
            inspectTypeName = inspectType.getInspectTypeName();
        }

        jsonObject.set("inspectTypeName", inspectTypeName);
        jsonObject.set("startTime", startTime);
        jsonObject.set("execStatus", execStatus);
        jsonObject.set("endTime", endTime == null ? "" : endTime);

        //计算当前机器人下一次任务执行时间等信息
        PageBean list = taskInfoService.listWithoutPark(roomId, robotId, 1000, 1);
        List<TaskInfo> taskInfos = list.getContentList();
        String nowTimeStr = DateUtil.now();
        String nowHourMinuteStr = nowTimeStr.substring(0, 16);
        String nowDayStr = nowTimeStr.substring(0, 10);

        String nextExecTime = "-";
        String nextInspectTypeName = "-";

        long timeDifference = 1000000000000L;

        long nowTime = DateUtil.parse(nowHourMinuteStr, "yyyy-MM-dd HH:mm").getTime();
        for (TaskInfo taskInfo : taskInfos) {
            String inUse = taskInfo.getInUse();
            if ("1".equals(inUse)) {
                continue;
            }
            String execTime = taskInfo.getExecTime();
            String[] split = execTime.split(",");
            for (String s : split) {
                long dbExecTime = DateUtil.parse(nowDayStr + " " + s).getTime();
                if (dbExecTime > nowTime) {
                    if ((dbExecTime - nowTime) < timeDifference) {
                        timeDifference = dbExecTime - nowTime;
                        nextExecTime = s;
                        nextInspectTypeName = taskInfo.getInspectTypeName();
                    }
                }
            }
        }

        if (StringUtils.isEmpty(nextExecTime)) {
            nextExecTime = "-";
        }

        if (StringUtils.isEmpty(nextInspectTypeName)) {
            nextInspectTypeName = "-";
        }

        jsonObject.set("nextExecTime", nextExecTime);
        jsonObject.set("nextInspectTypeName", nextInspectTypeName);

        return jsonObject;
    }

    /**
     * 获取机器人电量变化曲线
     *
     * @param robotId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/8/9 9:48
     */
    @Override
    public JSONObject getRobotPowerChangeLine(long robotId) {
        JSONArray xArr = new JSONArray();
        JSONArray yArr = new JSONArray();
        List<RobotPower> list = robotPowerDao.list(robotId);
        String heartTime;
        double power;
        for (RobotPower robotPower : list) {
            heartTime = robotPower.getHeartTime();
            power = robotPower.getPower();
            xArr.add(heartTime);
            yArr.add(power);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("xArr", xArr);
        jsonObject.set("yArr", yArr);
        return jsonObject;
    }

    @Override
    public List getRobotServiceUrl() {
        return mobileMonitorConfigDao.list();
    }

    @Override
    public JSONObject getRecentPicture(long robotId) {
        TaskDetectionResult detectionResult = null;
        List<TaskDetectionResult> lists = taskDetectionResultDao.getRecentInfraredPic(robotId);
        if (lists.size() > 0) {
            detectionResult = lists.get(0);
        }
        String infrared = detectionResult.getInfrared();

        JSONArray infraredArr = new JSONArray(infrared);
        JSONObject tempObject;
        String infraredImgUrl = "";
        String infraredTime = "";
        String alarmLightImgUrl = "";
        String alarmLightTime = "";
        String frontImgUrl = "";
        String frontTime = "";
        if (detectionResult != null) {
            infraredTime = detectionResult.getUpdateTime();
        }
        for (int i = 0; i < infraredArr.size(); i++) {
            tempObject = infraredArr.getJSONObject(i);
            if (tempObject.containsKey("infrared_merge_url")) {
                String infraredMergeUrl = tempObject.getStr("infrared_merge_url");
                infraredImgUrl = commonService.url2Https(infraredMergeUrl);
            }
            String timestamp = tempObject.getStr("timestamp");
            if (timestamp.compareTo(infraredTime) > 0) {
                infraredTime = timestamp;
            }
        }
        detectionResult = null;
        lists = taskDetectionResultDao.getRecentAlarmLightPic(robotId);
        if (lists.size() > 0) {
            detectionResult = lists.get(0);
        }
        String alarmLight = detectionResult.getAlarmLight();
        JSONObject alarmLightObject = new JSONObject(alarmLight);
        if (alarmLightObject.containsKey("alarm_light_merge_url")) {
            String alarmLightMergeUrl = alarmLightObject.getStr("alarm_light_merge_url");

            alarmLightImgUrl = commonService.url2Https(alarmLightMergeUrl);
        } else {
            alarmLightImgUrl = alarmLightObject.getJSONArray("path").getStr(0);

        }
        alarmLightTime = alarmLightObject.getStr("timestamp");


        JSONObject jsonObject = new JSONObject();
        jsonObject.set("infraredImgUrl", infraredImgUrl);
        jsonObject.set("infraredTime", infraredTime);
        jsonObject.set("alarmLightImgUrl", alarmLightImgUrl);
        jsonObject.set("alarmLightTime", alarmLightTime);
        jsonObject.set("frontImgUrl", frontImgUrl);
        jsonObject.set("frontTime", frontTime);

        return jsonObject;
    }


    @Override
    public void setRobotTaskStatus(long robotId, boolean status) {
        if (!robotOnline(robotId)) {
            return;
        }
        JSONObject robotStatusObj = getRobotStatus(robotId);
        robotStatusObj.set("task_status", status);
        robotStatus.put(robotId, robotStatusObj.toString());
    }

    @Override
    public void pileReturnFailure(long robotId, String message) throws Exception {

        JSONObject paramObject = new JSONObject();
        paramObject.set("robotid", robotId+"");
        paramObject.set("warncontent", message);
        ////回桩失败，发短信
        sendSmsService.sendSms(notifyPhoneNumber, "SMS_257678030", paramObject.toString());
        phoneNoticeService.phoneNotice(notifyPhoneNumber, "TTS_256355249", paramObject.toString());
    }
}
