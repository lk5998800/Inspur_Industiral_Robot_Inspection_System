package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.TaskInfo;
import com.inspur.industrialinspection.entity.TaskInstance;

/**
 * @author kliu
 * @description 任务
 * @date 2022/4/18 20:25
 */
public interface TaskDao {
    /**
     * 根据机房id获取最近一次任务实例信息
     * @param roomId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 19:09
     */
    TaskInstance getRecentTaskByRoomId(long roomId);

    /**
     * 根据机房id获取最近一次有数据的任务实例信息
     * @param roomId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 19:09
     */
    TaskInstance getRecentHasDataTaskByRoomId(long roomId);
    /**
     * 判断当前机房是否存在执行的任务
     * @param roomId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:09
     */
    boolean checkTaskExistByRoomId(long roomId);
    /**
     * 依据任务实例获取机房id
     * @param instanceId
     * @return long
     * @author kliu
     * @date 2022/5/24 19:09
     */
    long getRoomIdByTaskId(long instanceId);
}
