package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.TaskDetectionSum;

import java.util.List;
/**
 * @author kliu
 * @description 机房检测项按日汇总
 * @date 2022/4/18 20:25
 */
public interface TaskDetectionSumDao {
    /**
     * 添加任务检测项汇总数据
     * @param taskDetectionSum
     * @author kliu
     * @date 2022/5/24 19:18
     */
    void add(TaskDetectionSum taskDetectionSum);
    /**
     * 更新任务检测项汇总
     * @param taskDetectionSum
     * @author kliu
     * @date 2022/5/24 19:18
     */
    void update(TaskDetectionSum taskDetectionSum);
    /**
     * 校验数据是否存在
     * @param taskDetectionSum
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:18
     */
    boolean checkExist(TaskDetectionSum taskDetectionSum);
    /**
     * 获取明细
     * @param taskDetectionSum
     * @return com.inspur.industrialinspection.entity.TaskDetectionSum
     * @author kliu
     * @date 2022/5/24 19:18
     */
    TaskDetectionSum getDetlById(TaskDetectionSum taskDetectionSum);
    /**
     * 根据任务实例获取任务检测项汇总数据
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionSum>
     * @author kliu
     * @date 2022/5/24 19:19
     */
    List<TaskDetectionSum> list(long instanceId);
    /**
     * 获取近期一段时间内的异常数据
     * @param roomId
     * @param minDate
     * @return java.util.List
     * @author kliu
     * @date 2022/5/24 19:19
     */
    List getRecentAbnormalData(long roomId, String minDate);
}
