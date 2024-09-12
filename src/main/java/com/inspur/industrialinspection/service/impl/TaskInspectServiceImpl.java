package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.dao.BuildingInfoDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.dao.TaskInspectDao;
import com.inspur.industrialinspection.dao.UniappPhotoDao;
import com.inspur.industrialinspection.entity.BuildingInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.entity.TaskInspect;
import com.inspur.industrialinspection.entity.UniappPhoto;
import com.inspur.industrialinspection.service.TaskInspectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 巡检任务服务实现
 * @author wangzhaodi
 * @date 2022/11/16 14:32
 */
@Service
public class TaskInspectServiceImpl implements TaskInspectService {
    @Autowired
    private TaskInspectDao taskInspectDao;
    @Autowired
    private UniappPhotoDao uniappPhotoDao;
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private BuildingInfoDao buildingInfoDao;

    @Override
    public TaskInspect getByRoomName(String roomName) {
        return taskInspectDao.getByRoomName(roomName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endTask(TaskInspect taskInspect) {
        taskInspect.setEndTime(DateUtil.now());
        taskInspectDao.endTask(taskInspect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTask(TaskInspect taskInspect) {
        taskInspect.setStartTime(DateUtil.now());
        taskInspectDao.addTask(taskInspect);
    }

    @Override
    public JSONObject getPhoneInspectTasks(int parkId, JSONObject jsonObject) {
        if (!jsonObject.containsKey("startTime")){
            throw new RuntimeException("开始时间不能为空，请检查传入的数据");
        }
        if (!jsonObject.containsKey("endTime")){
            throw new RuntimeException("结束时间不能为空，请检查传入的数据");
        }

        String startTime = jsonObject.getStr("startTime");
        String endTime = jsonObject.getStr("endTime");

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

        List list = taskInspectDao.list(parkId, startTime, endTime);
        JSONObject returnObject = new JSONObject();
        returnObject.set("taskList",list);
        return returnObject;
    }

    @Override
    public JSONObject getPhoneInspectDetl(long parkId, JSONObject jsonObject) {
        String buildingName = "-";
        JSONArray jsonArray = new JSONArray();
        Long taskInspectId = jsonObject.getLong("taskInspectId", 0L);
        if (taskInspectId==0){
            throw new RuntimeException("taskInspectId应存在且大于0，请检查传入的数据");
        }
        TaskInspect taskInspect = taskInspectDao.getDetlById(taskInspectId);
        if (taskInspect == null){
            throw new RuntimeException("传入的任务id不存在，请检查传入的数据");
        }
        String roomName = taskInspect.getRoomName();
        RoomInfo roomInfo = roomInfoDao.getByRoomNameAndParkId(roomName, parkId);
        if (roomInfo != null){
            long buildingId = roomInfo.getBuildingId();
            BuildingInfo buildingInfo = buildingInfoDao.getDetlById(buildingId);
            buildingName = buildingInfo.getBuildingName();
        }

        JSONObject returnObject = new JSONObject();
        returnObject.set("roomName", roomName);
        returnObject.set("buildingName", buildingName);

        JSONObject tempObject;

        List<UniappPhoto> uniappPhotos = uniappPhotoDao.list(taskInspectId);
        for (UniappPhoto uniappPhoto : uniappPhotos) {
            tempObject = new JSONObject();
            tempObject.set("imgUrl", uniappPhoto.getImgUrl());
            tempObject.set("pointName", uniappPhoto.getPointName());
            tempObject.set("time", uniappPhoto.getTime());
            jsonArray.add(tempObject);
        }
        returnObject.set("instanceDetlList", jsonArray);
        return returnObject;
    }
}
