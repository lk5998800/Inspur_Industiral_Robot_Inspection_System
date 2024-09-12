package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RoomInfo;

import java.util.List;
/**
 * @author kliu
 * @description 机房信息
 * @date 2022/4/18 20:25
 */
public interface RoomInfoDao {
    /**
     * 根据机器人id获取机房信息
     * @param robotId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomInfo>
     * @author kliu
     * @date 2022/5/24 19:03
     */
    List<RoomInfo> list(long robotId);
    /**
     * 根据机器人楼栋获取机房
     * @param robotId
     * @param buildingId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomInfo>
     * @author kliu
     * @date 2022/6/17 19:49
     */
    List<RoomInfo> listWithBuilding(long robotId, long buildingId, int parkId);
    /**
     * 根据机器人园区获取机房信息
     * @param robotId
     * @param parkId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomInfo>
     * @author kliu
     * @date 2022/6/7 16:15
     */
    List<RoomInfo> list(long robotId, long parkId);
    /**
     * 添加机房并返回id
     * @param roomInfo
     * @return long
     * @author kliu
     * @date 2022/5/24 19:04
     */
    long addAndReturnId(RoomInfo roomInfo);
    /**
     * 更新机房信息
     * @param roomInfo
     * @author kliu
     * @date 2022/5/24 19:04
     */
    void update(RoomInfo roomInfo);
    /**
     * 删除机房信息
     * @param roomId
     * @author kliu
     * @date 2022/5/24 19:05
     */
    void delete(long roomId);
    /**
     * 校验数据是否存在
     * @param roomId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:05
     */
    boolean checkExist(long roomId);
    /**
     * 根据机房id获取明细信息
     * @param roomId
     * @return com.inspur.industrialinspection.entity.RoomInfo
     * @author kliu
     * @date 2022/5/24 19:07
     */
    RoomInfo getDetlById(long roomId);

    RoomInfo getByRoomNameAndParkId(String roomName, long parkId);
}
