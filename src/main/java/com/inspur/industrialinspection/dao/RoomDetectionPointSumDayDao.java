package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RoomDetectionPointSumDay;

import java.util.List;

/**
 * @author kliu
 * @description 机房检测项按日汇总
 * @date 2022/4/18 20:25
 */
public interface RoomDetectionPointSumDayDao {
    /**
     * 添加
     * @param roomDetectionPointSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    void add(RoomDetectionPointSumDay roomDetectionPointSumDay);
    /**
     * 更新
     * @param roomDetectionPointSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    void update(RoomDetectionPointSumDay roomDetectionPointSumDay);
    /**
     * 添加
     * @param roomDetectionPointSumDay
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:24
     */
    boolean checkExist(RoomDetectionPointSumDay roomDetectionPointSumDay);
    /**
     * 查询明细
     * @param roomDetectionPointSumDay
     * @return com.inspur.industrialinspection.entity.RoomDetectionPointSumDay
     * @author kliu
     * @date 2022/5/24 18:25
     */
    RoomDetectionPointSumDay getDetlById(RoomDetectionPointSumDay roomDetectionPointSumDay);
    /**
     * 依据机房id和日期获取数据
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionPointSumDay>
     * @author kliu
     * @date 2022/5/24 18:26
     */
    List<RoomDetectionPointSumDay> list(long roomId, String dateStr);
    /**
     * 依据机房id和日期获取数据，大于等于日期
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionPointSumDay>
     * @author kliu
     * @date 2022/6/25 17:43
     */
    List<RoomDetectionPointSumDay> listGteDate(long roomId, String dateStr);
}
