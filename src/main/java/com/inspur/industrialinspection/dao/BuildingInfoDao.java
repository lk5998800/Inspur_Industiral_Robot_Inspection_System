package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.BuildingInfo;

import java.util.List;

/**
 * @author kliu
 * @description 楼栋信息
 * @date 2022/4/18 20:25
 */
public interface BuildingInfoDao {
    /**
     * 根据园区获取楼栋信息
     * @param parkId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomInfo>
     * @author kliu
     * @date 2022/6/17 19:28
     */
    List<BuildingInfo> list(int parkId);
    /**
     * 添加
     * @param buildingInfo
     * @return void
     * @author kliu
     * @date 2022/6/17 19:28
     */
    void add(BuildingInfo buildingInfo);
    /**
     * 更新
     * @param buildingInfo
     * @return void
     * @author kliu
     * @date 2022/6/17 19:28
     */
    void update(BuildingInfo buildingInfo);
    /**
     * 删除
     * @param buildingId
     * @return void
     * @author kliu
     * @date 2022/6/17 19:28
     */
    void delete(long buildingId);

    BuildingInfo getDetlById(long buildingId);
}
