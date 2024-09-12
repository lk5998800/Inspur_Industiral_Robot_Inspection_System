package com.inspur.cron;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.code.AlongWorkStatus;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.AlongWork;
import com.inspur.industrialinspection.entity.PersonnelManagement;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.RobotParamService;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.util.*;

/**
 * 随工定时任务
 * @author kliu
 * @date 2022/6/13 11:16
 */
@SuppressWarnings("AlibabaRemoveCommentedCode")
@Component
@Slf4j
public class AlongWorkExecuteCron {
    @Autowired
    private AlongWorkDao alongWorkDao;
    @Autowired
    private AlongWorkPointInfoDao alongWorkPointInfoDao;
    @Autowired
    private TaskExecuteCron taskExecuteCron;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private CommonService commonService;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private PersonnelManagementDao personnelManagementDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private RobotParamService robotParamService;
    @Autowired
    private PointInfoDao pointInfoDao;

    /**
     * 随工定时任务
     * @return void
     * @author kliu
     * @date 2022/6/13 11:16
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void taskExecute() throws IOException {
        String nowDateStr = DateUtil.now().substring(0, 16);
        List<AlongWork> alongWorkList = alongWorkDao.getListByCron();
        for (AlongWork alongWork : alongWorkList) {
            //任务前5分钟前往门口待命点，所以提前5分钟下发任务
            String taskTime = alongWork.getTaskTime();
            DateTime taskTimeDate = DateUtil.parse(taskTime);
            Date date5MinuteBefore = DateUtil.offset(taskTimeDate, DateField.MINUTE, -5);
            String date5MinuteBeforeStr = DateUtil.format(date5MinuteBefore, "yyyy-MM-dd HH:mm");
            //任务前5分钟，则执行任务
            if (nowDateStr.equals(date5MinuteBeforeStr)) {
                execute(alongWork);
            }
        }
    }

    /**
     * 执行随工任务
     * @param alongWork
     * @return void
     * @author kliu
     * @date 2022/6/20 20:24
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    public void execute(AlongWork alongWork) throws IOException {
        TransactionStatus transactionStatus = null;
        JSONObject paramObject;
        //检测项  点位检测项
        Map detectionMap, pointActionMap;
        JSONArray detectionArray, pointActionArray = new JSONArray();
        //文件中检测项、阈值等数据
        JSONArray rowPointActionArray = new JSONArray();
        long id = alongWork.getId();
        long roomId = alongWork.getRoomId();
        String points = alongWork.getPoints();
        long userId = alongWork.getTaskUserId();

        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        detectionArray = new JSONArray();
        //所有位姿信息 一次查询所有，进行遍历，单条查询速度略慢
        List<PointInfo> pointInfos = alongWorkPointInfoDao.list(roomId);

        List<PointInfo> gatingPointInfos = pointInfoDao.list(roomId);

        String currentPointName = "ADMD";

        //到达待命点前的门控逻辑  ADMD
        taskExecuteCron.addGatingDetectionForFirstPointName(roomId, currentPointName, rowPointActionArray);
        //开始待命点生成逻辑
        rowPointActionArray.add(waitPointStart(userId, robotId, roomId, id));

        String[] split = points.split(",");
        //取当前点位作为随工位姿参数
        int i=1;
        int length = split.length;
        for (String pointName : split) {

            taskExecuteCron.addGatingDetectionForPointName2NewPointName(roomId, currentPointName, pointName, gatingPointInfos, rowPointActionArray);

            paramObject = taskExecuteCron.getPosture(pointInfos, pointName);

            detectionArray = new JSONArray();
            detectionMap = new LinkedHashMap();
            detectionMap.put("action", "navigation");
            detectionMap.put("param", paramObject);
            detectionArray.add(new JSONObject(detectionMap));

            detectionMap = new LinkedHashMap();
            paramObject = new JSONObject();
            paramObject.set("context", "已到达随工点位，请运维人员在机器人正前方摄像头范围内开始运维工作");
            detectionMap.put("action", "voice");
            detectionMap.put("param", paramObject);
            detectionArray.add(new JSONObject(detectionMap));

            detectionMap = new LinkedHashMap();
            detectionMap.put("action", "wait");
            detectionArray.add(new JSONObject(detectionMap));

            if (length == i){
                detectionMap = new LinkedHashMap();
                paramObject = new JSONObject();
                paramObject.set("context", "当前随工已全部完成，请跟随机器人离开机房");
                detectionMap.put("action", "voice");
                detectionMap.put("param", paramObject);
                detectionArray.add(new JSONObject(detectionMap));
            }

            pointActionMap = new LinkedHashMap();
            pointActionMap.put("point_name", pointName);
            pointActionMap.put("detection_item", detectionArray);
            rowPointActionArray.add(new JSONObject(pointActionMap));

            currentPointName = pointName;

            i++;
        }

        taskExecuteCron.addGatingDetectionForPointName2NewPointName(roomId, currentPointName, "ADMD", gatingPointInfos, rowPointActionArray);

        rowPointActionArray.add(waitPointEnd(pointInfos, roomId));

        //添加回桩指令
        taskExecuteCron.addBackChargingPile(rowPointActionArray, roomId);

        pointActionArray.add(rowPointActionArray);

        transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            alongWork.setStatus(AlongWorkStatus.WAIT);
            alongWorkDao.update(alongWork);
            //显示提交事务，防止查询的时候查询不到
            dataSourceTransactionManager.commit(transactionStatus);
        }catch (Exception e){
            dataSourceTransactionManager.rollback(transactionStatus);
            throw new RuntimeException(e.getMessage());
        }

        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", DateUtil.now());
        map.put("task_id", id);
        map.put("robot_id", robotId);
        map.put("run_mode", "casual_work");
        map.put("point_action_list", pointActionArray);

        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        String json = new JSONObject(returnMap).toString();
        log.info("随工任务json:"+json);
        String compress = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
        mqttPushClient.publish("industrial_robot_issued/"+robotId, compress);
    }

    /**
     * 开始待命点生成逻辑
     * @param userId
     * @param robotId
     * @param roomId
     * @param taskId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/16 16:47
     */
    @SuppressWarnings("AlibabaRemoveCommentedCode")
    private JSONObject waitPointStart(long userId, long robotId, long roomId, long taskId) throws IOException {
        PersonnelManagement personnelManagement = personnelManagementDao.getDetlById(userId);
        //该版本取ADMD点位作为待命点
        List<PointInfo> list = new ArrayList<>();
        PointInfo pointInfo = new PointInfo();
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName("ADMD");
        pointInfo  = pointInfoDao.getDetlById(pointInfo);
        list.add(pointInfo);
        JSONObject paramObject = taskExecuteCron.getPosture(list, "ADMD");

        JSONObject facialFeatureObject = JSONUtil.parseObj(personnelManagement.getPersonnelFacialFeature());
        Object facialFeature = facialFeatureObject.get("face_feature");

        JSONArray detectionArray = new JSONArray();

        JSONObject robotParamObject = robotParamService.getRobotParam(robotId);
        int streamCameraNo = robotParamObject.getInt("stream_camera_no");
        String srsRtmpServerUrl = robotParamObject.getStr("srs_rtmp_server_url")+"/"+robotId+"-"+taskId;

        Map detectionMap = new LinkedHashMap();
        detectionMap.put("action", "navigation");
        detectionMap.put("param", paramObject);
        detectionArray.add(new JSONObject(detectionMap));

        detectionMap = new LinkedHashMap();
        detectionMap.put("action", "init_lifter");
        detectionArray.add(new JSONObject(detectionMap));

        JSONObject param = new JSONObject();
        param.set("direction", "up");
        param.set("distance", 100);
        detectionMap.put("action", "lifter");
        detectionMap.put("param", param);
        detectionArray.add(new JSONObject(detectionMap));

        detectionMap = new LinkedHashMap();
        paramObject = new JSONObject();
        paramObject.set("wait_time", 20*60);
        paramObject.set("facial_feature", facialFeature);
        paramObject.set("similarity", 0.35);
        detectionMap.put("action", "wait_to_face");
        detectionMap.put("param", paramObject);
        detectionArray.add(new JSONObject(detectionMap));

        detectionMap = new LinkedHashMap();
        paramObject = new JSONObject();
        paramObject.set("srs_rtmp_server_url", srsRtmpServerUrl);
        paramObject.set("camera_no", streamCameraNo);
        detectionMap.put("action", "alongwork_video");
        detectionMap.put("param", paramObject);
        detectionArray.add(new JSONObject(detectionMap));

        detectionMap = new LinkedHashMap();
        paramObject = new JSONObject();
        paramObject.set("context", "请跟随机器人前往随工点");
        detectionMap.put("action", "voice");
        detectionMap.put("param", paramObject);
        detectionArray.add(new JSONObject(detectionMap));

        Map pointActionMap = new LinkedHashMap();
        pointActionMap.put("point_name", "到达待命点");
        pointActionMap.put("detection_item", detectionArray);
        return new JSONObject(pointActionMap);
    }
    /**
     * 开始待命点生成逻辑
     * @param pointInfos
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/20 20:29
     */
    private JSONObject waitPointEnd(List<PointInfo> pointInfos, long roomId){
        JSONArray detectionArray = new JSONArray();
        //该版本取ADMD点位作为待命点
        List<PointInfo> list = new ArrayList<>();
        PointInfo pointInfo = new PointInfo();
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName("ADMD");
        pointInfo  = pointInfoDao.getDetlById(pointInfo);
        list.add(pointInfo);
        JSONObject paramObject = taskExecuteCron.getPosture(list, "ADMD");

        Map detectionMap = new LinkedHashMap();
        detectionMap.put("action", "navigation");
        detectionMap.put("param", paramObject);
        detectionArray.add(new JSONObject(detectionMap));

        detectionMap = new LinkedHashMap();
        paramObject = new JSONObject();
        paramObject.set("context", "当前随工已全部完成，请您离开机房，祝您工作愉快");
        detectionMap.put("action", "voice");
        detectionMap.put("param", paramObject);
        detectionArray.add(new JSONObject(detectionMap));

        detectionMap = new LinkedHashMap();
        paramObject = new JSONObject();
        paramObject.set("wait_time", 1*60);
        detectionMap.put("action", "wait_continue");
        detectionMap.put("param", paramObject);
        detectionArray.add(new JSONObject(detectionMap));

        detectionMap = new LinkedHashMap();
        detectionMap.put("action", "init_lifter");
        detectionArray.add(new JSONObject(detectionMap));

        Map pointActionMap = new LinkedHashMap();
        pointActionMap.put("point_name", "随工任务结束");
        pointActionMap.put("detection_item", detectionArray);
        return new JSONObject(pointActionMap);
    }
}
