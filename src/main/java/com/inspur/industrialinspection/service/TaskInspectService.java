package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.TaskInspect;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 巡检任务服务
 * @author wangzhaodi
 * @date 2022/11/16 14:22
 */
@Service
public interface TaskInspectService {
    /**
     * 依据机房名称获取巡检任务信息
     * @param roomName
     * @return TaskInspect
     * @author wangzhaodi
     * @date 2022/11/16 14:30
     */
    TaskInspect getByRoomName(String roomName);
    /**
     * 改变巡检任务状态为结束
     * @param taskInspect
     * @author wangzhaodi
     * @date 2022/11/16 14：30
     */
    void endTask(TaskInspect taskInspect);
    /**
     * 创建任务
     * @param taskInspect
     * @author wangzhaodi
     * @date 2022/11/16 14:30
     */
    void addTask(TaskInspect taskInspect);

    /**
     * 获取任务信息-dcim
     * @param parkId
     * @param jsonObject
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/22 13:45
     */
    JSONObject getPhoneInspectTasks(int parkId, JSONObject jsonObject);

    /**
     * 依据任务id获取图片数据
     * @param parkId
     * @param paramObject
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/22 9:28
     */
    JSONObject getPhoneInspectDetl(long parkId, JSONObject paramObject);
}
