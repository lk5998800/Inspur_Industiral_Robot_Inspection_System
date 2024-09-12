package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.AlongWorkPedestrianDetectionAlarm;

import java.util.List;

/**
 * 随工行人检测异常信息
 * @author kliu
 * @date 2022/6/27 19:52
 */
public interface AlongWorkPedestrianDetectionAlarmDao {
    /**
     * 添加
     * @param alongWorkPedestrianDetectionAlarm
     * @return void
     * @author kliu
     * @date 2022/6/14 19:54
     */
    void add(AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm);
    /**
     * 修改
     * @param alongWorkPedestrianDetectionAlarm
     * @return void
     * @author kliu
     * @date 2022/6/14 16:56
     */
    void update(AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm);

    /**
     * 校验数据是否存在
     * @param alongWorkPedestrianDetectionAlarm
     * @return boolean
     * @author kliu
     * @date 2022/6/14 19:59
     */
    boolean checkExist(AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm);

    /**
     * 依据机房id和日期获取数据
     * @param roomId
     * @param dateStr
     * @return java.util.List
     * @author kliu
     * @date 2022/11/5 11:28
     */
    List listByRoomIdAndDate(long roomId, String dateStr);
}
