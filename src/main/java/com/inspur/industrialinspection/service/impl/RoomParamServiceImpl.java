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
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.industrialinspection.service.RoomParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
public class RoomParamServiceImpl implements RoomParamService {

    @Value("${param.parent.path}")
    private String paramParentPath;
    @Value("${param.filename.robot}")
    private String robotParamFilename;
    @Value("${param.filename.camera.indoor_norm_cam}")
    private String indoorNormCamFileName;
    @Value("${param.filename.camera.infrared_cam}")
    private String infraredCamFileName;

    @Value("${param.filename.room}")
    private String paramRoomFilename;

    @Autowired
    private RequestService requestService;

    private volatile static ConcurrentHashMap<Long, String> roomParamHm = new ConcurrentHashMap<Long, String>();

    @Autowired
    TaskDao taskDao;
    @Autowired
    RobotRoomDao robotRoomDao;
    @Autowired
    RoomInfoDao roomInfoDao;
    @Autowired
    ParkInfoDao parkInfoDao;

    @Override
    public JSONObject getCamaraParam(long taskId){
        JSONArray xmlArray = new JSONArray();
        JSONObject xmlObject = new JSONObject();
        long roomId = taskDao.getRoomIdByTaskId(taskId);

        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);

        RoomInfo roomInfo = roomInfoDao.getDetlById(roomId);
        ParkInfo parkInfo = parkInfoDao.getDetlById(roomInfo.getParkId());

        String filePath = (paramParentPath+robotParamFilename).replace("{robotid}", robotId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());
        String indoorNormCamPath = (paramParentPath+indoorNormCamFileName).replace("{roomid}",roomId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());
        String infraredCamPath = (paramParentPath+infraredCamFileName).replace("{roomid}",roomId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());

        String robotParamStr = FileUtil.readUtf8String(filePath);
        JSONObject robotParamObject = JSONUtil.parseObj(robotParamStr);
        String indoorNormCamStr = FileUtil.readUtf8String(indoorNormCamPath);
        String infraredCamStr = FileUtil.readUtf8String(infraredCamPath);

        xmlObject.set("fileName", "indoor_norm_cam.xml");
        xmlObject.set("content", indoorNormCamStr);
        xmlArray.add(xmlObject);
        xmlObject = new JSONObject();
        xmlObject.set("fileName", "redlight_cam.xml");
        xmlObject.set("content", infraredCamStr);
        xmlArray.add(xmlObject);

        JSONObject resultObject = new JSONObject();
        resultObject.set("cameraMainParam", robotParamObject.getJSONArray("camera_param"));
        resultObject.set("cameraDetlParam", xmlArray);

        robotParamObject = null;
        xmlArray = null;
        return resultObject;
    }

    @Override
    public JSONObject getRoomParam(long roomId){
        if (roomParamHm.containsKey(roomId)){
            return JSONUtil.parseObj(roomParamHm.get(roomId));
        }
        RoomInfo roomInfo = roomInfoDao.getDetlById(roomId);
        ParkInfo parkInfo = parkInfoDao.getDetlById(roomInfo.getParkId());
        String filePath = (paramParentPath+paramRoomFilename).replace("{roomid}", roomId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());
        String roomParamStr = FileUtil.readUtf8String(filePath);
        if(StringUtils.isEmpty(roomParamStr)){
            return new JSONObject();
        }
        roomParamHm.put(roomId, roomParamStr);
        return JSONUtil.parseObj(roomParamStr);
    }

    @Override
    public void add(long roomId, String str){
        if (roomParamHm.containsKey(roomId)){
            roomParamHm.put(roomId, str);
        }
        RoomInfo roomInfo = roomInfoDao.getDetlById(roomId);
        ParkInfo parkInfo = parkInfoDao.getDetlById(roomInfo.getParkId());
        String filePath = (paramParentPath+paramRoomFilename).replace("{roomid}", roomId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());
        FileUtil.writeUtf8String(str, filePath);
    }

    /**
     * 清除机房参数缓存
     * @return void
     * @author kliu
     * @date 2022/7/12 19:43
     */
    @Override
    public void clearRoomParamCache() {
        roomParamHm.clear();
    }
}
