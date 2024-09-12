package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.FireExtinguisherParaDao;
import com.inspur.industrialinspection.dao.PointInfoDao;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.entity.FireExtinguisherPara;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.service.PointInfoService;
import com.inspur.industrialinspection.service.RobotParamService;
import com.inspur.industrialinspection.service.RobotSysSetService;
import com.inspur.mqtt.MqttPushClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 点位信息服务
 * @author kliu
 * @date 2022/5/25 8:55
 */
@Service
public class PointInfoServiceImpl implements PointInfoService {
    private volatile static ConcurrentHashMap<String, PointInfo> points = new ConcurrentHashMap();
    private volatile static ConcurrentHashMap<String, FireExtinguisherPara> fireExtinguisherParas = new ConcurrentHashMap();

    @Autowired
    private PointInfoDao pointInfoDao;
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private RobotParamService robotParamService;
    @Autowired
    private RobotSysSetService robotSysSetService;
    @Autowired
    private FireExtinguisherParaDao fireExtinguisherParaDao;

    /**
     * 获取点位信息
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/5/25 8:55
     */
    @Override
    public List list(long roomId) throws RuntimeException, IOException {
        if(!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }
        List<PointInfo> list = pointInfoDao.list(roomId);
        BigDecimal zeroDecimal = new BigDecimal(0);

        for (PointInfo pointInfo : list) {
            BigDecimal locationX = pointInfo.getLocationX();
            boolean postureFlag = false;
            if(locationX.doubleValue() != zeroDecimal.doubleValue()){
                postureFlag = true;
            }
            pointInfo.setPostureFlag(postureFlag);
        }
        PointInfo pointInfo = new PointInfo();
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName("dmd");

        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);

        JSONObject robotParamObejct = robotParamService.getRobotParam(robotId);
        if (robotParamObejct.containsKey(ParaKey.WAIT_POINT_POSE)){
            pointInfo.setPostureFlag(true);
        }else{
            pointInfo.setPostureFlag(false);
        }
        list.add(pointInfo);
        return list;
    }

    /**
     * 关联位姿，校验机房id，获取实时位姿并保存
     * @param pointInfo
     * @author kliu
     * @date 2022/4/19 11:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void associatedPosture(PointInfo pointInfo) throws RuntimeException, InterruptedException {
        long roomId = pointInfo.getRoomId();
        if(!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }

        String pointName = pointInfo.getPointName();
        if(StringUtils.isEmpty(pointName)){
            throw new RuntimeException("检测点名称不能为空");
        }
        pointInfo = getRealTimePosture(roomId);
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName(pointName);
        if(pointInfoDao.checkExist(pointInfo)){
            pointInfoDao.update(pointInfo);
        }else{
            pointInfoDao.add(pointInfo);
        }

        if (pointName.indexOf("灭火器") > -1){
            //下发灭火器参数获取
            associatedFireExtinguisherPara(roomId, pointName, robotRoomDao.getRobotIdByRoomId(roomId));
        }
    }

    /**
     * 关联位姿，手动关联
     * @param pointInfo
     * @author kliu
     * @date 2022/4/19 11:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void associatedPostureManual(PointInfo pointInfo) throws RuntimeException {
        long roomId = pointInfo.getRoomId();
        if(!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }

        String pointName = pointInfo.getPointName();
        if(StringUtils.isEmpty(pointName)){
            throw new RuntimeException("检测点名称不能为空");
        }
        if(pointInfoDao.checkExist(pointInfo)){
            pointInfoDao.update(pointInfo);
        }else{
            pointInfoDao.add(pointInfo);
        }
    }

    /**
     * 关联待命点
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/5/25 8:56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void associatedWaitPoint(long roomId) throws RuntimeException, InterruptedException, IOException {
        if(!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }

        PointInfo pointInfo = getRealTimePosture(roomId);
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);

        JSONObject robotParamObject = robotParamService.getRobotParam(robotId);
        JSONObject waitPointPoseObject = new JSONObject();
        JSONObject orientationObject = new JSONObject();
        JSONObject positionObject = new JSONObject();
        positionObject.set("x", pointInfo.getLocationX());
        positionObject.set("y", pointInfo.getLocationY());
        positionObject.set("z", pointInfo.getLocationZ());

        orientationObject.set("w", pointInfo.getOrientationW());
        orientationObject.set("x", pointInfo.getOrientationX());
        orientationObject.set("y", pointInfo.getOrientationY());
        orientationObject.set("z", pointInfo.getOrientationZ());

        waitPointPoseObject.set("orientation", orientationObject);
        waitPointPoseObject.set("position", positionObject);
        robotParamObject.set("wait_point_pose", waitPointPoseObject);

        robotParamService.add(robotId, robotParamObject.toString());

        robotSysSetService.issued(robotId);

        robotParamObject = null;
    }

    /**
     * 获取检测点位姿，通过mqtt下发指令，机器人manage端收到请求后，请求底盘获取位姿，将获取到的位姿数据通过http返回结果，用map临时存储
     * @return com.inspur.industrial_robot.entity.PointInfo
     * @author kliu
     * @date 2022/4/12 10:50
     */
    @Override
    public PointInfo getRealTimePosture(long roomId) throws InterruptedException {
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);

        String uuid = IdUtil.simpleUUID();
        points.put(uuid, new PointInfo());

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("uuid", uuid);
        jsonObject.set("robotId", robotId);

        //下发获取位姿指令到mqtt
        mqttPushClient.publish("industrial_robot_getposture/"+robotId,jsonObject.toString());

        PointInfo pointInfo = new PointInfo();

        int i=0;
        int whileCount = 50;
        //10s无结果返回则报错
        while (i < whileCount){
            pointInfo = points.get(uuid);
            if(pointInfo.getLocationX() == null){
                i++;
                Thread.sleep(200);
            }else{
                points.remove(uuid);
                break;
            }
        }

        points.remove(uuid);
        if(pointInfo.getLocationX() == null){
            throw new RuntimeException("获取位姿失败，请稍后再次尝试");
        }

        return pointInfo;
    }


    /**
     * 关联灭火器参数
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/11/25 11:32
     */
    private void associatedFireExtinguisherPara(long roomId, String pointName, long robotId) throws InterruptedException {
        String uuid = IdUtil.simpleUUID();
        fireExtinguisherParas.put(uuid, new FireExtinguisherPara());

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("uuid", uuid);
        jsonObject.set("robotId", robotId);

        //下发获取位姿指令到mqtt
        mqttPushClient.publish("industrial_robot/fireextinguisherpara/"+robotId+"/cloudserver",jsonObject.toString());

        FireExtinguisherPara fireExtinguisherPara = null;

        int i=0;
        int whileCount = 50;
        //10s无结果返回则报错
        while (i < whileCount){
            fireExtinguisherPara = fireExtinguisherParas.get(uuid);
            if(StringUtils.isEmpty(fireExtinguisherPara.getFireExitinguisherPath())){
                i++;
                Thread.sleep(200);
            }else{
                fireExtinguisherParas.remove(uuid);
                break;
            }
        }

        fireExtinguisherParas.remove(uuid);
        fireExtinguisherPara.setRoomId(roomId);
        fireExtinguisherPara.setPointName(pointName);
        if(StringUtils.isEmpty(fireExtinguisherPara.getFireExitinguisherPath())){
            throw new RuntimeException("关联灭火器特征信息失败，请重新关联");
        }
        if (fireExtinguisherParaDao.checkExist(roomId, pointName)){
            fireExtinguisherParaDao.update(fireExtinguisherPara);
        }else{
            fireExtinguisherParaDao.add(fireExtinguisherPara);
        }
    }

    /**
     * 接收位姿
     * @param json
     * @return void
     * @author kliu
     * @date 2022/5/25 8:56
     */
    @Override
    public void receivePosture(String json){
        JSONObject jsonObject = JSONUtil.parseObj(json);
        String uuid = jsonObject.getStr("uuid");
        String pointInfoStr = jsonObject.getStr("pointInfo");
        PointInfo pointInfo = com.alibaba.fastjson.JSONObject.parseObject(pointInfoStr, PointInfo.class);
        if(points.containsKey(uuid)){
            points.put(uuid, pointInfo);
        }
    }

    @Override
    public void receiveFireExtinguisherPara(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        String uuid = jsonObject.getStr("uuid");
        String fireExitinguisherPath = jsonObject.getStr("fire_exitinguisher_path");
        JSONArray fireExitinguisherPosArray = jsonObject.getJSONArray("fire_exitinguisher_pos");
        Integer fireExitinguisherNum = jsonObject.getInt("fire_exitinguisher_num");
        if (fireExitinguisherPosArray.size() == 0){
            throw new RuntimeException("接收灭火器特征数据错误，无特征数据");
        }
        FireExtinguisherPara fireExtinguisherPara = new FireExtinguisherPara();
        fireExtinguisherPara.setFireExitinguisherPath(fireExitinguisherPath);
        fireExtinguisherPara.setFireExitinguisherPos(fireExitinguisherPosArray.toString());
        fireExtinguisherPara.setFireExitinguisherNum(fireExitinguisherNum);
        if(fireExtinguisherParas.containsKey(uuid)){
            fireExtinguisherParas.put(uuid, fireExtinguisherPara);
        }
    }
}
