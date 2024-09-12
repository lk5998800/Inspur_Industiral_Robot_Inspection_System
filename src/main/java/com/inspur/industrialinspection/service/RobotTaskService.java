package com.inspur.industrialinspection.service;

import java.util.HashMap;
/**
 * 机器人任务服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RobotTaskService {
    /**
     * 获取最近一次任务基本信息
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/14 16:49
     */
    HashMap getRecentTaskBasic(long roomId);
    /**
     * 获取最近7日异常的数据
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/14 16:58
     */
    HashMap getAbnormalCountRecentDays7(long roomId);
    /**
     * 获取最近一次任务运行机柜信息
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/14 16:49
     */
    HashMap getRecentTaskCabinetsInfo(long roomId);
    /**
     * 获取最近一次任务告警信息
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/14 16:48
     */
    HashMap getRecentTaskWarnInfo(long roomId);
    /**
     * 获取机器人运行状态信息
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/14 16:48
     */
    HashMap getRobotRunStatusInfo(long roomId);
}
