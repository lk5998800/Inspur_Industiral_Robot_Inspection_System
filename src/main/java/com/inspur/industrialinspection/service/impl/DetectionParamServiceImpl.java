package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.DetectionInfoDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.entity.DetectionInfo;
import com.inspur.industrialinspection.entity.DetectionParam;
import com.inspur.industrialinspection.entity.Threshold;
import com.inspur.industrialinspection.service.DetectionParamService;
import com.inspur.industrialinspection.service.RoomParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 检测项参数实现
 * @author kliu
 * @date 2022/5/25 8:45
 */
@Service
public class DetectionParamServiceImpl implements DetectionParamService {
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private DetectionInfoDao detectionInfoDao;

    @Autowired
    private RoomParamService roomParamService;

    /**
     * 获取检测项参数
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionParam>
     * @author kliu
     * @date 2022/5/25 8:45
     */
    @Override
    public List<DetectionParam> list(long roomId) {
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        //获取机房参数
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);

        JSONArray detectionThresholdArray = new JSONArray();

        //取检测项参数
        if(roomParamObject.containsKey(ParaKey.DETECTION_PARA)){
            detectionThresholdArray = roomParamObject.getJSONArray(ParaKey.DETECTION_PARA);
        }

        //检测项参数转换成list
        List<DetectionParam> detectionParams = JSONUtil.toList(detectionThresholdArray, DetectionParam.class);

        //取检测项信息
        List<DetectionInfo> detectionInfos = detectionInfoDao.list();

        //设置检测项参数中的检测项名称、最低阈值等
        for (DetectionParam detectionParam : detectionParams) {
            detectionParam.setRoomId(roomId);
            String detectionId = detectionParam.getDetectionId();
            for (DetectionInfo detectionInfo : detectionInfos) {
                String detectionId1 = detectionInfo.getDetectionId();
                if(detectionId1.equals(detectionId)){
                    detectionParam.setDetectionName(detectionInfo.getDetectionName());
                    break;
                }
            }
            String thresholdStr = null;
            List<Threshold> thresholdList = detectionParam.getThresholdList();
            for (Threshold threshold : thresholdList) {
                if ("预警".equals(threshold.getLevel())) {
                    thresholdStr = threshold.getThresholdLl();
                    detectionParam.setThreshold("阈值"+thresholdStr);
                }
            }
        }

        //添加检测项中有的，但是检测项参数中没有的数据
        for (DetectionInfo detectionInfo : detectionInfos) {
            String detectionId = detectionInfo.getDetectionId();
            boolean existData = false;
            for (DetectionParam detectionParam : detectionParams) {
                String detectionId1 = detectionParam.getDetectionId();
                if(detectionId1.equals(detectionId)){
                    existData = true;
                    break;
                }
            }

            if(!existData){
                DetectionParam detectionParam = new DetectionParam();
                detectionParam.setDetectionId(detectionId);
                detectionParam.setDetectionName(detectionInfo.getDetectionName());
                detectionParam.setRoomId(roomId);
                detectionParam.setInfraredHeightList(new ArrayList<>());
                detectionParam.setThresholdList(new ArrayList<>());
                detectionParams.add(detectionParam);
            }
        }


        return detectionParams;
    }

    /**
     * 添加检测项参数
     * @param detectionParam
     * @return void
     * @author kliu
     * @date 2022/5/25 8:45
     */
    @Override
    public void add(DetectionParam detectionParam) {
        long roomId = detectionParam.getRoomId();
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);

        JSONArray detectionThresholdArray = new JSONArray();

        if(roomParamObject.containsKey(ParaKey.DETECTION_PARA)){
            detectionThresholdArray = roomParamObject.getJSONArray(ParaKey.DETECTION_PARA);
        }

        String detectionId = detectionParam.getDetectionId();

        if(!detectionInfoDao.checkExist(detectionId)){
            throw new RuntimeException("传入检测项id不存在，请检查传入的数据");
        }

        //添加检测项名称
        String detectionName = detectionInfoDao.getDetlById(detectionId).getDetectionName();
        detectionParam.setDetectionName(detectionName);

        List<DetectionParam> detectionThresholdInfos = JSONUtil.toList(detectionThresholdArray, DetectionParam.class);
        for (int i = 0; i < detectionThresholdInfos.size(); i++) {
            if (detectionId.equals(detectionThresholdInfos.get(i).getDetectionId())) {
                detectionThresholdInfos.remove(i);
            }
        }

        detectionThresholdInfos.add(detectionParam);

        String dataStr = JSONUtil.parseArray(detectionThresholdInfos).toString();
        dataStr = dataStr.replaceAll("thresholdLl", "threshold_ll");
        dataStr = dataStr.replaceAll("thresholdUl", "threshold_ul");
        dataStr = dataStr.replaceAll("roomId", "room_id");
        dataStr = dataStr.replaceAll("detectionId", "detection_id");
        dataStr = dataStr.replaceAll("detectionName", "detection_name");
        dataStr = dataStr.replaceAll("thresholdList", "threshold_list");
        dataStr = dataStr.replaceAll("infraredHeightList", "infrared_height_list");

        roomParamObject.set(ParaKey.DETECTION_PARA, JSONUtil.parseArray(dataStr));

        roomParamService.add(roomId, roomParamObject.toString());
    }
}
