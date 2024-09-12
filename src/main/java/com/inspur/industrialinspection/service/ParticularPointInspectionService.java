package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskInstance;

public interface ParticularPointInspectionService {
    /**
     * 添加
     * @param particularPointInspectionTaskInstance
     * @return void
     * @author kliu
     * @date 2022/9/7 17:34
     */
    void add(ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance);

    /**
     * 接收任务结果
     * @param json
     * @return void
     * @author kliu
     * @date 2022/9/7 14:18
     */
    void receiveTaskResult(String json);

    /**
     * 机器人结束任务
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/14 16:39
     */
    void industrialRobotEndTask(String json);
}
