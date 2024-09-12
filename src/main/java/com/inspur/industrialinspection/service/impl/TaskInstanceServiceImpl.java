package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.TaskStatus;
import com.inspur.cron.TaskExecuteCron;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.*;
import com.inspur.mqtt.MqttPushClient;
import com.inspur.page.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务实例服务实现
 *
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Service
@Slf4j
public class TaskInstanceServiceImpl implements TaskInstanceService {

    private volatile static ConcurrentHashMap<Long, Boolean> taskInstanceMap = new ConcurrentHashMap<Long, Boolean>();

    @Autowired
    private DetectionParamService detectionParamService;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private AlongWorkService alongWorkService;
    @Autowired
    private ItAssetTaskInstanceService itAssetTaskInstanceService;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    private ParticularPointInspectionService particularPointInspectionService;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private RemoteControlService remoteControlService;
    @Autowired
    private PointTreeNodeService pointTreeNodeService;
    @Autowired
    private PointInfoDao pointInfoDao;
    @Autowired
    private TaskExecuteCron taskExecuteCron;
    @Autowired
    private RoomInfoDao roomInfoDao;

    @Override
    public PageBean list(long roomId, long robotId, int pageSize, int page) {
        PageBean pageBean = taskInstanceDao.list(roomId, robotId, pageSize, page);
        List<TaskInstance> taskInstances = pageBean.getContentList();

        JSONObject roomParamObject = null;
        JSONArray inspectTypeArray = null;
        for (TaskInstance taskInstance : taskInstances) {
            long inspectTypeId = taskInstance.getInspectTypeId();
            roomParamObject = roomParamService.getRoomParam(taskInstance.getRoomId());
            inspectTypeArray = roomParamObject.getJSONObject("inspect_setting").getJSONArray("inspect_type");
            for (int i = 0; i < inspectTypeArray.size(); i++) {
                long fileInspectTypeId = inspectTypeArray.getJSONObject(i).getLong("inspect_type_id");
                if (inspectTypeId == fileInspectTypeId) {
                    String inspectTypeName = inspectTypeArray.getJSONObject(i).getStr("inspect_type_name");
                    taskInstance.setInspectTypeName(inspectTypeName);
                }
            }
        }
        return pageBean;
    }

    @Override
    public List list(int parkId, long roomId, long robotId, String startTime, String endTime) {
        List<TaskInstance> taskInstances = taskInstanceDao.list(parkId, roomId, robotId, startTime, endTime);

        JSONObject roomParamObject = null;
        JSONArray inspectTypeArray = null;
        for (TaskInstance taskInstance : taskInstances) {
            long inspectTypeId = taskInstance.getInspectTypeId();
            roomParamObject = roomParamService.getRoomParam(taskInstance.getRoomId());
            inspectTypeArray = roomParamObject.getJSONObject("inspect_setting").getJSONArray("inspect_type");
            for (int i = 0; i < inspectTypeArray.size(); i++) {
                long fileInspectTypeId = inspectTypeArray.getJSONObject(i).getLong("inspect_type_id");
                if (inspectTypeId == fileInspectTypeId) {
                    String inspectTypeName = inspectTypeArray.getJSONObject(i).getStr("inspect_type_name");
                    taskInstance.setInspectTypeName(inspectTypeName);
                }
            }
        }
        return taskInstances;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminate(TaskInstance taskInstance) {
        long instanceId = taskInstance.getInstanceId();
        if (!taskInstanceDao.checkExist(instanceId)) {
            throw new RuntimeException("要终止的任务不存在，请检查传入的数据");
        }
        String execStatus = taskInstanceDao.getDetlById(instanceId).getExecStatus();
        if (TaskStatus.TERMINATE.equals(execStatus)) {
            throw new RuntimeException("当前任务已终止，不允许再次终止");
        }
        if (TaskStatus.END.equals(execStatus)) {
            throw new RuntimeException("当前任务已结束，不允许终止");
        }
        if (TaskStatus.CREATE.equals(execStatus)) {
            throw new RuntimeException("当前任务未运行，不允许终止");
        }
        taskInstance = taskInstanceDao.getDetlById(instanceId);
        long taskId = taskInstance.getTaskId();
        TaskInfo taskInfo = taskInfoDao.getDetlById(taskId);
        long robotId = taskInfo.getRobotId();
        JSONObject issuedJsonObject = new JSONObject();
        issuedJsonObject.set("taskId", instanceId);
        //1   普通巡检    2   随工任务
        issuedJsonObject.set("type", 1);
        mqttPushClient.publish("industrial_robot_terminate/" + robotId, issuedJsonObject.toString());
        taskInstanceMap.put(instanceId, false);

        int i = 0;
        int terminateWaitCount = 50;
        while (i < terminateWaitCount) {
            if (taskInstanceMap.get(instanceId)) {
                taskInstanceMap.remove(instanceId);

                taskInstance.setEndTime(DateUtil.now());
                taskInstance.setExecStatus(TaskStatus.TERMINATE);
                //更新状态为已终止
                taskInstanceDao.update(taskInstance);
                break;
            } else {
                i++;
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (taskInstanceMap.containsKey(instanceId)) {
            throw new RuntimeException("任务终止失败，请尝试重新终止");
        }
    }

    @Override
    public void receiveTerminateResult(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        Long taskId = jsonObject.getLong("taskId");
        int type = 0;
        String taskTypeKey = "type";
        String messageKey = "message";
        if (jsonObject.containsKey(taskTypeKey)) {
            type = jsonObject.getInt(taskTypeKey);
        }
        //1 普通巡检    2随工任务
        int casualWorkType = 2;
        int itAssetWoekType = 3;
        if (type == casualWorkType) {
            if (jsonObject.containsKey(messageKey)) {
                String message = jsonObject.getStr(messageKey);
                alongWorkService.normalEndTask(taskId, "", message);
            }
            alongWorkService.setAlongWorkMap(taskId, true);
        } else if (type == itAssetWoekType) {
            itAssetTaskInstanceService.receiveTerminateResult(json);
        } else {
            if (taskInstanceMap.containsKey(taskId)) {
                taskInstanceMap.put(taskId, true);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void industrialRobotEndTask(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        Long instanceId = jsonObject.getLong("task_id");
        String endTime = jsonObject.getStr("end_time");

        int type = 0;
        String taskTypeKey = "type";
        if (jsonObject.containsKey(taskTypeKey)) {
            type = jsonObject.getInt(taskTypeKey);
        }
        //1 普通巡检    2随工任务  3 资产盘点  4 返航等临时任务 5 特定点巡检
        int casualWorkType = 2;
        int itAssetWorkType = 3;
        if (type == casualWorkType) {
            String message = jsonObject.getStr("message");
            alongWorkService.normalEndTask(instanceId, endTime, message);
            return;
        }
        if (type == itAssetWorkType) {
            itAssetTaskInstanceService.industrialRobotEndTask(json);
            return;
        }
        if (type == 4) {
            remoteControlService.tempTaskEnd(instanceId);
            return;
        }
        if (type == 5) {
            particularPointInspectionService.industrialRobotEndTask(json);
            return;
        }

        //机器人移动到某个机柜则采用同样的任务下发机制，此时，对应的任务实例id不会记录
        if (!taskInstanceDao.checkExist(instanceId)) {
            return;
        }

        TaskInstance dbTaskInstance = taskInstanceDao.getDetlByIdForUpdate(instanceId);

        //仅running状态下更新结束时间，防止终止任务后任务状态也变为已结束
        if (TaskStatus.RUNNING.equals(dbTaskInstance.getExecStatus())) {
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setInstanceId(instanceId);
            taskInstance.setEndTime(endTime);
            taskInstance.setExecStatus(TaskStatus.END);
            taskInstanceDao.update(taskInstance);
        }
    }

    @Override
    public int getRunningTaskCount(long roomId) {
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        List<TaskInstance> taskInstances = taskInstanceDao.getRunningTaskInstanceByRobotId(robotId);
        return taskInstances.size();
    }

    @Override
    public void terminateRunningTask(long roomId) {
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        List<TaskInstance> taskInstances = taskInstanceDao.getRunningTaskInstanceByRobotId(robotId);
        for (TaskInstance taskInstance : taskInstances) {
            terminate(taskInstance);
        }
    }

    @Override
    public void updateTaskStatus(String taskStatusJson) {
        JSONObject jsonObject = JSONUtil.parseObj(taskStatusJson);
        Long taskId = jsonObject.getLong("taskId");
        String time = jsonObject.getStr("time");
        //查询当前任务id是否存在
        TaskInstance task = taskInstanceDao.getTaskById(taskId);
        if (task != null) {
            task.setExecStatus(TaskStatus.SUSPEND);
            task.setEndTime(time);
            taskInstanceDao.updateById(task);
        }
    }

    @Override
    public List getInstanceInfos(int parkId, JSONObject paramObject) {
        if (!paramObject.containsKey("roomId")){
            throw new RuntimeException("机房id不能为空，请检查传入的数据");
        }
        if (!paramObject.containsKey("startTime")){
            throw new RuntimeException("开始时间不能为空，请检查传入的数据");
        }
        if (!paramObject.containsKey("endTime")){
            throw new RuntimeException("结束时间不能为空，请检查传入的数据");
        }
        Long roomId = paramObject.getLong("roomId");
        String startTime = paramObject.getStr("startTime");
        String endTime = paramObject.getStr("endTime");

        if (!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入机房id在系统中不存在，请检查传入的数据");
        }

        try {
            DateUtil.parse(startTime,"yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            throw new RuntimeException("传入的开始时间应符合yyyy-MM-dd HH:mm:ss，请检查传入的数据");
        }

        try {
            DateUtil.parse(endTime,"yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            throw new RuntimeException("传入的结束时间应符合yyyy-MM-dd HH:mm:ss，请检查传入的数据");
        }

        long betweenDay = DateUtil.betweenDay(DateUtil.parse(startTime), DateUtil.parse(endTime), false);
        if (betweenDay>365){
            throw new RuntimeException("起始时间与结束时间差值不能大于365天，请检查传入的数据");
        }

        return list(parkId, roomId, 0, startTime, endTime);
    }

    @Override
    public JSONArray getInstanceSensorDetl(int parkId, JSONObject paramObject) {
        if (!paramObject.containsKey("instanceId")){
            throw new RuntimeException("任务实例id不能为空，请检查传入的数据");
        }
        Long instanceId = paramObject.getLong("instanceId");
        if (instanceId<=0){
            throw new RuntimeException("任务实例id应大于0，请检查传入的数据");
        }
        List<TaskDetectionResult> list = taskDetectionResultDao.list(instanceId);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        JSONObject tempObject;
        Object o;
        for (TaskDetectionResult detectionResult : list) {
            String sensor = detectionResult.getSensor();
            if(!StringUtils.isEmpty(sensor)){
                tempObject = JSONUtil.parseObj(sensor);
                jsonObject = new JSONObject();
                jsonObject.set("pointName", detectionResult.getPointName());
                jsonObject.set("timestamp", DateUtil.parse(tempObject.getStr("timestamp")).getTime());
                Object temperature = tempObject.getObj(Detection.TEMPERATURE);
                if(temperature instanceof Number){
                    jsonObject.set("temperature", temperature);
                }else{
                    jsonObject.set("temperature", ((JSONObject)temperature).getDouble("value"));
                }

                Object humidity = tempObject.getObj(Detection.HUMIDITY);
                if(humidity instanceof Number){
                    jsonObject.set("humidity", humidity);
                }else{
                    jsonObject.set("humidity", ((JSONObject)humidity).getDouble("value"));
                }
                Object noise = tempObject.getObj(Detection.NOISE);
                if(noise instanceof Number){
                    jsonObject.set("noise", noise);
                }else{
                    jsonObject.set("noise", ((JSONObject)noise).getDouble("value"));
                }
                Object pm2p5 = tempObject.getObj(Detection.PM2P5);
                if(pm2p5 instanceof Number){
                    jsonObject.set("pm2p5", pm2p5);
                }else{
                    jsonObject.set("pm2p5", ((JSONObject)pm2p5).getDouble("value"));
                }
                Object smoke = tempObject.getObj(Detection.SMOKE);
                if(smoke instanceof Number){
                    jsonObject.set("smoke", smoke);
                }else{
                    jsonObject.set("smoke", ((JSONObject)smoke).getDouble("value"));
                }
                jsonArray.add(jsonObject);
            }
        }
        return jsonArray;
    }

    @Override
    public JSONArray getBackChargingPilePath(long taskId, int type, String pointName) {
        JSONArray pointArray = new JSONArray();
        JSONArray detectionArray = new JSONArray();
        JSONObject detectionObject;
        if (type == 1) {
            TaskInstance taskInstance = taskInstanceDao.getDetlById(taskId);
            TaskInfo taskInfo = taskInfoDao.getDetlById(taskInstance.getTaskId());
            long roomId = taskInfo.getRoomId();
            //判断对应的机房是否为常州这一类比较特殊的机房，如果不是，则直接就返回充电桩的技能即可
            JSONArray point2ChargingPilePathArr = pointTreeNodeService.getPoint2ChargingPilePath(roomId, pointName);

            if (point2ChargingPilePathArr==null || point2ChargingPilePathArr.size()==0) {
                pointArray.add(getBackChargingPileDetection());
                return pointArray;
            }

            DetectionInfo gatingDetectionInfo = new DetectionInfo();
            gatingDetectionInfo.setDetectionId("gating");
            gatingDetectionInfo.setRobotDetection("gating");
            JSONObject gatingObject;
            Map detectionMap;

            //point2ChargingPilePathArr.size()==1代表只含有一个充电桩技能
            //充电桩机房门
            //StrUtil.count(pointName, "-")==2 门-15-内开
            if (pointName.indexOf("门")>-1 && point2ChargingPilePathArr.size()==1 && StrUtil.count(pointName, "-")==2){
                //此处包含门，需要看是包含的哪个门，如果包含是充电桩机房的门，则直接执行开门，然后执行门内关
                String doorPoint = pointName.substring(0, pointName.length() - 3);
                //如果节点包含门，则将门直接打开，打开的时机为到达该点位之后，先执行开门，后执行门内关
                int begin = pointName.indexOf("-");
                int end = pointName.indexOf("-", begin+1);
                if (end==-1){
                    end = pointName.length();
                }
                long nodeRoomId = Long.parseLong(pointName.substring(begin+1, end));
                List pointInfos = pointInfoDao.list(nodeRoomId);

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外开", gatingDetectionInfo);
                pointArray.add(gatingObject);

                //添加外关
                //导航至外关  添加外关指令
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", taskExecuteCron.getPosture(pointInfos, doorPoint + "-内关"));
                pointArray.add(new JSONObject(detectionMap));

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-内关", gatingDetectionInfo);
                pointArray.add(gatingObject);

                detectionObject = new JSONObject();
                detectionObject.set("pointName", doorPoint + "-内关");
                detectionObject.set("detection_item", pointArray);
                detectionArray.add(detectionObject);
            }else if (pointName.indexOf("门")>-1 && point2ChargingPilePathArr.size()==1 && StrUtil.count(pointName, "-")==3){
                //充电桩冷通道门
                String doorPoint = pointName.substring(0, pointName.length() - 3);
                //如果节点包含门，则将门直接打开，先执行开门，后执行门外关
                int begin = pointName.indexOf("-");
                int end = pointName.indexOf("-", begin+1);
                if (end==-1){
                    end = pointName.length();
                }
                long nodeRoomId = Long.parseLong(pointName.substring(begin+1, end));
                List pointInfos = pointInfoDao.list(nodeRoomId);

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外开", gatingDetectionInfo);
                pointArray.add(gatingObject);

                //添加外关
                //导航至外关  添加外关指令
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", taskExecuteCron.getPosture(pointInfos, doorPoint + "-外关"));
                pointArray.add(new JSONObject(detectionMap));

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外关", gatingDetectionInfo);
                pointArray.add(gatingObject);

                detectionObject = new JSONObject();
                detectionObject.set("pointName", doorPoint + "-外关");
                detectionObject.set("detection_item", pointArray);
                detectionArray.add(detectionObject);
            }else if(pointName.indexOf("门")>-1){
                //非充电桩机房门和冷通道门，直接开门和门外关即可
                String doorPoint = pointName.substring(0, pointName.length() - 3);
                int begin = pointName.indexOf("-");
                int end = pointName.indexOf("-", begin+1);
                if (end==-1){
                    end = pointName.length();
                }
                long nodeRoomId = Long.parseLong(pointName.substring(begin+1, end));
                List pointInfos = pointInfoDao.list(nodeRoomId);

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外开", gatingDetectionInfo);
                pointArray.add(gatingObject);

                //添加外关
                //导航至外关  添加外关指令
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", taskExecuteCron.getPosture(pointInfos, doorPoint + "-外关"));
                pointArray.add(new JSONObject(detectionMap));

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外关", gatingDetectionInfo);
                pointArray.add(gatingObject);

                detectionObject = new JSONObject();
                detectionObject.set("pointName", doorPoint + "-外关");
                detectionObject.set("detection_item", pointArray);
                detectionArray.add(detectionObject);
            }

            for (int i = 0; i < point2ChargingPilePathArr.size(); i++) {
                String node = point2ChargingPilePathArr.getStr(i);
                //两个横线代表内通道门 一定是门内门然后门外关
                if (StrUtil.count(node, "-") == 2){
                    //门内开 门外关
                    String doorPoint = node;
                    //如果节点包含门，则将门直接打开，打开的时机为到达该点位之后，先执行开门，后执行门外关
                    int begin = node.indexOf("-");
                    int end = node.indexOf("-", begin+1);
                    if (end==-1){
                        end = node.length();
                    }
                    long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                    List pointInfos = pointInfoDao.list(nodeRoomId);

                    taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内开", gatingDetectionInfo, pointInfos, detectionArray);

                    //添加外关
                    //导航至外关  添加外关指令
                    taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外关", gatingDetectionInfo, pointInfos, detectionArray);
                }

                if (StrUtil.count(node, "-") == 1){
                    //当前是机房门，下一个一定是机房门或者无数据

                    //当前机房门 下一个机房门
                    if (point2ChargingPilePathArr.size()>=i+1 && point2ChargingPilePathArr.getStr(i+1).indexOf("门")>-1){
                        //门内开  门外关
                        String doorPoint = node;
                        //如果节点包含门，则将门直接打开，打开的时机为到达该点位之后，先执行开门，后执行门外关
                        int begin = node.indexOf("-");
                        int end = node.indexOf("-", begin+1);
                        if (end==-1){
                            end = node.length();
                        }
                        long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                        List pointInfos = pointInfoDao.list(nodeRoomId);

                        //门内开
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内开", gatingDetectionInfo, pointInfos, detectionArray);
                        //门外关
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外关", gatingDetectionInfo, pointInfos, detectionArray);
                    }else{
                        //当前机房门下一个无数据
                        //门外开 门内关
                        String doorPoint = node;
                        //如果节点包含门，则将门直接打开，打开的时机为到达该点位之后，先执行开门，后执行门外关
                        int begin = node.indexOf("-");
                        int end = node.indexOf("-", begin+1);
                        if (end==-1){
                            end = node.length();
                        }
                        long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                        List pointInfos = pointInfoDao.list(nodeRoomId);

                        //门外开
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外开", gatingDetectionInfo, pointInfos, detectionArray);
                        //门内关
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内关", gatingDetectionInfo, pointInfos, detectionArray);
                    }
                }
            }

            detectionArray.add(getBackChargingPileDetection());
        }
        return detectionArray;

    }

    /**
     * 获取充电桩检测项
     *
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/10/8 16:28
     */
    private JSONObject getBackChargingPileDetection() {
        JSONObject detectionObject = new JSONObject();
        detectionObject.set("action", "back_charging_pile");
        JSONArray detectionArray = new JSONArray();
        detectionArray.add(detectionObject);
        JSONObject pointObject = new JSONObject();
        pointObject.set("point_name", "cdz");
        pointObject.set("detection_item", detectionArray);
        return pointObject;
    }

    @Override
    public JSONArray getBackToPointNamePath(long taskId, int type, String pointName) {
        JSONArray pointArray = new JSONArray();
        JSONArray detectionFirstArray = new JSONArray();
        JSONArray detectionArray = new JSONArray();
        JSONObject detectionObject;
        if (type == 1) {
            TaskInstance taskInstance = taskInstanceDao.getDetlById(taskId);
            TaskInfo taskInfo = taskInfoDao.getDetlById(taskInstance.getTaskId());
            long roomId = taskInfo.getRoomId();
            //判断对应的机房是否为常州这一类比较特殊的机房，如果不是，则直接就返回充电桩的技能即可
            JSONArray chargingPilePath2PointArr = pointTreeNodeService.getChargingPilePath2Point(roomId, pointName);

            if (chargingPilePath2PointArr==null || chargingPilePath2PointArr.size()==0) {
                return pointArray;
            }

            DetectionInfo gatingDetectionInfo = new DetectionInfo();
            gatingDetectionInfo.setDetectionId("gating");
            gatingDetectionInfo.setRobotDetection("gating");
            JSONObject gatingObject;
            Map detectionMap;

            //point2ChargingPilePathArr.size()==1代表只含有一个充电桩技能
            //充电桩机房门
            //StrUtil.count(pointName, "-")==2 门-15-内开
            if (pointName.indexOf("门")>-1 && chargingPilePath2PointArr.size()==1 && StrUtil.count(pointName, "-")==2){
                //此处包含门，需要看是包含的哪个门，如果包含是充电桩机房的门，则直接执行开门->，前往目标点，->关门
                String doorPoint = pointName.substring(0, pointName.length() - 3);
                int begin = pointName.indexOf("-");
                int end = pointName.indexOf("-", begin+1);
                if (end==-1){
                    end = pointName.length();
                }
                long nodeRoomId = Long.parseLong(pointName.substring(begin+1, end));
                List pointInfos = pointInfoDao.list(nodeRoomId);

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外开", gatingDetectionInfo);
                pointArray.add(gatingObject);

                //添加外关
                //导航至外关  添加外关指令
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", taskExecuteCron.getPosture(pointInfos, pointName));
                pointArray.add(new JSONObject(detectionMap));

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-内关", gatingDetectionInfo);
                pointArray.add(gatingObject);

                detectionObject = new JSONObject();
                detectionObject.set("pointName", pointName);
                detectionObject.set("detection_item", pointArray);
                detectionFirstArray.add(detectionObject);
            }else if (pointName.indexOf("门")>-1 && chargingPilePath2PointArr.size()==1 && StrUtil.count(pointName, "-")==3){
                //充电桩冷通道门
                String doorPoint = pointName.substring(0, pointName.length() - 3);
                //执行 开门 ->导航至目标点 ->关门
                int begin = pointName.indexOf("-");
                int end = pointName.indexOf("-", begin+1);
                if (end==-1){
                    end = pointName.length();
                }
                long nodeRoomId = Long.parseLong(pointName.substring(begin+1, end));
                List pointInfos = pointInfoDao.list(nodeRoomId);

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外开", gatingDetectionInfo);
                pointArray.add(gatingObject);

                //添加外关
                //导航至外关  添加外关指令
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", taskExecuteCron.getPosture(pointInfos, pointName));
                pointArray.add(new JSONObject(detectionMap));

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外关", gatingDetectionInfo);
                pointArray.add(gatingObject);

                detectionObject = new JSONObject();
                detectionObject.set("pointName", pointName);
                detectionObject.set("detection_item", pointArray);
                detectionFirstArray.add(detectionObject);
            }else if(pointName.indexOf("门")>-1){
                //非充电桩机房门和冷通道门，直接开门->导航至目标点 ->关门
                String doorPoint = pointName.substring(0, pointName.length() - 3);
                int begin = pointName.indexOf("-");
                int end = pointName.indexOf("-", begin+1);
                if (end==-1){
                    end = pointName.length();
                }
                long nodeRoomId = Long.parseLong(pointName.substring(begin+1, end));
                List pointInfos = pointInfoDao.list(nodeRoomId);

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外开", gatingDetectionInfo);
                pointArray.add(gatingObject);

                //添加外关
                //导航至外关  添加外关指令
                detectionMap = new LinkedHashMap();
                detectionMap.put("action", "navigation");
                detectionMap.put("param", taskExecuteCron.getPosture(pointInfos, pointName));
                pointArray.add(new JSONObject(detectionMap));

                gatingObject = taskExecuteCron.addGatingDetection(nodeRoomId, doorPoint + "-外关", gatingDetectionInfo);
                pointArray.add(gatingObject);

                detectionObject = new JSONObject();
                detectionObject.set("pointName", pointName);
                detectionObject.set("detection_item", pointArray);
                detectionFirstArray.add(detectionObject);
            }

            for (int i = 0; i < chargingPilePath2PointArr.size(); i++) {
                String str = chargingPilePath2PointArr.getStr(i);
                //在for的上边代码中，已经将带有内开外开的门的处理添加进去了这里不应该再有相关的逻辑
                if (str.endsWith("内开") || str.endsWith("外开") || str.endsWith("外关") || str.endsWith("-内关")){
                    chargingPilePath2PointArr.remove(i);
                    i--;
                }
            }

            for (int i = 0; i < chargingPilePath2PointArr.size(); i++) {
                String node = chargingPilePath2PointArr.getStr(i);
                //两个横线代表冷通道门 因为是从充电桩到点位的规划 一定是门外开->门内关
                if (StrUtil.count(node, "-") == 2){
                    String doorPoint = node;
                    //如果节点包含门，则将门直接打开，打开的时机为到达该点位之后，先执行开门，后执行门外关
                    int begin = node.indexOf("-");
                    int end = node.indexOf("-", begin+1);
                    if (end==-1){
                        end = node.length();
                    }
                    long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                    List pointInfos = pointInfoDao.list(nodeRoomId);
                    taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外开", gatingDetectionInfo, pointInfos, detectionArray);
                    taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内关", gatingDetectionInfo, pointInfos, detectionArray);
                }

                if (StrUtil.count(node, "-") == 1){
                    //当前为机房门，下一个如果为机房门，则认为是充电桩机房门 执行 门内开->门外关
                    if (chargingPilePath2PointArr.size()>i+1 && chargingPilePath2PointArr.getStr(i+1).indexOf("门")>-1 && StrUtil.count(chargingPilePath2PointArr.getStr(i+1), "-")==1){
                        //门内开  门外关
                        String doorPoint = node;
                        int begin = node.indexOf("-");
                        int end = node.indexOf("-", begin+1);
                        if (end==-1){
                            end = node.length();
                        }
                        long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                        List pointInfos = pointInfoDao.list(nodeRoomId);

                        //门内开
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内开", gatingDetectionInfo, pointInfos, detectionArray);
                        //门外关
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外关", gatingDetectionInfo, pointInfos, detectionArray);
                    }else if (chargingPilePath2PointArr.size()>i+1 && chargingPilePath2PointArr.getStr(i+1).indexOf("门")>-1 && StrUtil.count(chargingPilePath2PointArr.getStr(i+1), "-")==2){
                        //当前为机房门，下一个如果为机柜门，则认为是非充电桩机房门 执行 门外开->门内关
                        String doorPoint = node;
                        //如果节点包含门，则将门直接打开，打开的时机为到达该点位之后，先执行开门，后执行门外关
                        int begin = node.indexOf("-");
                        int end = node.indexOf("-", begin+1);
                        if (end==-1){
                            end = node.length();
                        }
                        long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                        List pointInfos = pointInfoDao.list(nodeRoomId);

                        //门内开
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外开", gatingDetectionInfo, pointInfos, detectionArray);
                        //门外关
                        taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内关", gatingDetectionInfo, pointInfos, detectionArray);
                    }else{
                        //此时为充电桩机房门
                        if (chargingPilePath2PointArr.size() == 1){
                            String doorPoint = node;
                            int begin = node.indexOf("-");
                            int end = node.indexOf("-", begin+1);
                            if (end==-1){
                                end = node.length();
                            }
                            long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                            List pointInfos = pointInfoDao.list(nodeRoomId);

                            //门外开
                            taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内开", gatingDetectionInfo, pointInfos, detectionArray);
                            //门内关
                            taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外关", gatingDetectionInfo, pointInfos, detectionArray);
                        }else if (chargingPilePath2PointArr.size() == 2){
                            String doorPoint = node;
                            int begin = node.indexOf("-");
                            int end = node.indexOf("-", begin+1);
                            if (end==-1){
                                end = node.length();
                            }
                            long nodeRoomId = Long.parseLong(node.substring(begin+1, end));
                            List pointInfos = pointInfoDao.list(nodeRoomId);

                            //门外开
                            taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-外开", gatingDetectionInfo, pointInfos, detectionArray);
                            //门内关
                            taskExecuteCron.addNavAndGating(nodeRoomId,doorPoint + "-内关", gatingDetectionInfo, pointInfos, detectionArray);
                        }
                    }
                }
            }

            for (int i = 0; i < detectionFirstArray.size(); i++) {
                detectionArray.add(detectionFirstArray.getJSONObject(i));
            }

        }
        return detectionArray;
    }


    @Override
    public JSONArray getRoomTaskDetections(JSONObject paramObject) {
        if (!paramObject.containsKey("roomId")){
            throw new RuntimeException("机房id不能为空，请检查传入的数据");
        }
        long roomId = paramObject.getLong("roomId");
        if (roomId<=0){
            throw new RuntimeException("机房id应大于0，请检查传入的数据");
        }

        JSONArray taskLiat_json = new JSONArray();
        JSONObject jsonObject = new JSONObject();

        List<TaskInfo> taskInfos = taskInfoDao.getByRoomId(roomId);
        for (TaskInfo taskInfo : taskInfos) {
            jsonObject.set("taskId",taskInfo.getTaskId());
            jsonObject.set("inspectTypeId",taskInfo.getInspectTypeId());
            jsonObject.set("inspectTypeName",taskInfo.getInspectTypeName());
            jsonObject.set("robotId",taskInfo.getRobotId());
            jsonObject.set("robotName",taskInfo.getRobotName());
            jsonObject.set("roomId",taskInfo.getRoomId());
            jsonObject.set("roomName",taskInfo.getRoomName());
            jsonObject.set("execTime",taskInfo.getExecTime());
            jsonObject.set("inUse",taskInfo.getInUse());
            //获取detectionOrderList

            //
            taskLiat_json.add(jsonObject);
        }

        //把taskLiat_json变成taskList

        //
        List<DetectionParam> detectionInfoList = detectionParamService.list(roomId);

        //把detectionInfoList和taskList一块返回前端



        return taskLiat_json;
    }

    @Override
    public PageBean getRoomInspectionResume(int parkId, JSONObject paramObject) {
        //山东移动默认机房ID为1
        Long roomId = paramObject.getLong("roomId", 1L);
        String taskName = paramObject.getStr("taskName", "");
        String startTime = paramObject.getStr("startTime","");
        String endTime = paramObject.getStr("endTime","");
        if (!paramObject.containsKey("pageNum")){
            throw new RuntimeException("pageNum不能为空");
        }
        int pageNum = paramObject.getInt("pageNum");
        if (pageNum <= 0){
            throw new RuntimeException("pageNum应大于0");
        }

        if (!paramObject.containsKey("pageSize")){
            throw new RuntimeException("pageSize不能为空");
        }
        int pageSize = paramObject.getInt("pageSize");
        if (pageSize <= 0){
            throw new RuntimeException("pageSize应大于0");
        }

        String startTimeStart = "";
        String startTimeEnd = "";
        String endTimeStart = "";
        String endTimeEnd = "";

        if (!StringUtils.isEmpty(startTime)){
            if (startTime.length() != 39){
                throw new RuntimeException("巡检开始时间应符合yyyy-MM-dd HH:mm:ss-yyyy-MM-dd HH:mm:ss标准");
            }
            startTimeStart = startTime.substring(0,19);
            startTimeEnd = startTime.substring(20);
        }

        if (!StringUtils.isEmpty(endTime)){
            if (endTime.length() != 39){
                throw new RuntimeException("巡检结束时间应符合yyyy-MM-dd HH:mm:ss-yyyy-MM-dd HH:mm:ss标准");
            }
            endTimeStart = endTime.substring(0,19);
            endTimeEnd = endTime.substring(20);
        }

        //匹配巡检任务id
        String inspectTypeIdIn = "";
        if (!StringUtils.isEmpty(taskName)){
            JSONObject roomParamObject = null;
            JSONArray inspectTypeArray = null;
            roomParamObject = roomParamService.getRoomParam(roomId);
            inspectTypeArray = roomParamObject.getJSONObject("inspect_setting").getJSONArray("inspect_type");
            for (int i = 0; i < inspectTypeArray.size(); i++) {
                long inspectTypeId = inspectTypeArray.getJSONObject(i).getLong("inspect_type_id");
                String inspectTypeName = inspectTypeArray.getJSONObject(i).getStr("inspect_type_name");
               if (inspectTypeName.indexOf(taskName) > -1){
                   inspectTypeIdIn += "'"+inspectTypeId+"',";
               }
            }
        }

        if (!StringUtils.isEmpty(inspectTypeIdIn)){
            inspectTypeIdIn = inspectTypeIdIn.substring(0, inspectTypeIdIn.length() - 1);
        }

        PageBean pageBean = taskInstanceDao.list(parkId, roomId, startTimeStart, startTimeEnd, endTimeStart, endTimeEnd, inspectTypeIdIn, pageSize, pageNum);
        List<TaskInstance> taskInstances = pageBean.getContentList();

        JSONObject roomParamObject = null;
        JSONArray inspectTypeArray = null;
        for (TaskInstance taskInstance : taskInstances) {
            long inspectTypeId = taskInstance.getInspectTypeId();
            roomParamObject = roomParamService.getRoomParam(taskInstance.getRoomId());
            inspectTypeArray = roomParamObject.getJSONObject("inspect_setting").getJSONArray("inspect_type");
            for (int i = 0; i < inspectTypeArray.size(); i++) {
                long fileInspectTypeId = inspectTypeArray.getJSONObject(i).getLong("inspect_type_id");
                if (inspectTypeId == fileInspectTypeId) {
                    String inspectTypeName = inspectTypeArray.getJSONObject(i).getStr("inspect_type_name");
                    taskInstance.setInspectTypeName(inspectTypeName);
                }
            }
        }

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (TaskInstance taskInstance : taskInstances) {
            jsonObject = new JSONObject();
            jsonObject.set("instanceId", taskInstance.getInstanceId());
            jsonObject.set("taskName", taskInstance.getInspectTypeName());
            jsonObject.set("startTime", taskInstance.getStartTime());
            jsonObject.set("endTime", taskInstance.getEndTime());
            jsonObject.set("inspectionCycle", getChineseCycleType(taskInstance.getCycleType()));
            jsonObject.set("dataSource", "机器人巡检");
            jsonArray.add(jsonObject);
        }
        pageBean.setContentList(jsonArray);
        return pageBean;
    }

    /**
     * 获取循环周期中文
     * @param cycleType
     * @return java.lang.String
     * @author kliu
     * @date 2022/12/5 14:55
     */
    private String getChineseCycleType(String cycleType){
        if (StringUtils.isEmpty(cycleType)){
            return "临时任务";
        }else{
            //workday:工作日 everyday:每天 week:每周 twoweek:每两周 month:每月
            if ("workday".equals(cycleType)){
                return "工作日";
            }else if ("everyday".equals(cycleType)){
                return "每天";
            }else if ("week".equals(cycleType)){
                return "每周";
            }else if ("twoweek".equals(cycleType)){
                return "每两周";
            }else if ("month".equals(cycleType)){
                return "每月";
            }else{
                return "-";
            }
        }
    }
}
