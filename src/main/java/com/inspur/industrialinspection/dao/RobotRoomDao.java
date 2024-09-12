package com.inspur.industrialinspection.dao;

/**
 * @author kliu
 * @description 机器人机房信息
 * @date 2022/4/18 20:25
 */
public interface RobotRoomDao {
    /**
     * 根据机房id获取机器人id
     * @param roomId
     * @return long
     * @author kliu
     * @date 2022/5/24 18:23
     */
    long getRobotIdByRoomId(long roomId);
    /**
     * 添加
     * @param roomId
     * @param robotId
     * @author kliu
     * @date 2022/5/24 18:23
     */
    void add(long roomId, long robotId);
    /**
     * 依据机器人id获取机房id
     * @param robotId
     * @return long
     * @author kliu
     * @date 2022/5/24 18:23
     */
    long getRoomIdByRobotId(long robotId);
}
