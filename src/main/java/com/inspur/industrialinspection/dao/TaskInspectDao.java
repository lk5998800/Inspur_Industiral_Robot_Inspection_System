package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.TaskInspect;

import java.util.List;

/**
 * @author wangzhaodi
 * @description 巡检任务信息
 * @date 2022/11/16 14:06
 */
public interface TaskInspectDao {
    /**
     * 依据机房名称获取巡检任务信息
     * @param roomName
     * @return TaskInspect
     * @author wangzhaodi
     * @date 2022/11/16 14:08
     */
    TaskInspect getByRoomName(String roomName);
    /**
     * 改变巡检任务状态为结束
     * @param taskInspect
     * @author wangzhaodi
     * @date 2022/11/16 14：13
     */
    void endTask(TaskInspect taskInspect);
    /**
     * 创建任务
     * @param taskInspect
     * @author wangzhaodi
     * @date 2022/11/16 14:13
     */
    void addTask(TaskInspect taskInspect);

    /**
     * 获取数据
     * @param parkId
     * @param startTime
     * @param endTime
     * @return java.util.List
     * @author kliu
     * @date 2022/11/21 17:54
     */
    List list(int parkId, String startTime, String endTime);

    TaskInspect getDetlById(long taskInspectId);
}
