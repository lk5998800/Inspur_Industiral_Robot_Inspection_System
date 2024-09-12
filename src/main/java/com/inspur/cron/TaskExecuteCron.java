package com.inspur.cron;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.code.Detection;
import com.inspur.code.TaskRunMode;
import com.inspur.code.TaskStatus;
import com.inspur.gating.TreeNode;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.*;
import com.inspur.mqtt.MqttPushClient;
import com.inspur.page.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.util.*;

/**
 * 任务执行定时任务
 *
 * @author: kliu
 * @date: 2022/4/20 15:51
 */
@Component
@Slf4j
public class TaskExecuteCron {
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private PointInfoDao pointInfoDao;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private InspectTypeService inspectTypeService;
    @Autowired
    private DetectionCombinationService detectionCombinationService;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private RobotStatusService robotStatusService;
    @Autowired
    private RobotParamService robotParamService;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private GatingParaDao gatingParaDao;
    @Autowired
    private MechanicalArmParaDao mechanicalArmParaDao;
    @Autowired
    private PointTreeNodeService pointTreeNodeService;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private FireExtinguisherParaDao fireExtinguisherParaDao;


    /**
     * @author: LiTan
     * @description: 判断电量知否够暂停任务执行
     * @date: 2022-10-24 16:14:19
     */
    //@Scheduled(cron = "0 0/1 * * * ? ")
    public void suspendTaskExecute() throws IOException {
        //1.获取所有任务
        List<TaskInfo> infos = taskInfoDao.getTasks();
        JSONArray oldJsonArray = new JSONArray();
        JSONArray oldJsonDetlArray = new JSONArray();
        JSONArray tempArray = new JSONArray();
        JSONObject taskObject = new JSONObject();
        TransactionStatus transactionStatus = null;
        for (TaskInfo info : infos) {
            //2.判断机器人下是否存在暂停任务
            long roomId = info.getRoomId();
            TaskInstance suspendTask = taskInstanceDao.getSuspendTask(info.getRobotId(), TaskStatus.SUSPEND);
            if (suspendTask != null) {
                //3.判断机器人是否无任务
                TaskInstance task = taskInstanceDao.getSuspendTask(info.getRobotId(), TaskStatus.RUNNING);
                if (task == null) {
                    //4.根据task_detection_result 查看当前任务执行到的点位。计算电量是否够任务执行
                    TaskDetectionResult detectionResult = taskDetectionResultDao.getByInstanceIdNewestResult(suspendTask.getInstanceId());
                    //比较
                    if (detectionResult != null) {
                        String currentPointName = detectionResult.getPointName();
                        taskObject = JSONUtil.parseObj(commonService.gzipUnCompress(suspendTask.getTaskJsonCompress()));
                        oldJsonArray = taskObject.getJSONObject("data").getJSONArray("point_action_list");

                        boolean breakFor = false;
                        for (int j = 0; j < oldJsonArray.size(); j++) {
                            oldJsonDetlArray = oldJsonArray.getJSONArray(j);
                            if (breakFor) {
                                break;
                            }
                            for (int i = 0; i < oldJsonDetlArray.size(); i++) {
                                String pointName = oldJsonDetlArray.getJSONObject(i).getStr("point_name");
                                if (pointName.equals(currentPointName)) {
                                    breakFor = true;
                                    break;
                                } else {
                                    oldJsonDetlArray.remove(i);
                                    i--;
                                }
                            }
                        }

                        //多个巡检顺序的时候，会生成多个数组，此时将空数组过滤掉，如果不过滤可能会有问题，如robotmanage的解析
                        for (int j = 0; j < oldJsonArray.size(); j++) {
                            oldJsonDetlArray = oldJsonArray.getJSONArray(j);
                            if (oldJsonDetlArray.size() == 0) {
                                oldJsonArray.remove(j);
                                j--;
                            }
                        }

                        oldJsonDetlArray = oldJsonArray.getJSONArray(0);

                        DetectionInfo gatingDetectionInfo = new DetectionInfo();
                        gatingDetectionInfo.setDetectionId("gating");
                        gatingDetectionInfo.setRobotDetection("gating");

                        //重新计算充电桩到该点位currentPointName的路径，并添加进路径规划
                        //此时仅添加到点位的即可，不用管点位是否配置相关的开门指令等信息
                        //因为当前点位会重复执行一次检测，当前点位可能配置了对门的操作，也可能没配置，这些都是依据缓存中的数据而确定的，不需要重复添加
                        addGatingDetectionForFirstPointName(roomId, currentPointName, tempArray);
                    }

                    for (int i = 0; i < tempArray.size(); i++) {
                        oldJsonDetlArray.add(i, tempArray.get(i));
                    }

                    //重新计算剩余继续执行的点位
                    int remainderPointCount = 0;
                    for (int j = 0; j < oldJsonArray.size(); j++) {
                        oldJsonDetlArray = oldJsonArray.getJSONArray(j);
                        remainderPointCount += oldJsonDetlArray.size();
                    }

                    //5.判断机器人是否在线，电量够，下发任务到机器人
                    if (!robotStatusService.robotOnline(info.getRobotId())) {
                        throw new RuntimeException("任务执行失败，当前机器人【" + info.getRobotId() + "】已离线");
                    }
                    String robotPower = robotStatusService.getRobotPower(info.getRobotId());
                    JSONObject robotParamObject = robotParamService.getRobotParam(info.getRobotId());
                    Double taskMinimumPower = robotParamObject.getDouble("task_minimum_power");
                    if (Double.parseDouble(robotPower) > taskMinimumPower + (double) (remainderPointCount / 5) + 1) {
                        //压缩json，下发到机器人
                        String gzipCompress = commonService.gzipCompress(taskObject.toString());
                        log.info("suspendTaskExecute定时任务下发：" + taskObject.toString());
                        //更改任务状态为running
                        transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

                        try {
                            suspendTask.setExecStatus(TaskStatus.RUNNING);
                            suspendTask.setEndTime(null);
                            taskInstanceDao.update(suspendTask);
                            dataSourceTransactionManager.commit(transactionStatus);
                            transactionStatus = null;
                        } catch (TransactionException e) {
                            if (transactionStatus != null) {
                                dataSourceTransactionManager.rollback(transactionStatus);
                            }
                        }
                        mqttPushClient.publish("industrial_robot_issued/" + info.getRobotId(), gzipCompress);
                    }
                }
            }
        }
    }


    /**
     * 任务执行，每分钟匹配下需要执行的任务
     *
     * @author kliu
     * @date 2022/5/24 17:52
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void taskExecute() {
        String now = DateUtil.now();
        String hourminute = now.substring(11, 16);
        List<TaskInfo> taskInfos = taskInfoDao.list();
        for (TaskInfo taskInfo : taskInfos) {
            //如果存在暂停任务，定时任务不下发
            TaskInstance taskInstances = taskInstanceDao.getSuspendTask(taskInfo.getRobotId(), TaskStatus.SUSPEND);
            if (taskInstances != null) {
                return;
            }
            String execTime = taskInfo.getExecTime();
            String[] splitArr = execTime.split(",");
            for (String s : splitArr) {
                if (s.equals(hourminute)) {
                    //组装任务，下发任务
                    try {
                        now = DateUtil.now();
                        executeTask(taskInfo, now);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        taskInfos = null;
    }

    /**
     * 保存任务实例
     *
     * @param taskInfo
     * @param timeStr
     * @return long
     * @author kliu
     * @date 2022/4/27 18:38
     */
    private long saveTaskInstanceAndReturnInstanceId(TaskInfo taskInfo, String timeStr) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskId(taskInfo.getTaskId());
        taskInstance.setStartTime(timeStr);
        taskInstance.setExecStatus(TaskStatus.RUNNING);
        return taskInstanceDao.addAndReturnId(taskInstance);
    }

    /**
     * 获取位姿
     *
     * @param pointInfos
     * @param pointName
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/4/27 18:41
     */
    public JSONObject getPosture(List<PointInfo> pointInfos, String pointName) {
        JSONObject orientation = new JSONObject();
        JSONObject position = new JSONObject();
        JSONObject param = new JSONObject();

        boolean existData = false;
        for (PointInfo pointInfo : pointInfos) {
            if (pointInfo.getPointName().equals(pointName)) {
                existData = true;
                orientation.set("w", pointInfo.getOrientationW());
                orientation.set("x", pointInfo.getOrientationX());
                orientation.set("y", pointInfo.getOrientationY());
                orientation.set("z", pointInfo.getOrientationZ());
                position.set("x", pointInfo.getLocationX());
                position.set("y", pointInfo.getLocationY());
                position.set("z", pointInfo.getLocationZ());
                param.set("orientation", orientation);
                param.set("position", position);
                break;
            }
        }
        if (!existData) {
            throw new RuntimeException("巡检点位【" + pointName + "】对应的位姿不存在，请检查");
        }
        orientation = null;
        position = null;
        return param;
    }

    /**
     * 生成机柜数据
     *
     * @author kliu
     * @date 2022/5/18 17:56
     */
    private List getCabinetList(JSONObject roomParamObject, long inspectTypeId) {
        JSONArray inspectTypeArray = roomParamObject.getJSONObject("inspect_setting").getJSONArray("inspect_type");

        JSONObject inspectTypeObject;
        for (int i = 0; i < inspectTypeArray.size(); i++) {
            inspectTypeObject = inspectTypeArray.getJSONObject(i);
            if (inspectTypeId == inspectTypeObject.getLong("inspect_type_id")) {
                JSONArray inspectOrderArray = inspectTypeObject.getJSONArray("inspect_order");
                int cabinetCount = 0;
                for (int j = 0; j < inspectOrderArray.size(); j++) {
                    cabinetCount += inspectOrderArray.getJSONArray(j).size();
                }
                if (cabinetCount == 0) {
                    throw new RuntimeException("任务执行失败，当前巡检任务未配置巡检顺序");
                }
                return inspectOrderArray.toList(Object.class);
            }
        }
        return null;
    }

    /**
     * 获取升降杆高度
     *
     * @param roomParamObject
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/5/24 17:52
     * @update 20221110 升降杆不再配置，改为定值
     */
    private JSONArray getInfraredHeightArray(JSONObject roomParamObject) {
        JSONArray infraredHeightArray = new JSONArray();
        infraredHeightArray.add(0);
        infraredHeightArray.add(60);
        infraredHeightArray.add(120);
//        JSONArray detectionParaArray = roomParamObject.getJSONArray("detection_para");
//        JSONArray infraredHeightListArray;
//
//        for (int i = 0; i < detectionParaArray.size(); i++) {
//            infraredHeightListArray = detectionParaArray.getJSONObject(i).getJSONArray("infrared_height_list");
//            for (int j = 0; j < infraredHeightListArray.size(); j++) {
//                int height = infraredHeightListArray.getInt(j);
//                boolean exists = false;
//                for (int p = 0; p < infraredHeightArray.size(); p++) {
//                    int tempHeight = infraredHeightArray.getInt(p);
//                    if(height == tempHeight){
//                        exists = true;
//                    }
//                }
//                if(!exists){
//                    infraredHeightArray.add(height);
//                }
//            }
//        }
//
//        for (int i = 0; i < infraredHeightArray.size()-1; i++) {
//            for (int j = 0; j < infraredHeightArray.size()-1-i; j++) {
//                if (infraredHeightArray.getInt(j) > infraredHeightArray.getInt(j+1)) {
//                    int temp = infraredHeightArray.getInt(j);
//                    infraredHeightArray.set(j, infraredHeightArray.getInt(j+1));
//                    infraredHeightArray.set(j+1, temp);
//                }
//            }
//        }

        return infraredHeightArray;
    }

    /**
     * 任务执行
     *
     * @param taskInfo
     * @param timeStr
     * @author kliu
     * @date 2022/4/20 19:10
     */
    public synchronized void executeTask(TaskInfo taskInfo, String timeStr) throws IOException {
        TransactionStatus transactionStatus = null;
        long robotId = taskInfo.getRobotId();
        //校验机器人是否离线，如机器人已离线，则不允许发送任务
        if (!robotStatusService.robotOnline(robotId)) {
            throw new RuntimeException("任务执行失败，当前机器人【" + robotId + "】已离线");
        }

        String robotPower = robotStatusService.getRobotPower(robotId);
        JSONObject robotParamObject = robotParamService.getRobotParam(robotId);
        Double taskMinimumPower = robotParamObject.getDouble("task_minimum_power");
        if (Double.valueOf(robotPower) < taskMinimumPower) {
            throw new RuntimeException("任务执行失败，当前机器人【" + robotId + "】电量低于阈值");
        }

        JSONObject robotStatusObj = robotStatusService.getRobotStatus(robotId);

        //添加临时任务后，不再判断机器人是否正在执行任务了

        // true:需要重定位
        // false:不需要重定位
        Boolean relocalizationFlag = robotStatusObj.getBool("relocalization");
        if (relocalizationFlag) {
            throw new RuntimeException("任务执行失败，当前机器人定位状态异常，请联系运维人员处理");
        }

        long inspectTypeId = taskInfo.getInspectTypeId();
        long roomId = taskInfo.getRoomId();

        try {
            JSONObject roomParamObject = roomParamService.getRoomParam(roomId);

            //根据配置的检测项，生成下发的json文件
            InspectType inspectType = inspectTypeService.getDetlById(roomParamObject, inspectTypeId);
            String runMode = inspectType.getRunMode();

            JSONArray pointActionArray;

            List mechanicalArmParas = mechanicalArmParaDao.list(roomId);

            if (TaskRunMode.NORMAL.equals(runMode)) {
                if (mechanicalArmParas.size() > 0) {
                    pointActionArray = executeNormalInspectionArm(roomId, roomParamObject, inspectTypeId);
                } else {
                    pointActionArray = executeNormalInspection(roomId, roomParamObject, inspectTypeId);
                }
            } else {
                pointActionArray = executeFastInspection(roomId, roomParamObject, inspectTypeId);
            }

            transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
            long instanceId = saveTaskInstanceAndReturnInstanceId(taskInfo, timeStr);
            Map map = new LinkedHashMap<String, Object>();
            map.put("task_time", timeStr);
            map.put("task_id", instanceId);
            map.put("robot_id", robotId);
            map.put("run_mode", runMode);
            map.put("point_action_list", pointActionArray);

            Map returnMap = new LinkedHashMap<String, Object>();
            returnMap.put("data", map);
            String json = new JSONObject(returnMap).toString();
            log.info("普通巡检任务json:"+json);
            String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");

            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setInstanceId(instanceId);
            taskInstance.setTaskJsonCompress(issuedStr);
            taskInstanceDao.updateTaskJsonCompress(taskInstance);
            //显示提交事务，防止查询的时候查询不到
            dataSourceTransactionManager.commit(transactionStatus);
            mqttPushClient.publish("industrial_robot_issued/" + robotId, issuedStr);
            pointActionArray = null;
            returnMap = null;
            roomParamObject = null;
            robotStatusService.setRobotTaskStatus(robotId, true);
        } catch (Exception e) {
            log.error(commonService.getExceptionSrintStackTrace(e));
            if (transactionStatus != null) {
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 执行机器人移动任务
     *
     * @param roomId
     * @author kliu
     * @date 2022/5/24 16:52
     */
    public void executeRobotMoveTask(long roomId, String pointName) {

        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        long instanceId = RandomUtil.randomInt(1, 100);

        String timeStr = DateUtil.now();
        JSONArray pointActionArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        PointInfo pointInfo = new PointInfo();
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName(pointName);
        pointInfo = pointInfoDao.getDetlById(pointInfo);

        List<PointInfo> pointInfos = new ArrayList<>();
        pointInfos.add(pointInfo);

        JSONObject postureObject = getPosture(pointInfos, pointName);
        jsonObject.set("action", "navigation");
        jsonObject.set("param", postureObject);

        JSONArray detectionArr = new JSONArray();
        detectionArr.add(jsonObject);

        JSONObject pointAction = new JSONObject();
        pointAction.set("point_name", pointName);
        pointAction.set("detection_item", detectionArr);

        JSONArray pointActionArr = new JSONArray();
        pointActionArr.add(pointAction);

        pointActionArray.add(pointActionArr);

        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", timeStr);
        map.put("task_id", instanceId);
        map.put("robot_id", robotId);
        map.put("run_mode", "normal");
        map.put("point_action_list", pointActionArray);

        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        String json = new JSONObject(returnMap).toString();
        log.info(json);
        String compress = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
        mqttPushClient.publish("industrial_robot_issued/" + robotId, compress);

        pointActionArray = null;
        returnMap = null;
    }

    /**
     * 快速巡检-模式一巡检
     *
     * @param roomId
     * @param roomParamObject
     * @param inspectTypeId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/5/20 16:14
     */
    @Deprecated
    @SuppressWarnings("AlibabaMethodTooLong")
    private JSONArray executeFastInspection(long roomId, JSONObject roomParamObject, long inspectTypeId) throws IOException {
        //升降杆是否初始化
        boolean lifterInit = false;
        //升降杆状态
        String direction = "down";
        //当前升降杆高度
        int currentLifterHeight = 0;
        //检测项  点位检测项
        Map detectionMap, pointActionMap;

        List<DetectionInfo> detectionInfos;
        List<String> cabineRowList;
        JSONArray rowPointArray;
        JSONObject detectionObject;
        JSONObject param = null;

        //机柜列表
        List cabinetList = getCabinetList(roomParamObject, inspectTypeId);
        JSONArray detectionArray, pointActionArray = new JSONArray();

        //所有位姿信息 一次查询所有，进行遍历，单条查询速度略慢
        List<PointInfo> pointInfos = pointInfoDao.list(roomId);

        //升降杆高度
        JSONArray infraredHeightArray = getInfraredHeightArray(roomParamObject);

        //文件中检测项、阈值等数据
        JSONArray rowPointActionArray = new JSONArray();

        //获取传入的检测点中所有的升降杆高度数据，主要防止不同检测点对应升降杆位置不一致的情况
        //实际是否存在该种情况未可知，暂时考虑进去
        List<Integer> lifterHeightList = new ArrayList();
        for (int i = 0; i < infraredHeightArray.size(); i++) {
            lifterHeightList.add(infraredHeightArray.getInt(i));
        }

        //对数据进行升序排序
        //此处要求红外测温检测项对应的升降杆高度要保证升序（保存检测项阈值json文件时进行强转换）
        Object[] lifterHeightArray = lifterHeightList.toArray();
        Arrays.sort(lifterHeightArray);

        //如果都没有升降杆相关数据，认为每个机柜执行一次即可，此时添加一条数据为0的数据
        if (lifterHeightArray.length == 0) {
            lifterHeightArray = new Integer[1];
            lifterHeightArray[0] = 0;
        }

        //遍历每一排机柜数据，按照每一排机柜生成json
        for (int i = 0; i < cabinetList.size(); i++) {
            if ("down".equals(direction)) {
                direction = "up";
                Arrays.sort(lifterHeightArray);
            } else {
                direction = "down";
                Arrays.sort(lifterHeightArray, Collections.reverseOrder());
            }

            boolean lifterChangeDirection = false;

            //第一排机柜升降杆一直上升，第二排机柜升降杆一直降序
            rowPointArray = (JSONArray) cabinetList.get(i);
            cabineRowList = new ArrayList();
            //某一排机柜详细检测点数据
            for (int j = 0; j < rowPointArray.size(); j++) {
                cabineRowList.add(rowPointArray.get(j).toString());
            }
            //遍历升降杆高度数据
            for (int q = 0; q < lifterHeightArray.length; q++) {
                int lifterHeightArrayHeight = (int) lifterHeightArray[q];

                //某一排机柜
                for (int j = 0; j < cabineRowList.size(); j++) {
                    pointActionMap = new LinkedHashMap<String, Object>();
                    //机柜检测点名称
                    String pointName = cabineRowList.get(j);
                    detectionArray = new JSONArray();

                    //升降杆未初始化，需要初始化,执行第一个机柜任务时，初始化升降杆，将升降杆位置高度置为0，仅在机器人进行第一个机柜扫描时需要初始化升降杆
                    if (!lifterInit) {
                        detectionMap = new LinkedHashMap();
                        detectionMap.put("action", "init_lifter");
                        detectionArray.add(new JSONObject(detectionMap));
                        currentLifterHeight = 0;
                        lifterInit = true;
                    }
                    //当前升降杆高度与检测项中升降杆高度不一致
                    if (currentLifterHeight != lifterHeightArrayHeight) {
                        detectionMap = new LinkedHashMap();
                        param = new JSONObject();
                        if ("up".equals(direction)) {
                            param.set("direction", "up");
                            int distance = lifterHeightArrayHeight - currentLifterHeight;
                            param.set("distance", distance);

                            //当换到第二排时，如果高度不是在最高点，而第二排数据在最高点时会出现负值，此处需要进行额外的数据
                            if (distance < 0) {
                                param.set("direction", "down");
                                param.set("distance", Math.abs(distance));
                            }

                            detectionMap.put("action", "lifter");
                            detectionMap.put("param", param);
                            detectionArray.add(new JSONObject(detectionMap));
                            currentLifterHeight = lifterHeightArrayHeight;
                        } else {
                            param.set("direction", "down");
                            int distance = currentLifterHeight - lifterHeightArrayHeight;
                            param.set("distance", distance);

                            //当换到第二排时，如果高度不是在最低点，而第二排数据在最低点时会出现负值，此处需要进行额外的数据
                            if (distance < 0) {
                                param.set("direction", "up");
                                param.set("distance", Math.abs(distance));
                            }

                            detectionMap.put("action", "lifter");
                            detectionMap.put("param", param);
                            detectionArray.add(new JSONObject(detectionMap));
                            currentLifterHeight = lifterHeightArrayHeight;
                        }
                    }

                    //导航  位姿数据获取及填充
                    detectionMap = new LinkedHashMap();
                    detectionMap.put("action", "navigation");
                    detectionMap.put("param", getPosture(pointInfos, pointName));
                    detectionArray.add(new JSONObject(detectionMap));

                    //获取该点检测项数据
                    detectionInfos = detectionCombinationService.list(roomParamObject, pointName, inspectTypeId);

                    //判断有没有红外测温和门控相关技能
                    boolean infraredExist = false;
                    boolean gatingExist = false;
                    for (DetectionInfo detectionInfo : detectionInfos) {
                        if (Detection.INFRARED.equals(detectionInfo.getRobotDetection())) {
                            //先看有没有红外测温相关的技能配置（与升降杆有关），如果没有，则认为该机柜处仅执行一次
                            infraredExist = true;
                            continue;
                        }
                        if (Detection.GATING.equals(detectionInfo.getRobotDetection())) {
                            //先看有没有门控相关的技能配置，有门控则每次都需要执行
                            gatingExist = true;
                            continue;
                        }
                    }


                    //无红外无门控，则仅执行传感器及报警灯相关检测项，仅执行一次
                    if (!infraredExist && !gatingExist) {
                        boolean sensorActionFlag = false;
                        for (DetectionInfo detectionInfo : detectionInfos) {
                            String detectionId = detectionInfo.getDetectionId();
                            //传感器数据仅添加一次，加快巡检效率
                            if (Detection.SENSOR.indexOf(detectionId) > -1) {
                                if (!sensorActionFlag) {
                                    detectionMap = new LinkedHashMap();
                                    detectionMap.put("action", detectionInfo.getRobotDetection());
                                    detectionArray.add(new JSONObject(detectionMap));
                                    sensorActionFlag = true;
                                }
                                continue;
                            }
                            //非传感器检测项直接添加
                            detectionMap = new LinkedHashMap();
                            detectionMap.put("action", detectionInfo.getRobotDetection());
                            detectionArray.add(new JSONObject(detectionMap));
                        }
                        pointActionMap.put("point_name", pointName);
                        pointActionMap.put("detection_item", detectionArray);
                        rowPointActionArray.add(new JSONObject(pointActionMap));
                        cabineRowList.remove(j);
                        j--;
                        continue;
                    }

                    //无红外有门控，则仅执行门控，仅执行一次
                    if (!infraredExist && gatingExist) {
                        for (DetectionInfo detectionInfo : detectionInfos) {
                            //添加门禁技能
                            if (Detection.GATING.equals(detectionInfo.getRobotDetection())) {
                                detectionArray.add(addGatingDetection(roomId, pointName, detectionInfo));
                                continue;
                            }
                        }
                        pointActionMap.put("point_name", pointName);
                        pointActionMap.put("detection_item", detectionArray);
                        rowPointActionArray.add(new JSONObject(pointActionMap));
                        cabineRowList.remove(j);
                        j--;
                        continue;
                    }

                    //存在红外检测技能
                    for (DetectionInfo detectionInfo : detectionInfos) {
                        //处理红外技能
                        if (Detection.INFRARED.equals(detectionInfo.getRobotDetection())) {
                            lifterChangeDirection = true;
                            for (int i1 = 0; i1 < infraredHeightArray.size(); i1++) {
                                //当前高度与配置文件中相同，则添加红外测温技能
                                int height = infraredHeightArray.getInt(i1);
                                if (currentLifterHeight == height) {
                                    detectionMap = new LinkedHashMap();
                                    detectionMap.put("action", Detection.INFRARED);
                                    detectionArray.add(detectionMap);

                                    //wzj 20226.4修改 优先进行传感器、报警灯数据检测
                                    //当前高度等于当前机柜第一个高度，添加除红外以外的其他技能
                                    //升降杆上升时需要获取第一个高度再执行相关技能，下降时需要使用最后一个高度进行环境监测数据的测量
                                    int allDetectionAddBound = 0;
                                    if ("up".equals(direction)) {
                                        allDetectionAddBound = 0;
                                    } else {
                                        allDetectionAddBound = infraredHeightArray.size() - 1;
                                    }

                                    //高度属于点位第一个高度时，执行非红外、非门禁技能
                                    if (currentLifterHeight == infraredHeightArray.getInt(allDetectionAddBound)) {
                                        //执行所有非红外技能、非门禁技能
                                        boolean sensorActionFlag = false;
                                        for (DetectionInfo info : detectionInfos) {
                                            if (Detection.INFRARED.equals(info.getRobotDetection())) {
                                                continue;
                                            }
                                            String detectionId = info.getDetectionId();
                                            //传感器数据仅添加一次，加快巡检效率
                                            if (Detection.SENSOR.indexOf(detectionId) > -1) {
                                                if (!sensorActionFlag) {
                                                    detectionMap = new LinkedHashMap();
                                                    detectionMap.put("action", info.getRobotDetection());
                                                    detectionArray.add(new JSONObject(detectionMap));
                                                    sensorActionFlag = true;
                                                }
                                                continue;
                                            }
                                            if (Detection.GATING.indexOf(detectionId) > -1) {
                                                continue;
                                            }
                                            //非传感器检测项，直接添加
                                            detectionMap = new LinkedHashMap();
                                            detectionMap.put("action", info.getRobotDetection());
                                            detectionArray.add(new JSONObject(detectionMap));
                                        }
                                    }

                                }
                            }
                        }

                        //添加门禁技能
                        if (Detection.GATING.equals(detectionInfo.getRobotDetection())) {
                            detectionArray.add(addGatingDetection(roomId, pointName, detectionInfo));
                            continue;
                        }
                    }

                    pointActionMap.put("point_name", pointName);
                    pointActionMap.put("detection_item", detectionArray);
                    rowPointActionArray.add(new JSONObject(pointActionMap));
                }
            }

            //升降杆不转向
            if (!lifterChangeDirection) {
                if ("down".equals(direction)) {
                    direction = "up";
                } else {
                    direction = "down";
                }
            }
        }

        addBackChargingPile(rowPointActionArray, roomId);
        moveLifterToTop(rowPointActionArray, currentLifterHeight);

        pointActionArray.add(rowPointActionArray);

        detectionMap = null;
        pointActionMap = null;
        detectionInfos = null;
        cabineRowList = null;
        rowPointArray = null;
        detectionObject = null;
        param = null;
        cabinetList = null;
        detectionArray = null;
        pointInfos = null;
        infraredHeightArray = null;
        rowPointActionArray = null;
        lifterHeightList = null;

        return pointActionArray;
    }


    /**
     * 添加导航及门控指令
     *
     * @param roomId
     * @param pointName
     * @param detectionInfo
     * @param pointInfos
     * @param rowPointActionArray
     * @return void
     * @author kliu
     * @date 2022/10/24 17:51
     */
    public void addNavAndGating(long roomId, String pointName, DetectionInfo detectionInfo, List<PointInfo> pointInfos, JSONArray rowPointActionArray) {
        JSONArray detectionArray = new JSONArray();
        //导航到目标点，执行门控指令
        JSONObject detectionObject = new JSONObject();
        detectionObject.set("action", "navigation");
        detectionObject.set("param", getPosture(pointInfos, pointName));
        detectionArray.add(detectionObject);
        detectionArray.add(addGatingDetection(roomId, pointName, detectionInfo));
        JSONObject pointObject = new JSONObject();
        pointObject.set("point_name", pointName);
        pointObject.set("detection_item", detectionArray);
        rowPointActionArray.add(pointObject);
    }

    /**
     * 添加门禁技能
     *
     * @param roomId
     * @param pointName
     * @param detectionInfo
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/25 11:18
     */
    public JSONObject addGatingDetection(long roomId, String pointName, DetectionInfo detectionInfo) {
        GatingPara gatingPara = gatingParaDao.getDetlById(roomId, pointName);
        String doorCode = gatingPara.getDoorCode();
        String requestOrder = gatingPara.getRequestOrder();
        Map detectionMap = new LinkedHashMap();
        JSONObject param = new JSONObject();
        param.set("door_code", doorCode);
        param.set("request_order", requestOrder);
        detectionMap.put("action", detectionInfo.getRobotDetection());
        detectionMap.put("param", param);
        return new JSONObject(detectionMap);
    }

    /**
     * 普通巡检-模式二巡检
     *
     * @param roomId
     * @param roomParamObject
     * @param inspectTypeId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/5/20 16:14
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    private JSONArray executeNormalInspection(long roomId, JSONObject roomParamObject, long inspectTypeId) throws IOException {
        //升降杆是否初始化
        boolean lifterInit = false;
        //升降杆状态
        String direction = "down";
        //当前升降杆高度
        int currentLifterHeight = 0;
        //检测项  点位检测项
        Map detectionMap, pointActionMap;

        List<DetectionInfo> detectionInfos;
        JSONObject param;

        //机柜列表
        List cabinetList = getCabinetList(roomParamObject, inspectTypeId);
        JSONArray detectionArray, pointActionArray = new JSONArray();

        //所有位姿信息 一次查询所有，进行遍历，单条查询速度略慢
        List<PointInfo> pointInfos = pointInfoDao.list(roomId);

        //升降杆高度
        JSONArray infraredHeightArray = getInfraredHeightArray(roomParamObject);

        //文件中检测项、阈值等数据
        JSONArray rowPointActionArray = new JSONArray();

        //该版本中，口罩检测项与红外测温检测项不能混用
        boolean maskExist = false;

        DetectionInfo gatingDetectionInfo = new DetectionInfo();
        gatingDetectionInfo.setDetectionId("gating");
        gatingDetectionInfo.setRobotDetection("gating");

        JSONArray point2PointPathArr;

        String currentPointName = "";
        String currentDoorStatus = "close";

        JSONObject pointCloseDoorObject;

        for (int i = 0; i < cabinetList.size(); i++) {
            JSONArray cabinetArr = (JSONArray) cabinetList.get(i);
            for (int j = 0; j < cabinetArr.size(); j++) {
                pointCloseDoorObject = null;
                String pointName = cabinetArr.getStr(j);
                if ("up".equals(direction)) {
                    direction = "down";
                } else {
                    direction = "up";
                }
                detectionArray = new JSONArray();

                //如果为第一个巡检点，则需要添加充电桩到点位的路径规划
                if (i == 0 && j == 0) {
                    addGatingDetectionForFirstPointName(roomId, pointName, rowPointActionArray);
                    //判断当前点位上是否配置了门的操作，配置了门的操作，意味着需要先执行门开的指令才能到达点位，否则导航会到不了该点
                    //开门->到达点位->关门->巡检
                    TreeNode treeNode = pointTreeNodeService.getTreeNode(roomId, pointName);
                    if (treeNode != null) {
                        pointInfos = pointInfoDao.list(roomId);
                        if (treeNode.isDoorFlag()) {
                            //当前门关，则该点位执行开
                            //当前门开，则该点位执行关
                            String parentNode = treeNode.getParentNode();
                            //当前门关
                            if ("close".equals(currentDoorStatus)) {
                                currentDoorStatus = "open";
                                //该点位添加对门的操作
                                //执行门开 内开外开都可
                                detectionArray.add(addGatingDetection(roomId, parentNode + "-内开", gatingDetectionInfo));

                                pointCloseDoorObject = addGatingDetection(roomId, parentNode + "-内关", gatingDetectionInfo);
                                currentDoorStatus = "close";
                            } else {
                                //当前门开，啥也不执行
                            }
                        }
                    }
                } else {
                    //非第一个点位，需要判断上一节点与当前节点之间是否有门，如果有门需要添加相应的操作
                    addGatingDetectionForPointName2NewPointName(roomId, currentPointName, pointName, pointInfos, rowPointActionArray);

                    //判断当前点位上是否配置了门的操作，配置了门的操作，意味着需要先执行门开的指令才能到达点位，否则导航会到不了该点
                    //开门->到达点位->关门->巡检
                    TreeNode treeNode = pointTreeNodeService.getTreeNode(roomId, pointName);
                    if (treeNode != null) {
                        if (treeNode.isDoorFlag()) {
                            //当前门关，则该点位执行开
                            //当前门开，则该点位执行关
                            String parentNode = treeNode.getParentNode();
                            //当前门关
                            if ("close".equals(currentDoorStatus)) {
                                currentDoorStatus = "open";
                                //该点位添加对门的操作
                                //执行门开 内开外开都可
                                detectionArray.add(addGatingDetection(roomId, parentNode + "-内开", gatingDetectionInfo));

                                pointCloseDoorObject = addGatingDetection(roomId, parentNode + "-内关", gatingDetectionInfo);

                                currentDoorStatus = "close";
                            } else {
                                //当前门开，啥也不执行
                            }
                        }
                    }
                }

                //获取该点检测项数据
                detectionInfos = detectionCombinationService.list(roomParamObject, pointName, inspectTypeId);

                for (DetectionInfo detectionInfo : detectionInfos) {
                    String detectionId = detectionInfo.getDetectionId();
                    if (Detection.MASK.equals(detectionId)) {
                        maskExist = true;
                        break;
                    }
                }

                //口罩检测项目前仅适用室外，无升降杆相关操作，不需要初始化升降杆
                if (!maskExist) {
                    if (!lifterInit) {
                        detectionMap = new LinkedHashMap();
                        detectionMap.put("action", "init_lifter");
                        detectionArray.add(new JSONObject(detectionMap));
                        currentLifterHeight = 0;
                        lifterInit = true;
                    }
                }

                //导航  位姿数据获取及填充
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", getPosture(pointInfos, pointName));
                detectionArray.add(new JSONObject(detectionMap));

                //说明点位上执行了开门指令，此时到达点位之后，应该将门关闭
                if (pointCloseDoorObject != null) {
                    detectionArray.add(pointCloseDoorObject);
                }

                //判断有没有等待技能--等待优先执行
                for (DetectionInfo detectionInfo : detectionInfos) {
                    String detectionId = detectionInfo.getDetectionId();
                    if (Detection.WAIT.equals(detectionId)) {
                        detectionMap.put("action", detectionInfo.getRobotDetection());
                        param = new JSONObject();
                        param.set("wait_time", detectionInfo.getThreshold());
                        detectionMap.put("param", param);
                        detectionArray.add(new JSONObject(detectionMap));
                    }
                }

                //添加灭火器检测
                for (DetectionInfo detectionInfo : detectionInfos) {
                    String detectionId = detectionInfo.getDetectionId();
                    if (Detection.FIREEXTINGUISHER.equals(detectionId)) {
                        //灭火器检测有个比较特殊的点，需要将升降杆调整到一定的高度，此处需要添加升降杆的处理（用前置拍照检测）
                        //先下降或上升至指定高度，再恢复原来高度
                        int lifterHeight = Integer.parseInt(detectionInfo.getThreshold());
                        int distance = currentLifterHeight - lifterHeight;
                        if (distance > 0) {
                            param = new JSONObject();
                            param.set("direction", "down");
                            detectionMap = new LinkedHashMap();
                            param.set("distance", Math.abs(distance));
                            detectionMap.put("action", "lifter");
                            detectionMap.put("param", param);
                            detectionArray.add(new JSONObject(detectionMap));
                        } else if (distance < 0) {
                            param = new JSONObject();
                            param.set("direction", "up");
                            detectionMap = new LinkedHashMap();
                            param.set("distance", Math.abs(distance));
                            detectionMap.put("action", "lifter");
                            detectionMap.put("param", param);
                            detectionArray.add(new JSONObject(detectionMap));
                        }

                        //获取灭火器特征值
                        FireExtinguisherPara fireExtinguisherPara = fireExtinguisherParaDao.findById(roomId, pointName);
                        if (fireExtinguisherPara == null) {
                            throw new RuntimeException("机房ID【"+roomId+"】点位名称【"+pointName+"】获取灭火器特征值失败，无数据，请检查");
                        }
                        String fireExitinguisherPos = fireExtinguisherPara.getFireExitinguisherPos();
                        param = new JSONObject();
                        param.set("fire_exitinguisher_pos", JSONUtil.parseArray(fireExitinguisherPos));
                        detectionMap = new LinkedHashMap();
                        detectionMap.put("action", detectionInfo.getRobotDetection());
                        detectionMap.put("param", param);
                        detectionArray.add(new JSONObject(detectionMap));

                        if (distance > 0) {
                            param = new JSONObject();
                            param.set("direction", "up");
                            detectionMap = new LinkedHashMap();
                            param.set("distance", Math.abs(distance));
                            detectionMap.put("action", "lifter");
                            detectionMap.put("param", param);
                            detectionArray.add(new JSONObject(detectionMap));
                        } else if (distance < 0) {
                            param = new JSONObject();
                            param.set("direction", "down");
                            detectionMap = new LinkedHashMap();
                            param.set("distance", Math.abs(distance));
                            detectionMap.put("action", "lifter");
                            detectionMap.put("param", param);
                            detectionArray.add(new JSONObject(detectionMap));
                        }
                    }
                }

                //判断有没有红外测温相关技能
                boolean infraredExist = false;

                for (DetectionInfo detectionInfo : detectionInfos) {
                    String detectionId = detectionInfo.getDetectionId();
                    if (Detection.INFRARED.equals(detectionId)) {
                        infraredExist = true;
                        break;
                    }
                }
                if (maskExist && infraredExist) {
                    throw new RuntimeException("口罩检测项与红外测温检测项暂不支持混用，口罩检测仅支持室外机器人使用");
                }
                //无红外测温检测数据，升降杆此时不变化，重新赋值
                if (!infraredExist) {
                    //升降杆方向认为不变化
                    if ("up".equals(direction)) {
                        direction = "down";
                    } else {
                        direction = "up";
                    }
                }

                //优先执行非红外测温技能
                boolean sensorActionFlag = false;
                for (DetectionInfo detectionInfo : detectionInfos) {
                    String robotDetection = detectionInfo.getRobotDetection();
                    String detectionId = detectionInfo.getDetectionId();
                    if (!Detection.INFRARED.equals(robotDetection) && !Detection.FIREEXTINGUISHER.equals(robotDetection)) {
                        //传感器数据仅添加一次，加快巡检效率
                        //添加传感器
                        if (Detection.SENSOR.indexOf(detectionId) > -1) {
                            if (!sensorActionFlag) {
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", robotDetection);
                                detectionArray.add(new JSONObject(detectionMap));
                                sensorActionFlag = true;
                            }
                            continue;
                        }
                        detectionMap = new LinkedHashMap();
                        detectionMap.put("action", robotDetection);
                        detectionArray.add(new JSONObject(detectionMap));
                    }
                }

                //如果该机柜的检测项中没有对升降杆的操作，则认为direction不需要变向
                boolean directionChangeFlag = false;

                //再执行红外技能
                for (DetectionInfo detectionInfo : detectionInfos) {
                    if (Detection.INFRARED.equals(detectionInfo.getRobotDetection())) {
                        if ("up".equals(direction)) {
                            for (int i1 = 0; i1 < infraredHeightArray.size(); i1++) {
                                int height = infraredHeightArray.getInt(i1);
                                param = new JSONObject();
                                param.set("direction", "up");
                                int distance = height - currentLifterHeight;
                                if (distance > 0) {
                                    detectionMap = new LinkedHashMap();
                                    param.set("distance", distance);
                                    detectionMap.put("action", "lifter");
                                    detectionMap.put("param", param);
                                    detectionArray.add(new JSONObject(detectionMap));
                                    currentLifterHeight += distance;
                                    directionChangeFlag = true;
                                } else if (distance < 0) {
                                    param.set("direction", "down");
                                    detectionMap = new LinkedHashMap();
                                    param.set("distance", Math.abs(distance));
                                    detectionMap.put("action", "lifter");
                                    detectionMap.put("param", param);
                                    detectionArray.add(new JSONObject(detectionMap));
                                    currentLifterHeight -= Math.abs(distance);
                                    directionChangeFlag = true;
                                }
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));
                            }
                        } else {
                            for (int i1 = infraredHeightArray.size() - 1; i1 >= 0; i1--) {
                                int height = infraredHeightArray.getInt(i1);
                                param = new JSONObject();
                                param.set("direction", "down");
                                int distance = currentLifterHeight - height;
                                if (distance > 0) {
                                    detectionMap = new LinkedHashMap();
                                    param.set("distance", distance);
                                    detectionMap.put("action", "lifter");
                                    detectionMap.put("param", param);
                                    detectionArray.add(new JSONObject(detectionMap));
                                    currentLifterHeight -= distance;
                                    directionChangeFlag = true;
                                } else if (distance < 0) {
                                    param.set("direction", "up");
                                    detectionMap = new LinkedHashMap();
                                    param.set("distance", Math.abs(distance));
                                    detectionMap.put("action", "lifter");
                                    detectionMap.put("param", param);
                                    detectionArray.add(new JSONObject(detectionMap));
                                    currentLifterHeight += Math.abs(distance);
                                    directionChangeFlag = true;
                                }
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));
                            }
                        }
                    }
                }

                //升降杆方向认为不变化
                if (!directionChangeFlag) {
                    if ("up".equals(direction)) {
                        direction = "down";
                    } else {
                        direction = "up";
                    }
                }

                pointActionMap = new LinkedHashMap();
                pointActionMap.put("point_name", pointName);
                pointActionMap.put("detection_item", detectionArray);
                rowPointActionArray.add(new JSONObject(pointActionMap));

                currentPointName = pointName;
            }
        }

        //如果包含口罩检测，则不回桩（室外取消回桩接口）
        //室内保留回桩，不包含口罩检测则为室内巡检
        if (!maskExist) {
            addBackChargingPile(rowPointActionArray, roomId);
        }

        if (!maskExist) {
            moveLifterToTop(rowPointActionArray, currentLifterHeight);
        }


        pointActionArray.add(rowPointActionArray);

        detectionMap = null;
        pointActionMap = null;
        detectionInfos = null;
        param = null;
        cabinetList = null;
        detectionArray = null;
        pointInfos = null;
        infraredHeightArray = null;
        rowPointActionArray = null;

        return pointActionArray;
    }

    /**
     * 对第一个点位添加门控的处理，主要是跨机房的情况
     * @param
     * @return void
     * @author kliu
     * @date 2022/11/16 15:38
     */
    public void addGatingDetectionForFirstPointName(long roomId, String pointName, JSONArray jsonArray){
        String tempPointName = "";
        DetectionInfo gatingDetectionInfo = new DetectionInfo();
        gatingDetectionInfo.setDetectionId("gating");
        gatingDetectionInfo.setRobotDetection("gating");
        JSONArray point2ChargingPilePathArray = pointTreeNodeService.getChargingPilePath2Point(roomId, pointName);
        for (int p = 0; p < point2ChargingPilePathArray.size(); p++) {
            String node = point2ChargingPilePathArray.getStr(p);

            //计算点位对应的机房id
            int begin = node.indexOf("-");
            int end = node.indexOf("-", begin + 1);
            if (end == -1) {
                end = node.length();
            }
            long nodeRoomId = Long.parseLong(node.substring(begin + 1, end));

            //获取机房下的所有位姿信息
            List pointInfos = pointInfoDao.list(nodeRoomId);
            int count = StrUtil.count(node, "-");
            //证明是机房内的一组组的机柜的门
            //point2ChargingPilePathArray.size()=1时 机房内部冷通道门
            if (count == 2) {
                //机柜门  执行两个点位  外开 内关  此时认为不应该存在两层机柜门嵌套的情况，如存在这种情况需要修改该部分逻辑
                //门外开
                tempPointName = node + "-外开";
                addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);

                //门内关
                tempPointName = node + "-内关";
                addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
            } else if (count == 1) {
                //point2ChargingPilePathArray.size()>1时   机房门、机房门、冷通道门
                //机房门
                //需要判断当前节点是应该执行门内开  还是门外开
                //依据常州机房的结构图，当前节点的下一个节点如果还是机房门，则当前是门内开、门外关
                //如当前节点的下一个节点没有机房门，则是门外开、门内关
                //即一个充电桩对应多个机房
                if (point2ChargingPilePathArray.size() >= p + 1) {
                    String node1 = point2ChargingPilePathArray.getStr(p + 1);
                    //下一节点对应的横线数量
                    int count1 = StrUtil.count(node1, "-");
                    //当前节点的下一个节点还是机房门，对应门内开、门外关
                    if (count1 == 1) {
                        //门内开
                        tempPointName = node + "-内开";
                        addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);

                        //门外关
                        tempPointName = node + "-外关";
                        addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
                    } else {
                        //下一节点是冷通道门的时候
                        //门外开
                        tempPointName = node + "-外开";
                        addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);

                        //门外关
                        tempPointName = node + "-内关";
                        addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
                    }
                }
            } else {
                throw new RuntimeException("路径中存在未识别的node" + node);
            }
        }
    }

    public void addGatingDetectionForPointName2NewPointName(long roomId, String currentPointName, String newPointName, List pointInfos, JSONArray jsonArray){
        DetectionInfo gatingDetectionInfo = new DetectionInfo();
        gatingDetectionInfo.setDetectionId("gating");
        gatingDetectionInfo.setRobotDetection("gating");
        //非第一个点位，需要判断上一节点与当前节点之间是否有门，如果有门需要添加相应的操作
        JSONArray point2PointPathArr = pointTreeNodeService.getPoint2PointPath(roomId, currentPointName, newPointName);
        String tempPointName;
        if (point2PointPathArr.size() > 2) {
            throw new RuntimeException("点位【" + currentPointName + "】到点位【" + newPointName + "】路径规划失败，请联系开发人员处理");
        }
        if (point2PointPathArr.size() == 1) {
            //point2PointPathArr.size()=1
            //冷通道->热通道 或者 热通道->冷通道
            //判断上一节点对应的父节点的门的横线数量，如为2 则 门内开 门外关(冷通道->热通道)  如为1 则门外开 门内关（热通道->冷通道）
            TreeNode treeNode = pointTreeNodeService.getTreeNode(roomId, currentPointName);
            int count = StrUtil.count(treeNode.getParentNode(), "-");
            String str = point2PointPathArr.getStr(0);
            if (count == 2) {
                //门内开 门外关(冷通道->热通道)
                tempPointName = str + "-内开";
                addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);

                tempPointName = str + "-外关";
                addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
            } else {
                //门外开 门内关（热通道->冷通道）
                tempPointName = str + "-外开";
                addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);

                tempPointName = str + "-内关";
                addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
            }
        }

        if (point2PointPathArr.size() == 2) {
            //point2PointPathArr.size()==2
            //冷通道1->冷通道2
            String str = point2PointPathArr.getStr(0);
            String str1 = point2PointPathArr.getStr(1);

            //冷通道1 内开 外关
            tempPointName = str + "-内开";
            addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
            tempPointName = str + "-外关";
            addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);

            //冷通道2 外开 内关
            tempPointName = str1 + "-外开";
            addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
            tempPointName = str1 + "-内关";
            addNavAndGating(roomId, tempPointName, gatingDetectionInfo, pointInfos, jsonArray);
        }
    }

    /**
     * 普通巡检，机械臂版本
     *
     * @param roomId
     * @param roomParamObject
     * @param inspectTypeId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/8 11:06
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    private JSONArray executeNormalInspectionArm(long roomId, JSONObject roomParamObject, long inspectTypeId) {

        //检测项  点位检测项
        Map detectionMap, pointActionMap;
        List<DetectionInfo> detectionInfos;
        //朝向
        String direction = "up";
        //当前高度
        String currentHeight = "bottom";

        //机柜列表
        List cabinetList = getCabinetList(roomParamObject, inspectTypeId);
        JSONArray detectionArray, pointActionArray = new JSONArray();

        //所有位姿信息 一次查询所有，进行遍历，单条查询速度略慢
        List<PointInfo> pointInfos = pointInfoDao.list(roomId);

        //文件中检测项、阈值等数据
        JSONArray rowPointActionArray = new JSONArray();
        boolean armInit = false;

        //该版本中，口罩检测项与红外测温检测项不能混用
        boolean maskExist = false;
        JSONObject armMoveObject;


        for (int i = 0; i < cabinetList.size(); i++) {
            JSONArray cabinetArr = (JSONArray) cabinetList.get(i);
            for (int j = 0; j < cabinetArr.size(); j++) {
                String pointName = cabinetArr.getStr(j);
                detectionArray = new JSONArray();

                //获取该点检测项数据
                detectionInfos = detectionCombinationService.list(roomParamObject, pointName, inspectTypeId);

                for (DetectionInfo detectionInfo : detectionInfos) {
                    String detectionId = detectionInfo.getDetectionId();
                    if (Detection.MASK.equals(detectionId)) {
                        maskExist = true;
                        break;
                    }
                }

                //口罩检测项目前仅适用室外，无升降杆相关操作，不需要初始化升降杆
                if (!maskExist && !armInit) {
                    //初始化
                    detectionArray.add(getArmObject("initial"));
                    //下
                    detectionArray.add(getArmObject("bottom"));

                    detectionMap = new LinkedHashMap();
                    detectionMap.put("action", "init_lifter");
                    detectionArray.add(new JSONObject(detectionMap));

                    armInit = true;
                }

                //导航  位姿数据获取及填充
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", getPosture(pointInfos, pointName));
                detectionArray.add(new JSONObject(detectionMap));

                //判断有没有红外测温相关技能
                boolean infraredExist = false;
                boolean infradeXianlanExist = false;

                for (DetectionInfo detectionInfo : detectionInfos) {
                    String detectionId = detectionInfo.getDetectionId();
                    if (Detection.INFRARED.equals(detectionId)) {
                        infraredExist = true;
                    }
                    if (Detection.INFRAREDXIANLAN.equals(detectionId)) {
                        infradeXianlanExist = true;
                    }
                }
                if ((maskExist && infraredExist) || (maskExist && infradeXianlanExist)) {
                    throw new RuntimeException("口罩检测项与红外测温检测项暂不支持混用，口罩检测仅支持室外机器人使用");
                }
                //无红外测温检测数据，升降杆此时不变化，重新赋值
                if (!infraredExist) {
                    //升降杆方向认为不变化
                    if ("up".equals(direction)) {
                        direction = "down";
                    } else {
                        direction = "up";
                    }
                }

                //优先执行非红外测温技能
                boolean sensorActionFlag = false;
                for (DetectionInfo detectionInfo : detectionInfos) {
                    String robotDetection = detectionInfo.getRobotDetection();
                    String detectionId = detectionInfo.getDetectionId();
                    if (!Detection.INFRARED.equals(robotDetection) && !Detection.INFRAREDXIANLAN.equals(robotDetection)) {
                        //传感器数据仅添加一次，加快巡检效率
                        //添加传感器
                        if (Detection.SENSOR.indexOf(detectionId) > -1) {
                            if (!sensorActionFlag) {
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", robotDetection);
                                detectionArray.add(new JSONObject(detectionMap));
                                sensorActionFlag = true;
                            }
                            continue;
                        }
                        detectionMap = new LinkedHashMap();
                        detectionMap.put("action", robotDetection);
                        detectionArray.add(new JSONObject(detectionMap));
                    }
                }

                //再执行红外技能和红外线缆的检测
                for (DetectionInfo detectionInfo : detectionInfos) {
                    if (Detection.INFRARED.equals(detectionInfo.getDetectionId())) {
                        if ("up".equals(direction)) {
                            if ("bottom".equals(currentHeight)) {
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));
                                //升降杆
                                detectionArray.add(armAddLifter("up"));
                                //中下
                                detectionArray.add(getArmObject("bottomdown"));
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));

                                //升降杆
                                detectionArray.add(armAddLifter("up"));
                                //中下
                                detectionArray.add(getArmObject("topdown"));
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));

                                //升降杆
                                detectionArray.add(armAddLifter("up"));
                                //中上
                                detectionArray.add(getArmObject("top"));
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));
                                currentHeight = "top";
                                direction = "down";
                            }
                        } else {
                            if ("top".equals(currentHeight)) {
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));
                                //升降杆
                                detectionArray.add(armAddLifter("down"));
                                //中上
                                detectionArray.add(getArmObject("topdown"));
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));

                                //升降杆-临时
                                detectionArray.add(armAddLifter("down"));
                                //中下
                                detectionArray.add(getArmObject("bottomup"));
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));
                                //升降杆-临时
                                detectionArray.add(armAddLifter("down"));
                                //下
                                detectionArray.add(getArmObject("bottom"));
                                //红外
                                detectionMap = new LinkedHashMap();
                                detectionMap.put("action", Detection.INFRARED);
                                detectionArray.add(new JSONObject(detectionMap));

                                currentHeight = "bottom";
                                direction = "up";
                            }
                        }
                    }

                    if (Detection.INFRAREDXIANLAN.equals(detectionInfo.getDetectionId())) {
                        if (!"upward".equals(currentHeight)) {
                            detectionArray.add(getArmObject("upward"));
                        }
                        detectionMap = new LinkedHashMap();
                        detectionMap.put("action", Detection.INFRARED);
                        detectionArray.add(new JSONObject(detectionMap));
                        currentHeight = "upward";
                    }
                }

                pointActionMap = new LinkedHashMap();
                pointActionMap.put("point_name", pointName);
                pointActionMap.put("detection_item", detectionArray);
                rowPointActionArray.add(new JSONObject(pointActionMap));
            }
        }

        //如果包含口罩检测，则不回桩（室外取消回桩接口）
        //室内保留回桩，不包含口罩检测则为室内巡检
        if (!maskExist) {
            addBackChargingPile(rowPointActionArray, roomId);
        }

        pointActionArray.add(rowPointActionArray);

        detectionMap = null;
        pointActionMap = null;
        detectionInfos = null;
        cabinetList = null;
        detectionArray = null;
        pointInfos = null;
        rowPointActionArray = null;

        return pointActionArray;
    }

    /**
     * 生成机械臂参数
     *
     * @param positionHeight
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/9/8 11:22
     */
    private JSONObject getArmObject(String positionHeight) {
        JSONObject detection = new JSONObject();
        JSONObject param = new JSONObject();
        param.set("trajectory", positionHeight);
        detection.set("action", "mechanical_arm");
        detection.set("param", param);
        return detection;
    }

    /**
     * 机械臂添加升降杆-临时版本
     *
     * @param direction
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/9/27 18:26
     */
    private JSONObject armAddLifter(String direction) {
        JSONObject param = new JSONObject();
        JSONObject detection = new JSONObject();
        param.set("direction", direction);
        param.set("distance", 30);
        detection.set("action", "lifter");
        detection.set("param", param);
        return detection;
    }

    /**
     * 返回充电桩改为任务触发，非自动触发
     *
     * @param rowPointActionArray
     * @author kliu
     * @date 2022/5/24 16:29
     */
    public void addBackChargingPile(JSONArray rowPointActionArray, long roomId) {
        String pointName = rowPointActionArray.getJSONObject(rowPointActionArray.size() - 1).getStr("point_name");

        JSONObject detectionObject, pointObject;
        JSONArray detectionArray;
        List<PointInfo> pointInfos;
        String pointNameWithOutGating = pointName.replace("-内开", "").replace("-内关", "").replace("-外开", "").replace("-外关", "");
        JSONArray pathArray = pointTreeNodeService.getPoint2ChargingPilePath(roomId, pointNameWithOutGating);
        String tempPointName = "";
        DetectionInfo gatingDetectionInfo = new DetectionInfo();
        gatingDetectionInfo.setDetectionId("gating");
        gatingDetectionInfo.setRobotDetection("gating");
        if (pathArray.size() > 0) {
            for (int i = 0; i < pathArray.size(); i++) {
                String node = pathArray.getStr(i);
                if ("charging_pile".equals(node)) {
                    detectionObject = new JSONObject();
                    detectionArray = new JSONArray();
                    detectionObject.set("action", "back_charging_pile");
                    detectionArray.add(detectionObject);
                    pointObject = new JSONObject();
                    pointObject.set("point_name", "cdz");
                    pointObject.set("detection_item", detectionArray);
                    rowPointActionArray.add(pointObject);
                    continue;
                }
                int begin = node.indexOf("-");
                int end = node.indexOf("-", begin + 1);
                if (end == -1) {
                    end = node.length();
                }
                long nodeRoomId = Long.parseLong(node.substring(begin + 1, end));

                pointInfos = pointInfoDao.list(nodeRoomId);
                int count = StrUtil.count(node, "-");
                //证明是机房内的一组组的机柜的门
                //一组组的机柜门在任务中已经编排
                if (count == 2) {
                    //门内开
                    detectionArray = new JSONArray();
                    tempPointName = node + "-内开";
                    //导航到目标点，执行门控指令
                    addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, rowPointActionArray);

                    //门外关
                    detectionArray = new JSONArray();
                    tempPointName = node + "-外关";
                    //导航到目标点，执行门控指令
                    addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, rowPointActionArray);
                } else if (count == 1) {
                    //机房门
                    //需要判断当前节点是应该执行门内开  还是门外开
                    //依据常州机房的结构图，当前节点的下一个节点如果还是机房门，则当前是门内开、门外关
                    //如当前节点的下一个节点没有机房门，则是门外开、门内关
                    if (i + 1 <= pathArray.size()) {
                        String node1 = pathArray.getStr(i + 1);
                        int count1 = StrUtil.count(node1, "-");
                        //当前节点的下一个节点还是机房门，对应门内开、门外关
                        if (count1 == 1) {
                            //门内开
                            detectionArray = new JSONArray();
                            tempPointName = node + "-内开";
                            //导航到目标点，执行门控指令
                            addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, rowPointActionArray);

                            //门外关
                            detectionArray = new JSONArray();
                            tempPointName = node + "-外关";
                            //导航到目标点，执行门控指令
                            addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, rowPointActionArray);
                        } else {
                            //门内开
                            detectionArray = new JSONArray();
                            tempPointName = node + "-外开";
                            //导航到目标点，执行门控指令
                            addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, rowPointActionArray);

                            //门外关
                            detectionArray = new JSONArray();
                            tempPointName = node + "-内关";
                            //导航到目标点，执行门控指令
                            addNavAndGating(nodeRoomId, tempPointName, gatingDetectionInfo, pointInfos, rowPointActionArray);
                        }
                    }
                } else {
                    throw new RuntimeException("路径中存在未识别的node" + node);
                }
            }
        } else {
            JSONObject rowPointActionObject = rowPointActionArray.getJSONObject(rowPointActionArray.size() - 1);
            JSONArray detectionArr = rowPointActionObject.getJSONArray("detection_item");
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("action", "back_charging_pile");
            detectionArr.add(jsonObject);
        }
    }

    /**
     * 任务最后执行初始化升降杆，防止下次任务执行的时候需要再次初始化浪费时间
     *
     * @param rowPointActionArray
     * @author kliu
     * @date 2022/5/24 17:53
     */
    private void initLifter(JSONArray rowPointActionArray) {
        JSONObject rowPointActionObject = rowPointActionArray.getJSONObject(rowPointActionArray.size() - 1);
        JSONArray detectionArr = rowPointActionObject.getJSONArray("detection_item");
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("action", "init_lifter");
        detectionArr.add(jsonObject);
    }

    /**
     * 任务最后，将升降杆移到最高点
     *
     * @param rowPointActionArray
     * @return void
     * @author kliu
     * @date 2022/7/2 10:38
     */
    private void moveLifterToTop(JSONArray rowPointActionArray, int currentLifterHeight) {
        JSONObject rowPointActionObject = rowPointActionArray.getJSONObject(rowPointActionArray.size() - 1);
        JSONArray detectionArr = rowPointActionObject.getJSONArray("detection_item");
        JSONObject param = new JSONObject();
        int maxLifterHeight = 120;
        if (currentLifterHeight < maxLifterHeight) {
            param.set("direction", "up");
            Map detectionMap = new LinkedHashMap();
            param.set("distance", maxLifterHeight - currentLifterHeight);
            detectionMap.put("action", "lifter");
            detectionMap.put("param", param);
            detectionArr.add(new JSONObject(detectionMap));
        }
    }

    /**
     * 测试指示灯程序
     *
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/8/29 13:48
     */
    public void testAlarmLight(JSONObject jsonObject) {
        TransactionStatus transactionStatus = null;
        Long robotId = jsonObject.getLong("robotId");
        JSONArray pointNameArr = jsonObject.getJSONArray("pointNameArr");
        int count = jsonObject.getInt("count");
        List<PointInfo> pointInfos = pointInfoDao.list(robotRoomDao.getRoomIdByRobotId(robotId));

        JSONArray pointActionArray = new JSONArray();
        JSONArray detectionItemArray;
        JSONObject actionObject, pointObject;
        String tempName = "";

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < pointNameArr.size(); j++) {
                String pointName = pointNameArr.getStr(j);
                tempName = pointName + i;

                actionObject = new JSONObject();
                actionObject.set("action", "navigation");
                actionObject.set("param", getPosture(pointInfos, pointName));
                detectionItemArray = new JSONArray();
                detectionItemArray.add(actionObject);

                actionObject = new JSONObject();
                actionObject.set("action", "alarm_light");
                detectionItemArray.add(actionObject);
                pointObject = new JSONObject();
                pointObject.set("point_name", tempName);
                pointObject.set("detection_item", detectionItemArray);
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(pointObject);
                pointActionArray.add(jsonArray);
            }
        }

        PageBean list = taskInfoDao.listWithoutPark(0, robotId, 100, 1);
        List<TaskInfo> contentList = list.getContentList();
        if (contentList.size() == 0) {
            throw new RuntimeException("根据机器人获取任务失败，无可选择的任务");
        }

        String timeStr = DateUtil.now();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskId(contentList.get(0).getTaskId());
        taskInstance.setStartTime(timeStr);
        taskInstance.setExecStatus(TaskStatus.RUNNING);
        transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        long instanceId = taskInstanceDao.addAndReturnId(taskInstance);
        //显示提交事务，防止查询的时候查询不到
        dataSourceTransactionManager.commit(transactionStatus);
        transactionStatus = null;
        try {
            Map map = new LinkedHashMap<String, Object>();
            map.put("task_time", DateUtil.now());
            map.put("task_id", instanceId);
            map.put("robot_id", robotId);
            map.put("run_mode", "normal");
            map.put("point_action_list", pointActionArray);

            Map returnMap = new LinkedHashMap<String, Object>();
            returnMap.put("data", map);
            String json = new JSONObject(returnMap).toString();
            log.info(json);
            String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
            mqttPushClient.publish("industrial_robot_issued/" + robotId, issuedStr);
        } catch (Exception e) {
            if (transactionStatus != null) {
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            e.printStackTrace();
        }
    }
}
