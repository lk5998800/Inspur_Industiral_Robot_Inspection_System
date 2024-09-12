package com.inspur.industrialinspection.service;

import java.io.IOException;
/**
 * 机器人系统设置服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RobotSysSetService {
    /**
     * 下发机器人系统设置信息
     * @param robotId
     * @return void
     * @throws InterruptedException
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:49
     */
    void issued(long robotId) throws IOException, InterruptedException;
    /**
     * 接收机器人系统设置结果
     * @param robotSysSetJson
     * @return void
     * @author kliu
     * @date 2022/6/14 16:50
     */
    void receiveRobotSysSetResult(String robotSysSetJson);
}
