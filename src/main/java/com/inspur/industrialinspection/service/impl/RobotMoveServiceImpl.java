package com.inspur.industrialinspection.service.impl;

import com.inspur.cron.TaskExecuteCron;
import com.inspur.industrialinspection.dao.PointInfoDao;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.service.RobotMoveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author kliu
 * @description 机器人移动
 * @date 2022/5/24 17:01
 */
@Service
@Slf4j
public class RobotMoveServiceImpl implements RobotMoveService {

    @Autowired
    private TaskExecuteCron taskExecuteCron;

    @Autowired
    private PointInfoDao pointInfoDao;

    @Override
    public void move(PointInfo pointInfo) {
        long roomId = pointInfo.getRoomId();
        String pointName = pointInfo.getPointName();

        if (!pointInfoDao.checkExist(pointInfo)) {
            throw new RuntimeException("传入点位未设置位姿，请先设置位姿后再移动");
        }
        taskExecuteCron.executeRobotMoveTask(roomId, pointName);
    }
}
