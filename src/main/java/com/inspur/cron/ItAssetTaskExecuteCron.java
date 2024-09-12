package com.inspur.cron;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.TaskStatus;
import com.inspur.industrialinspection.dao.ItAssetTaskInfoDao;
import com.inspur.industrialinspection.dao.ItAssetTaskInstanceDao;
import com.inspur.industrialinspection.dao.PointInfoDao;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.RobotParamService;
import com.inspur.industrialinspection.service.RobotStatusService;
import com.inspur.industrialinspection.service.WorkDayService;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产盘点任务执行
 *
 * @author kliu
 * @date 2022/7/26 16:26
 */
@Component
@Slf4j
public class ItAssetTaskExecuteCron {
    @Autowired
    private ItAssetTaskInfoDao itAssetTaskInfoDao;
    @Autowired
    private WorkDayService workDayService;
    @Autowired
    private ItAssetTaskInstanceDao itAssetTaskInstanceDao;
    @Autowired
    private RobotStatusService robotStatusService;
    @Autowired
    private RobotParamService robotParamService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private PointInfoDao pointInfoDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private TaskInstanceDao taskInstanceDao;

    private static String EXEC_TYPE_REGULAR = "regular";
    private static String CYCLE_TYPE_EVERYDAY = "everyday";
    private static String CYCLE_TYPE_WORKDAY = "workday";
    private static String CYCLE_TYPE_WEEK = "week";
    private static String CYCLE_TYPE_TWOWEEK = "twoweek";
    private static String CYCLE_TYPE_MONTH = "month";




    /**
     * 任务执行，每分钟匹配下需要执行的任务
     *
     * @author kliu
     * @date 2022/5/24 17:52
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void execute() throws IOException {
        TransactionStatus transactionStatus = null;
        String currentTimeStr = DateUtil.now();
        String currentTimeMinuteStr = currentTimeStr.substring(0, 16);
        //获取待执行的任务
        List<ItAssetTaskInfo> itAssetTaskInfos = itAssetTaskInfoDao.pendingExecutionTask();
        ItAssetTaskInstance itAssetTaskInstance;
        for (ItAssetTaskInfo itAssetTaskInfo : itAssetTaskInfos) {
            String nextExecTime = itAssetTaskInfo.getNextExecTime();
            if (nextExecTime.length() == 19) {
                nextExecTime = nextExecTime.substring(0, 16);
            }
            String execType = itAssetTaskInfo.getExecType();
            //判断是否是周期任务，对于周期性任务中没有下次执行日期的先计算一次
            if (StringUtils.isEmpty(nextExecTime)) {
                //周期性任务需要先计算下一次执行时间补充数据后再执行
                if ("cycle".equals(execType)) {
                    nextExecTime = getNextExecTime(itAssetTaskInfo, currentTimeMinuteStr);
                    itAssetTaskInfo.setNextExecTime(nextExecTime);
                }
            }
            //时间与下次执行时间一致则执行
            if (currentTimeMinuteStr.equals(nextExecTime)) {
                //任务执行完成后，保存下次执行日期
                //周期性任务继续更新下一次，非周期性任务置空，下次不再执行
                if ("cycle".equals(execType)) {
                    nextExecTime = getNextExecTime(itAssetTaskInfo, currentTimeMinuteStr);
                } else {
                    nextExecTime = "";
                }

                itAssetTaskInfo.setNextExecTime(nextExecTime);

                transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

                itAssetTaskInstance = new ItAssetTaskInstance();
                long instanceId = 0;

                try {
                    //更新下次执行时间
                    itAssetTaskInfoDao.update(itAssetTaskInfo);
                    //保存资产盘点任务记录
                    itAssetTaskInstance.setTaskId(itAssetTaskInfo.getId());
                    itAssetTaskInstance.setStartTime(currentTimeMinuteStr);
                    itAssetTaskInstance.setExecStatus(TaskStatus.RUNNING);
                    instanceId = itAssetTaskInstanceDao.addAndReturnId(itAssetTaskInstance);
                    //显示提交事务，防止查询的时候查询不到
                    String taskExecuteStr = getTaskExecuteStr(itAssetTaskInfo, currentTimeMinuteStr, instanceId);
                    dataSourceTransactionManager.commit(transactionStatus);
                    //各种保存及生成任务字符串都成功之后再下发任务
                    mqttPushClient.publish("industrial_robot_issued/" + itAssetTaskInfo.getRobotId(), taskExecuteStr);
                } catch (Exception e) {
                    if (transactionStatus != null) {
                        dataSourceTransactionManager.rollback(transactionStatus);
                    }
                    throw new RuntimeException(e.getMessage());
                }


            }
        }
        itAssetTaskInfos = null;
    }

    /**
     * 计算下一次执行时间
     *
     * @param itAssetTaskInfo
     * @param currentTimeMinuteStr
     * @return java.lang.String
     * @author kliu
     * @date 2022/7/27 15:53
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    private String getNextExecTime(ItAssetTaskInfo itAssetTaskInfo, String currentTimeMinuteStr) {
        String execType = itAssetTaskInfo.getExecType();
        if (EXEC_TYPE_REGULAR.equals(execType)) {
            throw new RuntimeException("定期任务不支持计算下一次执行时间");
        }

        String nextExecTime = itAssetTaskInfo.getNextExecTime();

        String currentHourMinute = DateUtil.parse(currentTimeMinuteStr, "yyyy-MM-dd HH:mm").toString("HH:mm");

        Date currentDate = DateUtil.date();

        String cycleType = itAssetTaskInfo.getCycleType();
        String execTime = itAssetTaskInfo.getExecTime();
        long cycleValue = itAssetTaskInfo.getCycleValue();

        if (StringUtils.isEmpty(nextExecTime)) {
            //从当天开始计算
            //workday:工作日 everyday:每天 week:每周 twoweek:每两周 month:每月
            if (CYCLE_TYPE_EVERYDAY.equals(cycleType)) {
                if (currentHourMinute.compareTo(execTime) > 0) {
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
                //计算大于当天的周期 取下一天
                nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
            } else if (CYCLE_TYPE_WORKDAY.equals(cycleType)) {
                //判断当天是否为工作日
                if (currentHourMinute.compareTo(execTime) > 0) {
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
                while (true) {
                    boolean isWorkDay = workDayService.isWorkDay(DateUtil.format(currentDate, "yyyy-MM-dd"));
                    if (isWorkDay) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            } else if (CYCLE_TYPE_WEEK.equals(cycleType)) {
                if (currentHourMinute.compareTo(execTime) > 0) {
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
                while (true) {
                    int dayOfWeek = DateUtil.dayOfWeek(currentDate);
                    dayOfWeek = dayOfWeek - 1;
                    if (dayOfWeek == 0) {
                        dayOfWeek = 7;
                    }

                    if (dayOfWeek == cycleValue) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            } else if (CYCLE_TYPE_TWOWEEK.equals(cycleType)) {
                if (currentHourMinute.compareTo(execTime) > 0) {
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
                while (true) {
                    int dayOfWeek = DateUtil.dayOfWeek(currentDate);
                    dayOfWeek = dayOfWeek - 1;
                    if (dayOfWeek == 0) {
                        dayOfWeek = 7;
                    }

                    if (dayOfWeek == cycleValue) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            } else if (CYCLE_TYPE_MONTH.equals(cycleType)) {
                if (currentHourMinute.compareTo(execTime) > 0) {
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
                while (true) {
                    int dayOfMonth = DateUtil.dayOfMonth(currentDate);
                    if (dayOfMonth == cycleValue) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            }
        } else {
            //依据下次执行时间再取下一次执行时间
            //workday:工作日 everyday:每天 week:每周 twoweek:每两周 month:每月
            //对于有下次执行时间的数据，则直接取下一天，只有当天的任务执行完成之后，才会计算下一次的时间
            currentDate = DateUtil.offsetDay(DateUtil.parse(nextExecTime), 1);
            if (CYCLE_TYPE_EVERYDAY.equals(cycleType)) {
                //计算大于下次执行时间的周期 取下一天
                nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
            } else if (CYCLE_TYPE_WORKDAY.equals(cycleType)) {
                //判断当天是是否为工作日
                while (true) {
                    boolean isWorkDay = workDayService.isWorkDay(DateUtil.format(currentDate, "yyyy-MM-dd"));
                    if (isWorkDay) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            } else if (CYCLE_TYPE_WEEK.equals(cycleType)) {
                //最开始已经加了1天
                currentDate = DateUtil.offsetDay(currentDate, 6);
                while (true) {
                    int dayOfWeek = DateUtil.dayOfWeek(currentDate);
                    dayOfWeek = dayOfWeek - 1;
                    if (dayOfWeek == 0) {
                        dayOfWeek = 7;
                    }

                    if (dayOfWeek == cycleValue) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            } else if (CYCLE_TYPE_TWOWEEK.equals(cycleType)) {
                //最开始已经加了1天
                currentDate = DateUtil.offsetDay(currentDate, 13);
                while (true) {
                    int dayOfWeek = DateUtil.dayOfWeek(currentDate);
                    dayOfWeek = dayOfWeek - 1;
                    if (dayOfWeek == 0) {
                        dayOfWeek = 7;
                    }

                    if (dayOfWeek == cycleValue) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            } else if (CYCLE_TYPE_MONTH.equals(cycleType)) {
                //最开始已经加了1天
                currentDate = DateUtil.offsetDay(currentDate, 27);
                while (true) {
                    int dayOfMonth = DateUtil.dayOfMonth(currentDate);
                    if (dayOfMonth == cycleValue) {
                        nextExecTime = DateUtil.format(currentDate, "yyyy-MM-dd") + " " + execTime;
                        break;
                    }
                    currentDate = DateUtil.offsetDay(currentDate, 1);
                }
            }
        }
        return nextExecTime;
    }

    /**
     * 获取任务执行json字符串
     *
     * @param itAssetTaskInfo
     * @param timeStr
     * @param instanceId
     * @return java.lang.String
     * @author kliu
     * @date 2022/8/5 15:29
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    public String getTaskExecuteStr(ItAssetTaskInfo itAssetTaskInfo, String timeStr, long instanceId) throws IOException {
        long robotId = itAssetTaskInfo.getRobotId();
        //校验机器人是否离线，如机器人已离线，则不允许发送任务
        if (!robotStatusService.robotOnline(robotId)) {
            throw new RuntimeException("任务执行失败，当前机器人已离线");
        }

        String robotPower = robotStatusService.getRobotPower(robotId);
        JSONObject robotParamObject = robotParamService.getRobotParam(robotId);
        Double taskMinimumPower = robotParamObject.getDouble("task_minimum_power");
        if (Double.valueOf(robotPower) < taskMinimumPower) {
            throw new RuntimeException("任务执行失败，当前机器人电量低于阈值");
        }

        JSONObject robotStatusObj = robotStatusService.getRobotStatus(robotId);

        String taskStatusKey = "task_status";
        if (robotStatusObj.containsKey(taskStatusKey)) {
            if (robotStatusObj.getBool(taskStatusKey)) {
                throw new RuntimeException("任务执行失败，当前机器人正在执行任务");
            }
        }

        long roomId = itAssetTaskInfo.getRoomId();

        JSONObject orientation;
        JSONObject position;
        JSONObject param;
        JSONObject jsonObject;
        JSONArray detectionArr;
        JSONArray rowPointActionArray = new JSONArray();

        List<PointInfo> pointInfos = pointInfoDao.list(roomId);
        PointInfo pointInfo;
        String pointName;
        for (int i = 0; i < pointInfos.size(); i++) {
            pointInfo = pointInfos.get(i);
            pointName = pointInfo.getPointName();
            if ("ADMD".equals(pointName)){
                continue;
            }
            //测试机柜名称中带有前的机柜，即仅巡检正面
            if (!pointName.endsWith("前")){
                continue;
            }

            if (i==0){

            }
            orientation = new JSONObject();
            position = new JSONObject();
            param = new JSONObject();
            detectionArr = new JSONArray();

            orientation.set("w", pointInfo.getOrientationW());
            orientation.set("x", pointInfo.getOrientationX());
            orientation.set("y", pointInfo.getOrientationY());
            orientation.set("z", pointInfo.getOrientationZ());
            position.set("x", pointInfo.getLocationX());
            position.set("y", pointInfo.getLocationY());
            position.set("z", pointInfo.getLocationZ());
            param = new JSONObject();
            param.set("orientation", orientation);
            param.set("position", position);

            jsonObject = new JSONObject();
            jsonObject.set("action", "navigation");
            jsonObject.set("param", param);
            detectionArr.add(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.set("action", "qr_code");
            detectionArr.add(jsonObject);

            Map pointActionMap = new LinkedHashMap();
            pointActionMap.put("point_name", pointInfo.getPointName());
            pointActionMap.put("detection_item", detectionArr);
            rowPointActionArray.add(new JSONObject(pointActionMap));
        }

        addBackChargingPile(rowPointActionArray);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(rowPointActionArray);

        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", timeStr);
        map.put("task_id", instanceId);
        map.put("robot_id", robotId);
        map.put("run_mode", "it_asset_work");
        map.put("point_action_list", jsonArray);

        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        String json = new JSONObject(returnMap).toString();
        log.info("资产盘点任务json:"+json);
        String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
        rowPointActionArray = null;
        returnMap = null;
        orientation = null;
        position = null;
        param = null;
        jsonObject = null;
        detectionArr = null;
        return issuedStr;
    }

    /**
     * 返回充电桩改为任务触发，非自动触发
     *
     * @param rowPointActionArray
     * @author kliu
     * @date 2022/5/24 16:29
     */
    private void addBackChargingPile(JSONArray rowPointActionArray) {
        JSONObject rowPointActionObject = rowPointActionArray.getJSONObject(rowPointActionArray.size() - 1);
        JSONArray detectionArr = rowPointActionObject.getJSONArray("detection_item");
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("action", "back_charging_pile");
        detectionArr.add(jsonObject);
    }
}
