package com.inspur.industrialinspection.service;
import cn.hutool.json.JSONObject;

import java.util.List;

/**
 * 机器人状态服务
 * @author kliu
 * @date 2022/6/1 20:23
 */
public interface RobotStatusService {
    /**
     * 接收机器人状态服务
     * @param robotStatusJson
     * @return void
     * @author kliu
     * @date 2022/6/1 20:23
     */
    void receiveRobotStatus(String robotStatusJson);
    /**
     * 获取机器人电量
     * @param robotId
     * @return java.lang.String
     * @author kliu
     * @date 2022/6/1 20:24
     */
    String getRobotPower(long robotId);
    /**
     * 获取机器人电量
     * @param roomId
     * @param robotId
     * @return double
     * @author kliu
     * @date 2022/9/19 10:23
     */
    double getRobotPower(long roomId, long robotId);
    /**
     * 机器人是否在线
     * @param robotId
     * @return boolean
     * @author kliu
     * @date 2022/6/1 20:24
     */
    boolean robotOnline(long robotId);

    /**
     * 获取机器人状态信息
     * @param robotId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/4 10:41
     */
    JSONObject getRobotStatus(long robotId);

    /**
     * 获取机器人状态信息-增加相关任务信息-给手机监控用
     * @param robotId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/4 10:41
     */
    JSONObject getRobotStatusWithTask(long robotId);

    /**
     * 获取机器人电量变化曲线
     * @param robotId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/8/9 9:48
     */
    JSONObject getRobotPowerChangeLine(long robotId);


    /**
     * 获取机器人服务地址
     * @param
     * @return java.util.List
     * @author kliu
     * @date 2022/9/5 14:28
     */
    List getRobotServiceUrl();

    /**
     * 获取机器人最近拍摄的一张图片
     * @param robotId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/9 11:06
     */
    JSONObject getRecentPicture(long robotId);

    /**
     * 设置机器人任务状态
     * @param robotId
     * @param status
     * @return void
     * @author kliu
     * @date 2022/9/20 9:44
     */
    void setRobotTaskStatus(long robotId, boolean status);

    /**
     * 接受机器人回桩异常信息发短信、打电话
     * @param robotId
     * @param message
     */
    void pileReturnFailure(long robotId, String message) throws Exception;
}
