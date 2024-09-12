package com.inspur.industrialinspection.service.impl;

import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Dcim 调用服务
 * @author kliu
 * @date 2022/9/1 15:48
 */
@Service
@Slf4j
public class DcimServiceImpl implements DcimService {

    private static final String SM4KEY = "cK6>pY0|uT7$yX6*";

    @Autowired
    private BuildingInfoService buildingInfoService;
    @Autowired
    private RoomInfoService roomInfoService;
    @Autowired
    private RobotInfoService robotInfoService;
    @Autowired
    private CabinetPicService cabinetPicService;
    @Autowired
    private TaskInstanceService taskInstanceService;
    @Autowired
    private TaskInspectService taskInspectService;

    /**
     * dcim调用
     * @param jsonObject
     * @param request
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/9/1 15:47
     */
    @Override
    public Object dcimInvoke(JSONObject jsonObject, HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("token不能为空");
        }
        SymmetricCrypto symmetricCrypto = SmUtil.sm4(SM4KEY.getBytes());
        String decryptStr = null;
        try {
            decryptStr = symmetricCrypto.decryptStr(token);
        } catch (Exception e) {
            throw new RuntimeException("token解析失败，请使用平台发放的token");
        }
        JSONObject parkObject = JSONUtil.parseObj(decryptStr);
        int parkId = parkObject.getInt("parkId");
        if (parkId==1){
            parkId = 2;
        }else if(parkId == 2){
            parkId = 1;
        }

        if (!jsonObject.containsKey("method") || !jsonObject.containsKey("param")){
            throw new RuntimeException("参数缺失");
        }

        String method = jsonObject.getStr("method");
        String param = jsonObject.getStr("param");
        if (StringUtils.isEmpty(method)){
            throw new RuntimeException("method参数不能为空");
        }
        JSONObject paramObject = new JSONObject();
        if (!StringUtils.isEmpty(param)){
            paramObject = JSONUtil.parseObj(param);
        }
        if ("getBuildingInfos".equals(method)){
            return buildingInfoService.getBuildingInfos(parkId);
        }else if ("getRoomInfos".equals(method)){
            return roomInfoService.getRoomInfos(parkId, paramObject);
        }else if ("getRobotInfos".equals(method)){
            return robotInfoService.getRobotInfos(parkId, paramObject);
        }else if ("getPicInfos".equals(method)){
            return cabinetPicService.getPicInfos(parkId, paramObject);
        }else if ("getAbnormalPicInfos".equals(method)){
            return cabinetPicService.getAbnormalPicInfos(parkId, paramObject);
        }else if ("getInstanceInfos".equals(method)){
            return taskInstanceService.getInstanceInfos(parkId, paramObject);
        }else if ("getInstanceSensorDetl".equals(method)){
            return taskInstanceService.getInstanceSensorDetl(parkId, paramObject);
        }else if ("getRoomTaskDetections".equals(method)){
            return taskInstanceService.getRoomTaskDetections(paramObject);
        }else if ("getPhoneInspectTasks".equals(method)){
            return taskInspectService.getPhoneInspectTasks(parkId, paramObject);
        }else if ("getPhoneInspectDetl".equals(method)){
            return taskInspectService.getPhoneInspectDetl(parkId, paramObject);
        }else if ("getRoomInspectionResume".equals(method)){
            return taskInstanceService.getRoomInspectionResume(parkId, paramObject);
        }
        else{
            throw new RuntimeException("传入的method【"+method+"】不支持");
        }
    }

    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("parkId", 2);
        SymmetricCrypto symmetricCrypto = SmUtil.sm4(SM4KEY.getBytes());
        String s = symmetricCrypto.encryptBase64(jsonObject.toString());
        System.out.println(s);
        String s1 = symmetricCrypto.decryptStr(s);
        System.out.println(s1);
    }
}
