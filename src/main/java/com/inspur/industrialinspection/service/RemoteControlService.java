package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;

/**
 * 远程控制服务
 *
 * @author kliu
 * @date 2022/8/11 15:54
 */
public interface RemoteControlService {

    /**
     * 移动
     *
     * @param robotId
     * @param roomId
     * @param v
     * @param w
     * @return void
     * @author kliu
     * @date 2022/8/25 14:32
     */
    void move(long robotId, long roomId, double v, double w);

    /**
     * 机器人重启
     *
     * @param robotId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/25 17:20
     */
    void reboot(long robotId, long roomId);

    /**
     * 机器人重启-can断电式
     *
     * @param robotId
     * @return void
     * @author kliu
     * @date 2022/9/21 13:44
     */
    void rebootCan(long robotId);

    /**
     * 重定位
     *
     * @param robotId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/25 17:20
     */
    void relocalization(long robotId, long roomId);

    /**
     * 升降杆控制
     *
     * @param robotId
     * @param roomId
     * @param position
     * @return void
     * @author kliu
     * @date 2022/8/25 17:24
     */
    void lifter(long robotId, long roomId, String position);

    /**
     * 急停
     *
     * @param robotId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/25 17:20
     */
    void emergencyStop(long robotId, long roomId);

    /**
     * 返航
     *
     * @param robotId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/25 17:20
     */
    void backChargingPile(long robotId, long roomId) throws InterruptedException;

    /**
     * 前置拍照
     *
     * @param robotId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/25 17:20
     */
    void frontPicture(long robotId, long roomId) throws InterruptedException;

    /**
     * 后置拍照
     *
     * @param robotId
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/25 17:20
     */
    void afterPicture(long robotId, long roomId) throws InterruptedException;

    /**
     * 相册任务列表
     *
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/9/14 13:39
     */
    JSONArray picTaskList(long roomId);

    /**
     * 照片明细
     *
     * @param taskType
     * @param instanceId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/14 15:16
     */
    JSONArray picTaskDetl(String taskType, long instanceId);


    /**
     * 升降杆结果是否正常
     *
     * @return
     * @throws
     * @author LiTan
     * @date 2022/10/9 17:21
     */
    void liftingLeverResults(Long robotId, String value);

    /**
     * 临时任务结束
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/10/18 11:01
     */
    void tempTaskEnd(Long instanceId);

    /**
     * 获取websocket URL地址
     * @param
     * @return java.lang.String
     * @author kliu
     * @date 2022/11/4 9:11
     */
    String getWebsocketUrl();
}
