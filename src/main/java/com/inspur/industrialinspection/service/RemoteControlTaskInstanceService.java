package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.RemoteControlTaskInstance;

/**
 * 远程控制实例服务
 *
 * @author kliu
 * @date 2022/9/7 14:16
 */
public interface RemoteControlTaskInstanceService {
    /**
     * 接收任务结果
     *
     * @param json
     * @return void
     * @author kliu
     * @date 2022/9/7 14:18
     */
    void receiveTaskResult(String json);

    /**
     * 添加
     *
     * @param remoteControlTaskInstance
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    void add(RemoteControlTaskInstance remoteControlTaskInstance);

    /**
     * 将任务更新为已结束
     *
     * @param userId
     * @return void
     * @author kliu
     * @date 2022/9/7 16:17
     */
    void updateTaskEndByUserId(long userId);

}
