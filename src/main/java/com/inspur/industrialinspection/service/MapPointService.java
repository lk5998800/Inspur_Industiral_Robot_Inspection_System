package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.MapPoint;

import java.util.List;

/**
 * 地图点位
 * @author kliu
 * @date 2022/6/1 15:22
 */
public interface MapPointService {
    /**
     * 地图点位列表
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/1 15:23
     */
    List list(long roomId);
    /**
     * 添加地图点位
     * @param mapPoint
     * @return void
     * @author kliu
     * @date 2022/6/1 15:47
     */
    void add(MapPoint mapPoint);
    /**
     * 更新地图点位
     * @param mapPoint
     * @return void
     * @author kliu
     * @date 2022/6/1 15:48
     */
    void update(MapPoint mapPoint);
    /**
     * 删除地图点位
     * @param mapPoint
     * @return void
     * @author kliu
     * @date 2022/6/1 15:48
     */
    void delete(MapPoint mapPoint);
}
