package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.BuildingInfo;

import java.util.List;

/**
 * 机房信息服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface BuildingInfoService {
    /**
     * 获取楼栋信息
     * @param
     * @return java.util.List
     * @author kliu
     * @date 2022/6/17 19:21
     */
    List list();
    /**
     * 获取楼栋信息-dcim
     * @param parkId
     * @return java.util.List
     * @author kliu
     * @date 2022/9/1 16:32
     */
    List getBuildingInfos(int parkId);
    /**
     * 添加
     * @param buildingInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:46
     */
    void add(BuildingInfo buildingInfo);
    /**
     * 更新
     * @param buildingInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:46
     */
    void update(BuildingInfo buildingInfo);
    /**
     * 删除
     * @param buildingInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:46
     */
    void delete(BuildingInfo buildingInfo);
}
