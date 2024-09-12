package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;

import java.io.IOException;
/**
 * 机器人参数
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RobotParamService {
    /**
     * 获取机器人参数
     * @param robotId
     * @return cn.hutool.json.JSONObject
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:50
     */
    JSONObject getRobotParam(long robotId) throws IOException;
    /**
     * 添加机器人参数
     * @param robotId
     * @param str
     * @return void
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:50
     */
    void add(long robotId, String str) throws IOException;

    /**
     * 清除机器人参数缓存
     * @param
     * @return void
     * @author kliu
     * @date 2022/7/12 19:43
     */
    void clearRobotParamCache();
}
