package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.PointInfo;
/**
 * 机器人移动服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RobotMoveService {
    /**
     * 机器人移动
     * @param pointInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:51
     */
    void move(PointInfo pointInfo);
}
