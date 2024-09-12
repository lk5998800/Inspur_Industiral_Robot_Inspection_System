package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * 任务实例服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface TaskInstanceService {
    /**
     * 获取任务实例
     * @param roomId
     * @param robotId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/10/14 16:54
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
     * 终止任务
     * @param taskInstance
     * @return void
     * @author kliu
     * @date 2022/6/14 16:38
     */
    void terminate(TaskInstance taskInstance);
    /**
     * 接收任务终止结果
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/14 16:38
     */
    void receiveTerminateResult(String json);
    /**
     * 机器人结束任务
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/14 16:39
     */
    void industrialRobotEndTask(String json);

    /**
     * 根据任务id和类型获取返回充电桩路径
     * @param taskId
     * @param type
     * @param pointName
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/24 15:31
     */
    JSONArray getBackChargingPilePath(long taskId, int type, String pointName);
    /**
     * 根据任务id和类型获取到达点位的路径
     * @param taskId
     * @param type
     * @param pointName
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/24 15:31
     */
    JSONArray getBackToPointNamePath(long taskId, int type, String pointName);

    /**
     * 获取正在运行的任务id-远程控制使用
     * @param roomId
     * @return int
     * @author kliu
     * @date 2022/9/19 15:31
     */
    int getRunningTaskCount(long roomId);

    /**
     * 终止正在运行的任务-远程控制使用
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/9/19 15:36
     */
    void terminateRunningTask(long roomId);

    /**
     * 暂停任务接口，修改任务为暂停状态
     * @param taskStatusJson
     */
    void updateTaskStatus(String taskStatusJson);

    /**
     * dcim获取实例信息
     * @param parkId
     * @param paramObject
     * @return List
     * @author kliu
     * @date 2022/11/7 13:32
     */
    List getInstanceInfos(int parkId, JSONObject paramObject);

    /**
     * 获取机房传感器数据
     * @param parkId
     * @param paramObject
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/2 16:17
     */
    JSONArray getInstanceSensorDetl(int parkId, JSONObject paramObject);

    /**
     * 获取机房任务巡检项
     * @param
     * @param
     * @return
     * @author wangzhaodi
     * @date 2022/11/18 10:53
     */
    JSONArray getRoomTaskDetections(JSONObject paramObject);

    /**
     * dcim获取机房履历
     * @param parkId
     * @param paramObject
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/12/5 9:01
     */
    PageBean getRoomInspectionResume(int parkId, JSONObject paramObject);
}
