package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.industrialinspection.dao.DetectionInfoDao;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.entity.DetectionInfo;
import com.inspur.industrialinspection.service.DetectionInfoService;
import com.inspur.industrialinspection.service.RobotParamService;
import com.inspur.industrialinspection.service.RoomParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 检测项信息服务
 * @author kliu
 * @date 2022/5/25 8:44
 */
@Service
public class DetectionInfoServiceImpl implements DetectionInfoService {
    @Autowired
    private DetectionInfoDao detectionInfoDao;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    private RobotParamService robotParamService;
    @Autowired
    private RobotRoomDao robotRoomDao;

    /**
     * 获取检测项信息带阈值
     * @param roomId
     * @param combinationCode
     * @return java.util.List
     * @author kliu
     * @date 2022/5/25 8:44
     */
    @Override
    public List list(long roomId, String combinationCode) throws IOException {
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        JSONObject robotParamObject = robotParamService.getRobotParam(robotId);
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        //此处添加阈值
        JSONArray detectionParaArr = new JSONArray();
        JSONObject jsonObject;
        if (StringUtils.isEmpty(combinationCode)){
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.HUMIDITY);
            jsonObject.set("threshold", "50");
            detectionParaArr.add(jsonObject);
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.INFRARED);
            jsonObject.set("threshold", "30");
            detectionParaArr.add(jsonObject);
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.NOISE);
            jsonObject.set("threshold", "70");
            detectionParaArr.add(jsonObject);
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.PM2P5);
            jsonObject.set("threshold", "10");
            detectionParaArr.add(jsonObject);
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.TEMPERATURE);
            jsonObject.set("threshold", "30");
            detectionParaArr.add(jsonObject);
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.FIREEXTINGUISHER);
            jsonObject.set("threshold", "70");
            detectionParaArr.add(jsonObject);
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.SMOKE);
            jsonObject.set("threshold", null);
            detectionParaArr.add(jsonObject);
            jsonObject = new JSONObject();
            jsonObject.set("detection_id", Detection.WAIT);
            jsonObject.set("threshold", null);
            detectionParaArr.add(jsonObject);
        }else{
            JSONArray tempArr = roomParamObject.getJSONObject("inspect_setting").getJSONArray("detection_combination");
            for (int i = 0; i < tempArr.size(); i++) {
                jsonObject = tempArr.getJSONObject(i);
                if (jsonObject.getStr("combination_code").equals(combinationCode)){
                    detectionParaArr = jsonObject.getJSONArray("detection_para");
                }
            }
            tempArr = null;
        }

        List<DetectionInfo> list = detectionInfoDao.list();
        DetectionInfo detectionInfo;
        //机器人支持的检测项
        JSONArray supportDetectionArr = robotParamObject.getJSONArray("support_detection");
        for (int p = 0; p < list.size(); p++) {
            detectionInfo = list.get(p);
            String detectionId = detectionInfo.getDetectionId();

            //判断机器人是否支持该功能
            boolean existData = false;
            for (int j = 0; j < supportDetectionArr.size(); j++) {
                String str = supportDetectionArr.getStr(j);
                if (detectionId.equals(str)){
                    existData = true;
                }
            }

            if (!existData){
                list.remove(p);
                p--;
                continue;
            }

            for (int i = 0; i < detectionParaArr.size(); i++) {
                String arrDetectionId = detectionParaArr.getJSONObject(i).getStr("detection_id");
                if(arrDetectionId.equals(detectionId)){
                    detectionInfo.setThreshold(detectionParaArr.getJSONObject(i).getStr("threshold"));
                    break;
                }
            }
        }
        return list;
    }
}
