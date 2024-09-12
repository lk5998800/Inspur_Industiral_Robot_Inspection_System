package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RobotInfo;

import java.util.List;

/**
 * @author kliu
 * @description 机器人信息
 * @date 2022/4/18 20:25
 */
public interface RobotInfoDao {
    /**
     * 获取机器人明细数据
     * @param robotId
     * @return com.inspur.industrialinspection.entity.RobotInfo
     * @author kliu
     * @date 2022/5/24 18:20
     */
    RobotInfo getDetlById(long robotId);
    /**
     * 依据机房获取机器人信息
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.RobotInfo>
     * @author kliu
     * @date 2022/5/24 18:21
     */
    List<RobotInfo> list(int parkId, long roomId);
    /**
     * 获取所有机器人信息
     * @return java.util.List<com.inspur.industrialinspection.entity.RobotInfo>
     * @author kliu
     * @date 2022/6/7 14:02
     */
    List<RobotInfo> listWithoutPark();
    /**
     * 判断机器人是否存在
     * @param robotId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:21
     */
    boolean checkIsExist(long robotId);
    /**
     * 添加机房信息
     * @param robotInfo
     * @author kliu
     * @date 2022/5/24 18:21
     */
    void add(RobotInfo robotInfo);
}
