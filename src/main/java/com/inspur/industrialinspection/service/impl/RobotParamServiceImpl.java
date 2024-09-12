package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.ParkInfoDao;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.dao.TaskDao;
import com.inspur.industrialinspection.entity.ParkInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.service.RobotParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @projectName: platform_app_service
 * @package: com.inspur.industrial_robot.service.impl
 * @className: RoomParamServiceImpl
 * @author: kliu
 * @description: 机房参数服务
 * @date: 2022/4/8 9:54
 * @version: 1.0
 */
@Service
public class RobotParamServiceImpl implements RobotParamService {

    @Value("${param.parent.path}")
    private String paramParentPath;
    @Value("${param.filename.robot}")
    private String robotParamFilename;
    @Value("${param.filename.camera.indoor_norm_cam}")
    private String indoorNormCamFileName;
    @Value("${param.filename.camera.infrared_cam}")
    private String infraredCamFileName;

    private volatile static ConcurrentHashMap<Long, String> robotParamHm = new ConcurrentHashMap<Long, String>();

    @Autowired
    TaskDao taskDao;
    @Autowired
    RobotRoomDao robotRoomDao;
    @Autowired
    RoomInfoDao roomInfoDao;
    @Autowired
    ParkInfoDao parkInfoDao;

    @Override
    public JSONObject getRobotParam(long robotId) throws IOException {
        if (robotParamHm.containsKey(robotId)){
            return JSONUtil.parseObj(robotParamHm.get(robotId));
        }
        RoomInfo roomInfo = roomInfoDao.getDetlById(robotRoomDao.getRoomIdByRobotId(robotId));
        ParkInfo parkInfo = parkInfoDao.getDetlById(roomInfo.getParkId());
        String filePath = (paramParentPath+robotParamFilename).replace("{robotid}", robotId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());
        String robotParamStr = FileUtil.readUtf8String(filePath);
        if(StringUtils.isEmpty(robotParamStr)){
            return new JSONObject();
        }
        robotParamHm.put(robotId, robotParamStr);
        return JSONUtil.parseObj(robotParamStr);
    }

    @Override
    public void add(long robotId, String str) throws IOException {
        if (robotParamHm.containsKey(robotId)){
            robotParamHm.put(robotId, str);
        }
        RoomInfo roomInfo = roomInfoDao.getDetlById(robotRoomDao.getRoomIdByRobotId(robotId));
        ParkInfo parkInfo = parkInfoDao.getDetlById(roomInfo.getParkId());
        String filePath = (paramParentPath+robotParamFilename).replace("{robotid}", robotId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());
        FileUtil.writeUtf8String(str, filePath);
    }

    /**
     * 清除机器人参数缓存
     * @return void
     * @author kliu
     * @date 2022/7/12 19:43
     */
    @Override
    public void clearRobotParamCache() {
        robotParamHm.clear();
    }
}
