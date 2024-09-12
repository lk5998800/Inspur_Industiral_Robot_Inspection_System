package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RoomDetectionSumDay;

import java.util.List;

/**
 * @author kliu
 * @description 机房检测项按日汇总
 * @date 2022/4/18 20:25
 */
public interface RoomDetectionSumDayDao {
    /**
     * 添加
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    void add(RoomDetectionSumDay roomDetectionSumDay);
    /**
     * 更新
     * @param roomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:24
     */
    void update(RoomDetectionSumDay roomDetectionSumDay);
    /**
     * 添加
     * @param roomDetectionSumDay
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:24
     */
    boolean checkExist(RoomDetectionSumDay roomDetectionSumDay);
    /**
     * 查询明细
     * @param roomDetectionSumDay
     * @return com.inspur.industrialinspection.entity.RoomDetectionSumDay
     * @author kliu
     * @date 2022/5/24 18:25
     */
    RoomDetectionSumDay getDetlById(RoomDetectionSumDay roomDetectionSumDay);
    /**
     * 依据机房id和日期获取数据
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionSumDay>
     * @author kliu
     * @date 2022/5/24 18:26
     */
    List<RoomDetectionSumDay> list(long roomId, String dateStr);
    /**
     * 依据机房id和日期获取数据，大于等于日期
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomDetectionSumDay>
     * @author kliu
     * @date 2022/6/25 17:43
     */
    List<RoomDetectionSumDay> listGteDate(long roomId, String dateStr);

    /**
     * 删除机房统计数据
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/29 10:23
     */
    void deleteAll(long roomId);

    /**
     * 获取传入技能的最高值
     * @param roomId
     * @param time
     * @param temperature
     * @return
     */
    List<RoomDetectionSumDay> getDayMaxTemperature(long roomId, String time, String temperature);
}
