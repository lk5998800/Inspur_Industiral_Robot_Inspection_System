package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RemoteControlTaskInstance;

import java.util.List;

/**
 * @author: kliu
 * @description: 远程控制实例
 * @date: 2022/9/7 14:15
 */
public interface RemoteControlTaskInstanceDao {
    /**
     * 添加
     * @param remoteControlTaskInstance
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    long addAndReturnId(RemoteControlTaskInstance remoteControlTaskInstance);

    /**
     * 将任务更新为已结束
     * @param userId
     * @return void
     * @author kliu
     * @date 2022/9/7 16:17
     */
    void updateTaskEndByUserId(long userId);

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
     * @date 2022/9/14 10:32
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
     * 根据用户获取实例id
     * @param userId
     * @return long
     * @author kliu
     * @date 2022/9/14 10:18
     */
    long getInstanceIdByUserId(long userId);

    /**
     * 获取图片列表
     * @param roomId
     * @param startTime
     * @param endTime
     * @return java.util.List<com.inspur.industrialinspection.entity.RemoteControlTaskInstance>
     * @author kliu
     * @date 2022/9/24 10:51
     */
    List<RemoteControlTaskInstance> getPictureList(long roomId, String startTime, String endTime);

    /**
     * 依据机房id和日期计算数量
     * @param roomId
     * @param startDate
     * @return int
     * @author kliu
     * @date 2022/10/29 11:44
     */
    int countByRoomIdAndDate(long roomId, String startDate);

    /**
     * 依据机房id和日期获取列表，大于日期到当日
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.RemoteControlTaskInstance>
     * @author kliu
     * @date 2022/10/31 8:46
     */
    List<RemoteControlTaskInstance> listByRoomIdAndDate(long roomId, String dateStr);

}
