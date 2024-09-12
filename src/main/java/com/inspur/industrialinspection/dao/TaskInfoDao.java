package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.TaskInfo;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.page.PageBean;

import java.util.List;
/**
 * @author kliu
 * @description 任务信息
 * @date 2022/4/18 20:25
 */
public interface TaskInfoDao {
    /**
     * 更新任务基本信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 19:58
     */
    void update(TaskInfo taskInfo);
    /**
     * 获取任务明细
     * @param taskId
     * @return com.inspur.industrialinspection.entity.TaskInfo
     * @author kliu
     * @date 2022/5/24 19:59
     */
    TaskInfo getDetlById(long taskId);
    /**
     * 根据机房id和日期获取任务执行实例，取大于日期的数据
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInstance>
     * @author kliu
     * @date 2022/5/24 19:59
     */
    List<TaskInstance> listByRoomIdAndDate(long roomId, String dateStr);
    /**
     * 分页查询任务信息
     * @param roomId
     * @param robotId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/5/24 20:00
     */
    PageBean list(long roomId, long robotId, int pageSize, int page);
    /**
     * 分页查询任务信息-所有数据
     * @param roomId
     * @param robotId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/6/8 11:26
     */
    PageBean listWithoutPark(long roomId, long robotId, int pageSize, int page);
    /**
     * 添加任务信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:00
     */
    void add(TaskInfo taskInfo);
    /**
     * 更新任务执行时间
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:01
     */
    void updateExecTime(TaskInfo taskInfo);
    /**
     * 更新任务所有信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:01
     */
    void updateAll(TaskInfo taskInfo);
    /**
     * 删除任务信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:01
     */
    void delete(TaskInfo taskInfo);
    /**
     * 获取启用中的任务
     * @return java.util.List
     * @author kliu
     * @date 2022/5/24 20:01
     */
    List list();
    /**
     * 校验同类型的任务是否已经存在
     * @param taskInfo
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:02
     */
    boolean checkExistByInspectTypeRobot(TaskInfo taskInfo);
    /**
     * 校验巡检类型是否已经创建任务
     * @param roomId
     * @param inspectTypeId
     * @return boolean
     * @author kliu
     * @date 2022/6/14 16:36
     */
    boolean checkExistByInspectType(long roomId, long inspectTypeId);

    /**
     * 依据机器人将任务改为停用状态
     * @param robotId
     * @return void
     * @author kliu
     * @date 2022/8/12 10:54
     */
    void updateTaskStopExecute(long robotId);

    /**
     * 获取所有任务
     * @return
     */
    List<TaskInfo> getTasks();

    /**
     * 根据room_id 获取任务id
     * @param roomId
     * @return
     */
    List<TaskInfo> getByRoomId(long roomId);
}
