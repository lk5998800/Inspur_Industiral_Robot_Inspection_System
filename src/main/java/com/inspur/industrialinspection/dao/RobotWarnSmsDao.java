package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RobotWarnSms;

/**
 * 机器人告警短信发送
 * @author kliu
 * @date 2022/9/15 16:08
 */
public interface RobotWarnSmsDao {
    /**
     * 添加
     * @param robotWarnSms
     * @return void
     * @author kliu
     * @date 2022/9/15 16:11
     */
    void add(RobotWarnSms robotWarnSms);

    /**
     * 获取最近一次机器人异常短信发送时间
     * @param robotId
     * @return java.lang.String
     * @author kliu
     * @date 2022/9/15 16:14
     */
    String getRecentTime(long robotId);

    /**
     * 校验数据是否存在
     * @param robotWarnSms
     * @return void
     * @author kliu
     * @date 2022/9/16 8:34
     */
    boolean checkExist(RobotWarnSms robotWarnSms);
    /**
     * 添加
     * @param robotWarnSms
     * @return void
     * @author kliu
     * @date 2022/9/15 16:11
     */
    void update(RobotWarnSms robotWarnSms);
}
