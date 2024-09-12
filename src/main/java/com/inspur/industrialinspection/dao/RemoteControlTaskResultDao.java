package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RemoteControlTaskResult;

import java.util.List;

/**
 * 远程遥控结果
 * @author kliu
 * @date 2022/9/7 14:45
 */
public interface RemoteControlTaskResultDao {
    /**
     * 添加
     * @param remoteControlTaskResult
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    void add(RemoteControlTaskResult remoteControlTaskResult);
    /**
     * 校验数据是否存在
     * @param remoteControlTaskResult
     * @return boolean
     * @author kliu
     * @date 2022/9/7 14:46
     */
    boolean checkExist(RemoteControlTaskResult remoteControlTaskResult);
    /**
     * 图片个数
     * @param instanceId
     * @return int
     * @author kliu
     * @date 2022/9/14 10:27
     */
    int picCount(long instanceId);
    /**
     * 获取列表
     * @param instanceId
     * @return java.util.List
     * @author kliu
     * @date 2022/9/14 15:21
     */
    List list(long instanceId);
}
