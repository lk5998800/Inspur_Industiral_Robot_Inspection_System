package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RobotPower;

import java.util.List;

/**
 * 机器人电量
 * @author kliu
 * @date 2022/8/9 8:46
 */
public interface RobotPowerDao {
    /**
     * 保存电量数据
     * @param robotPower
     * @return void
     * @author kliu
     * @date 2022/8/9 8:47
     */
    void save(RobotPower robotPower);
    /**
     * 校验数据是否存在
     * @param robotPower
     * @return boolean
     * @author kliu
     * @date 2022/8/9 9:04
     */
    boolean checkExist(RobotPower robotPower);

    /**
     * 更新
     * @param robotPower
     * @return void
     * @author kliu
     * @date 2022/8/9 9:05
     */
    void update(RobotPower robotPower);

    /**
     * 获取电量列表
     * @param robotId
     * @return java.util.List<com.inspur.industrialinspection.entity.RobotPower>
     * @author kliu
     * @date 2022/8/9 10:33
     */
    List<RobotPower> list(long robotId);

    /**
     * 删除过期数据 时间小于当前时间，日期小于当前日期
     * @param robotId
     * @param heartTime
     * @param hearDay
     * @return void
     * @author kliu
     * @date 2022/8/9 10:41
     */
    void deleteExpireData(long robotId, String heartTime, int hearDay);
}
