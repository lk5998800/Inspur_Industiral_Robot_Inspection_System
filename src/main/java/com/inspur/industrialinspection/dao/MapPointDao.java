package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.MapPoint;

import java.util.List;

/**
 * 检测项信息
 * @author: kliu
 * @date: 2022/4/18 20:21
 */
public interface MapPointDao {
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
    /**
     * 校验数据是否存在
     * @param mapPoint
     * @return boolean
     * @author kliu
     * @date 2022/6/1 16:18
     */
    boolean checkIsExist(MapPoint mapPoint);
}
