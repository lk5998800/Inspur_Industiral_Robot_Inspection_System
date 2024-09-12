package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskInstance;
import com.inspur.industrialinspection.entity.RemoteControlTaskInstance;

import java.util.List;

/**
 * @author: kliu
 * @description: 特定点巡检
 * @date: 2022/9/7 16:54
 */
public interface ParticularPointInspectionTaskInstanceDao {

    /**
     * 添加
     * @param particularPointInspectionTaskInstance
     * @return void
     * @author kliu
     * @date 2022/9/7 17:34
     */
    long addAndReturnId(ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance);

    /**
     * 判断数据是否存在
     * @param instanceId
     * @return boolean
     * @author kliu
     * @date 2022/9/7 16:21
     */
    boolean checkExist(long instanceId);

    /**
     * 更新图片个数
     * @param instanceId
     * @param count
     * @return void
     * @author kliu
     * @date 2022/9/14 10:45
     */
    void updatePicCount(long instanceId, int count);

    /**
     * 获取任务实例列表
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/9/14 13:44
     */
    List list(long roomId);

    /**
     * 特定点巡检实例
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.ParticularPointInspectionTaskInstance
     * @author kliu
     * @date 2022/9/15 10:46
     */
    ParticularPointInspectionTaskInstance getDetlById(long instanceId);

    /**
     * 更新结束时间
     * @param instanceId
     * @param time
     * @return void
     * @author kliu
     * @date 2022/9/15 10:49
     */
    void updateEndTime(long instanceId, String time);
    /**
     * 获取图片列表
     * @param roomId
     * @param startTime
     * @param endTime
     * @return java.util.List<com.inspur.industrialinspection.entity.RemoteControlTaskInstance>
     * @author kliu
     * @date 2022/9/24 10:54
     */
    List<ParticularPointInspectionTaskInstance> getPictureList(long roomId, String startTime, String endTime);

}
