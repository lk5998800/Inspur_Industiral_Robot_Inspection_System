package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.PointInfo;

import java.util.List;

/**
 * @author kliu
 * @description 检测点信息dao
 * @date 2022/4/18 20:24
 */
public interface PointInfoDao {
    /**
     * 获取机房下所有检测点位姿信息
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/5/24 18:17
     */
    List list(long roomId);
    /**
     * 添加监测点位姿信息
     * @param pointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    void add(PointInfo pointInfo);
    /**
     * 更新监测点位姿信息
     * @param pointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    void update(PointInfo pointInfo);
    /**
     * 校验检测点位姿是否存在
     * @param pointInfo
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:18
     */
    boolean checkExist(PointInfo pointInfo);
    /**
     * 获取检测点位姿明细
     * @param pointInfo
     * @return com.inspur.industrialinspection.entity.PointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    PointInfo getDetlById(PointInfo pointInfo);
}
