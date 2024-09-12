package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.RobotRoom;

import java.util.List;
/**
 * 机器人服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RobotInfoService {
    /**
     * 获取机器人列表
     * @param roomId
     * @return java.util.List
     * @throws Exception
     * @author kliu
     * @date 2022/6/14 16:51
     */
    List list(long roomId);
    /**
     * 获取机器人列表-dcim
     * @param parkId
     * @param jsonObject
     * @return java.util.List
     * @author kliu
     * @date 2022/9/1 17:08
     */
    List getRobotInfos(int parkId, JSONObject jsonObject);
    /**
     * 获取所有机器人列表
     * @return java.util.List
     * @author kliu
     * @date 2022/6/14 16:51
     */
    List listWithoutPark();
    /**
     * 添加机器人
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/6/14 16:52
     */
    void add(JSONObject jsonObject);
}
