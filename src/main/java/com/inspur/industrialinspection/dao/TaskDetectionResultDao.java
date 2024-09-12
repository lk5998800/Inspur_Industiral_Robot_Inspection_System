package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.TaskDetectionResult;

import java.util.List;
/**
 * @author kliu
 * @description 任务检测项结果
 * @date 2022/4/18 20:25
 */
public interface TaskDetectionResultDao {
    /**
     * 添加任务检测结果
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    void add(TaskDetectionResult taskDetectionResult);
    /**
     * 更新传感器数据
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    void updateSensor(TaskDetectionResult taskDetectionResult);
    /**
     * 更新红外测温数据
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    void updateInfrared(TaskDetectionResult taskDetectionResult);
    /**
     * 更新报警灯数据
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    void updateAlarmLight(TaskDetectionResult taskDetectionResult);

    /**
     * 更新灭火器数据
     * @param taskDetectionResult
     * @return void
     * @author kliu
     * @date 2022/11/8 10:40
     */
    void updateFireExtinguisher(TaskDetectionResult taskDetectionResult);
    /**
     * 设置检测项分析完成
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:12
     */
    void updateAnalyseComplete(TaskDetectionResult taskDetectionResult);
    /**
     * 判断当前机柜是否存在检测项数据
     * @param taskDetectionResult
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:12
     */
    boolean checkExist(TaskDetectionResult taskDetectionResult);
    /**
     * 根据实例id和点位名称获取检测项结果
     * @param instanceId
     * @param pointName
     * @return com.inspur.industrialinspection.entity.TaskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:12
     */
    TaskDetectionResult getDetlByInstanceIdAndPointName(long instanceId, String pointName);
    /**
     * 根据实例id和点位名称获取检测项结果-锁表
     * @param instanceId
     * @param pointName
     * @return com.inspur.industrialinspection.entity.TaskDetectionResult
     * @author kliu
     * @date 2022/6/7 8:55
     */
    TaskDetectionResult getDetlByInstanceIdAndPointNameForUpdate(long instanceId, String pointName);
    /**
     * 根据实例id获取检测项结果
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/5/24 19:13
     */
    List<TaskDetectionResult> list(long instanceId);
    /**
     * 异常数据
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/9/30 9:00
     */
    List<TaskDetectionResult> abnormalList(long instanceId);
    /**
     * 依据实例id获取机柜检测项数量
     * @param instanceId
     * @return int
     * @author kliu
     * @date 2022/5/24 19:13
     */
    int count(long instanceId);
    /**
     * 根据机房和日期获取检测项结果
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/5/24 19:13
     */
    List<TaskDetectionResult> listByRoomIdAndDate(long roomId, String dateStr);
    /**
     * 根据机房和日期和点位获取检测项结果
     * @param roomId
     * @param dateStr
     * @param pointName
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/11/1 20:04
     */
    List<TaskDetectionResult> listByRoomIdAndDate(long roomId, String dateStr, String pointName);
    /**
     * 根据机房和日期获取检测项机柜数量
     * 巡检机柜次
     * @param roomId
     * @param dateStr
     * @return int
     * @author kliu
     * @date 2022/5/24 19:13
     */
    int countByRoomIdAndDate(long roomId, String dateStr);
    /**
     * 根据机房和日期获取点位巡检机柜次
     * @param roomId
     * @param dateStr
     * @return int
     * @author kliu
     * @date 2022/5/24 19:13
     */
    List pointCountByRoomIdAndDate(long roomId, String dateStr);
    /**
     * 根据实例id获取最新检测结果数据
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:14
     */
    TaskDetectionResult getDetlByMaxUpdateTime(long instanceId);
    /**
     * 获取未分析的检测项结果数据
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/5/24 19:14
     */
    List<TaskDetectionResult> unAnalyseList();

    /**
     * 获取最近一次红外图片
     * @param robotId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/9/9 11:14
     */
    List<TaskDetectionResult> getRecentInfraredPic(long robotId);

    /**
     * 获取最近一次工业相机图片
     * @param robotId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/9/9 11:14
     */
    List<TaskDetectionResult> getRecentAlarmLightPic(long robotId);

    /**
     * @author: LiTan
     * @description:    根据id获取执行到最新的点位
     * @date:   2022-10-24 17:17:22
     */
    TaskDetectionResult getByInstanceIdNewestResult(long instanceId);

    /**
     * 计算正常巡检柜次
     * @param roomId
     * @param startDate
     * @return int
     * @author kliu
     * @date 2022/10/29 11:07
     */
    int normalCabinetcountByRoomIdAndDate(long roomId, String startDate);

    /**
     * 巡检机柜总数
     * @param roomId
     * @param startDate
     * @return int
     * @author kliu
     * @date 2022/11/2 9:07
     */
    int cabinetCountByRoomIdAndDate(long roomId, String startDate);

    /**
     * 累计设备报警
     * @param roomId
     * @param startDate
     * @return int
     * @author kliu
     * @date 2022/11/2 9:07
     */
    int cabinetWarnCountByRoomIdAndDate(long roomId, String startDate);

    /**
     * 依据机房id、点位名称、检测项id，时间获取检测结果
     * @param roomId
     * @param pointName
     * @param detectionId
     * @param dateStr
     * @return java.util.List
     * @author kliu
     * @date 2022/11/4 11:56
     */
    List list(long roomId, String pointName, String detectionId, String dateStr);
    /**
     * 获取执行的灭火器数据
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/11/8 14:06
     */
    List<TaskDetectionResult> getFireExtinguishers(long instanceId);

    /**
     * 获取灭火器检测数据
     * @param roomId
     * @param pointName
     * @param dateStr
     * @return java.util.List
     * @author kliu
     * @date 2022/11/15 10:27
     */
    List listFireExtinguisher(long roomId, String pointName, String dateStr);
}
