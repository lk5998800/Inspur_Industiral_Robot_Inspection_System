package com.inspur.cron;


import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.RobotStatusService;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.checkerframework.checker.units.qual.A;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author: LiTan
 * @description: 导览讲解定时任务
 * @date: 2022-11-01 15:22:32
 */
@SuppressWarnings("AlibabaRemoveCommentedCode")
@Component
@Slf4j
public class ExplainTaskExecuteCron {
    @Autowired
    private CommonService commonService;
    @Autowired
    private ExplainTaskDao explainTaskDao;
    @Autowired
    private RobotStatusService robotStatusService;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private ExplainPointSkillDao explainPointSkillDao;
    @Autowired
    private ExplainPointInfoDao explainPointInfoDao;


    /**
     * 导览讲解定时任务
     *
     * @throws IOException
     */
//    @Scheduled(cron = "0 0/1 * * * ? ")
//    public void explainTaskExecute() throws IOException {
//        String nowDateStr = DateUtil.parse(DateUtil.now()).toString("yyyy/MM/dd HH:mm");
//        List<ExplainTask> explainTasks = explainTaskDao.getListByCron();
//        for (ExplainTask explainTask : explainTasks) {
//            String taskTime = explainTask.getTaskTime();
//            if (nowDateStr.equals(taskTime)) {
//                ExecuteExplain(explainTask);
//            }
//        }
//    }

    /**
     * 组装任务执行
     *
     * @param explainTask
     */
    public void ExecuteExplain(ExplainTask explainTask) {
        JSONArray pointActionArray;
        Map map = new LinkedHashMap<String, Object>();
        map.put("task_time", explainTask.getTaskTime());
        map.put("task_id", explainTask.getId());
        map.put("robot_id", explainTask.getRoomId());
        map.put("run_mode", "explain");
        pointActionArray = executeExplainTaskInspection(explainTask.getRoomId(), explainTask.getPoints());
       JSONArray taskJsonArray = new JSONArray();
        taskJsonArray.add(pointActionArray);
        map.put("point_action_list", taskJsonArray);
        Map returnMap = new LinkedHashMap<String, Object>();
        returnMap.put("data", map);
        List<TaskInfo> byRoomId = taskInfoDao.getByRoomId(explainTask.getRoomId());
        if (byRoomId.size() > 0) {
            String json = new JSONObject(returnMap).toString();
            log.info(json);
            String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
            mqttPushClient.publish("industrial_robot_issued/" + byRoomId.get(0).getRobotId(), issuedStr);
            pointActionArray = null;
            returnMap = null;
            robotStatusService.setRobotTaskStatus(byRoomId.get(0).getRobotId(), true);
        }
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    private JSONArray executeExplainTaskInspection(long roomId, String points) {
        JSONArray detectionArray, pointActionArray = new JSONArray();
        JSONObject paramObject;
        // JSONArray  JSONArray
        Map detectionMap, pointActionMap;
        String[] splits = points.split(",");
        for (String split : splits) {
            ExplainPointSkill explainPointSkill = explainPointSkillDao.getExplainPointSkill(roomId, split);
            List<ExplainPointInfo> pointInfos = explainPointInfoDao.list(roomId);
            //导航  位姿数据获取及填充
            detectionArray = new JSONArray();
            detectionMap = new LinkedHashMap();
            detectionMap.put("action", "navigation");
            detectionMap.put("param", getExplainPointPosture(pointInfos, split));
            detectionArray.add(new JSONObject(detectionMap));
            //语音

            paramObject = new JSONObject();
            paramObject.set("context", explainPointSkill.getBroadcast());
            detectionMap.put("action", "voice");
            detectionMap.put("param", paramObject);
            detectionArray.add(new JSONObject(detectionMap));
            //等待
            paramObject = new JSONObject();
            paramObject.set("wait_time", explainPointSkill.getWaitingTime());
            detectionMap.put("action", "wait_continue");
            detectionMap.put("param", paramObject);
            detectionArray.add(new JSONObject(detectionMap));

            pointActionMap = new LinkedHashMap();
            pointActionMap.put("point_name", split);
            pointActionMap.put("detection_item", detectionArray);
            pointActionArray.add(new JSONObject(pointActionMap));
        }

        return pointActionArray;
    }

    /**
     * 组装导航点
     *
     * @param pointInfos
     * @param pointName
     * @return
     */
    public JSONObject getExplainPointPosture(List<ExplainPointInfo> pointInfos, String pointName) {
        JSONObject orientation = new JSONObject();
        JSONObject position = new JSONObject();
        JSONObject param = new JSONObject();

        boolean existData = false;
        for (ExplainPointInfo explainPointInfo : pointInfos) {
            if (explainPointInfo.getPointName().equals(pointName)) {
                existData = true;
                orientation.set("w", explainPointInfo.getOrientationW());
                orientation.set("x", explainPointInfo.getOrientationX());
                orientation.set("y", explainPointInfo.getOrientationY());
                orientation.set("z", explainPointInfo.getOrientationZ());
                position.set("x", explainPointInfo.getLocationX());
                position.set("y", explainPointInfo.getLocationY());
                position.set("z", explainPointInfo.getLocationZ());
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
}
