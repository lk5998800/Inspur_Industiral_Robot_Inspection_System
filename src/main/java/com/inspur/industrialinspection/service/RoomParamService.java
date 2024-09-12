package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;

import java.io.IOException;
/**
 * 机房参数服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RoomParamService {
    /**
     * 获取相机参数
     * @param taskId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/14 16:44
     */
    JSONObject getCamaraParam(long taskId);
    /**
     * 获取机房参数
     * @param roomId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/14 16:45
     */
    JSONObject getRoomParam(long roomId);
    /**
     * 添加机房参数
     * @param roomId
     * @param str
     * @return void
     * @author kliu
     * @date 2022/6/14 16:45
     */
    void add(long roomId, String str);

    /**
     * 清除机房参数缓存
     * @param
     * @return void
     * @author kliu
     * @date 2022/7/12 19:43
     */
    void clearRoomParamCache();
}
