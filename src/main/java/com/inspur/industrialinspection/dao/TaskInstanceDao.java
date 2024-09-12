package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * @author kliu
 * @description 任务执行实例信息
 * @date 2022/4/18 20:25
 */
public interface TaskInstanceDao {
    /**
     * 添加任务实例并返回id
     * @param taskInstance
     * @return long
     * @author kliu
     * @date 2022/5/24 20:09
     */
    long addAndReturnId(TaskInstance taskInstance);
    /**
     * 更新任务执行实例
     * @param taskInstance
     * @return void
     * @author kliu
     * @date 2022/5/24 20:09
     */
    void update(TaskInstance taskInstance);
    /**
     * 更新下发的json
     * @param taskInstance
     * @return void
     * @author kliu
     * @date 2022/11/16 14:54
     */
    void updateTaskJsonCompress(TaskInstance taskInstance);
    /**
     * 获取实例明细
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 20:10
     */
    TaskInstance getDetlById(long instanceId);
    /**
     * 获取实例明细锁表
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 20:10
     */
    TaskInstance getDetlByIdForUpdate(long instanceId);
    /**
     * 根据机房和日期获取实例
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInstance>
     * @author kliu
     * @date 2022/5/24 20:10
     */
    List<TaskInstance> getTaskByRoomIdAndDate(long roomId, String dateStr);
    /**
     * 分页查询任务实例
     * @param roomId
     * @param robotId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/5/24 20:10
     */
    PageBean list(long roomId, long robotId, int pageSize, int page);
    /**
     * 获取任务实例
     * @param parkId
     * @param roomId
     * @param robotId
     * @param startTime
     * @param endTime
     * @return java.util.List
     * @author kliu
     * @date 2022/11/7 13:33
     */
    List list(int parkId, long roomId, long robotId, String startTime, String endTime);
    /**
     * 校验实例是否存在
     * @param instanceId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:10
     */
    boolean checkExist(long instanceId);
    /**
     * 校验任务是否执行过实例
     * @param taskId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:11
     */
    boolean checkExistByTaskId(long taskId);

    /**
     * 根据机器人id获取最新的任务实例
     * @param robotId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/6/8 8:52
     */
    TaskInstance getLatestTaskInstanceByRobotId(long robotId);

    /**
     * 根据机房和日期获取实例
     * @param roomId
     * @param qsrq
     * @param zzrq
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInstance>
     * @author kliu
     * @date 2022/9/2 10:25
     */
    List<TaskInstance> getTaskInstanceByRoomIdAndDate(long roomId, String qsrq, String zzrq);

    /**
     * 依据机器人查看正在运行的任务
     * @param robotId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInstance>
     * @author kliu
     * @date 2022/8/8 14:41
     */
    List<TaskInstance> getRunningTaskInstanceByRobotId(long robotId);
    /**
     * 根据机房下所执行过的实例对象
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInstance>
     * @author kliu
     * @date 2022/7/29 11:15
     */
    List getAllInstanceByRoomId(long roomId);

    /**
     * 获取图片列表
     * @param roomId
     * @param startTime
     * @param endTime
     * @param inspectTypeArr
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInstance>
     * @author kliu
     * @date 2022/9/24 10:39
     */
    List<TaskInstance> getPictureList(long roomId, String startTime, String endTime, String inspectTypeArr);

    /**
     * 根据任务id查询
     * @param taskId
     * @return
     */
    TaskInstance getTaskById(Long taskId);

    /**
     * 修改任务状态信息
     * @param task
     */
    void updateById(TaskInstance task);

    /**
     * 获取暂停任务
     * @return
     * @param robotId
     * @param taskStatus
     */
    TaskInstance getSuspendTask(long robotId, String taskStatus);

    /**
     * 根据room_id 获取当前最新的一条任务的instanceId
     * @param roomId
     * @return
     */
    TaskInstance getByRoomIDNewTask(long roomId);

    /**
     * 依据机房id和日期计算数量
     * @param roomId
     * @param startDate
     * @return int
     * @author kliu
     * @date 2022/10/29 11:44
     */
    int countByRoomIdAndDate(long roomId, String startDate);

    List getTaskInstances(long robotId, String taskStatus);

    /**
     * 分页查询
     * @param parkId
     * @param roomId
     * @param startTimeStart
     * @param startTimeEnd
     * @param endTimeStart
     * @param endTimeEnd
     * @param inspectTypeIdIn
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/12/5 12:08
     */
    PageBean list(int parkId, long roomId, String startTimeStart, String startTimeEnd, String endTimeStart, String endTimeEnd, String inspectTypeIdIn, int pageSize, int page);
}
