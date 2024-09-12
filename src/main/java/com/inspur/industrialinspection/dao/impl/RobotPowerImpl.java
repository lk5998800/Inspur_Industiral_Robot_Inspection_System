package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RobotPowerDao;
import com.inspur.industrialinspection.entity.RobotPower;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: kliu
 * @description: 机器人电量服务
 * @date: 2022/8/9 8:45
 */
@Service
public class RobotPowerImpl implements RobotPowerDao {
    @Autowired
    BeanFactory beanFactory;

    @Override
    public void save(RobotPower robotPower) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into robot_power (robot_id, power, heart_time, heart_day) values (?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotPower.getRobotId());
        db.set(index++, robotPower.getPower());
        db.set(index++, robotPower.getHeartTime());
        db.set(index++, robotPower.getHeartDay());
        db.dbUpdate();
    }

    /**
     * 校验数据是否存在
     * @param robotPower
     * @return boolean
     * @author kliu
     * @date 2022/8/9 9:04
     */
    @Override
    public boolean checkExist(RobotPower robotPower) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from robot_power where robot_id = ? and heart_time = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotPower.getRobotId());
        db.set(index++, robotPower.getHeartTime());
        return db.dbQuery().size()>0;
    }

    /**
     * 更新
     * @param robotPower
     * @return void
     * @author kliu
     * @date 2022/8/9 9:05
     */
    @Override
    public void update(RobotPower robotPower) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update industrial_robot.robot_power ");
        stringBuffer.append("   set power = ?, heart_day = ? ");
        stringBuffer.append(" where robot_id = ? ");
        stringBuffer.append("   and heart_time = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotPower.getPower());
        db.set(index++, robotPower.getHeartDay());
        db.set(index++, robotPower.getRobotId());
        db.set(index++, robotPower.getHeartTime());
        db.dbUpdate();
    }

    /**
     * 获取电量列表
     * @param robotId
     * @return java.util.List<com.inspur.industrialinspection.entity.RobotPower>
     * @author kliu
     * @date 2022/8/9 10:33
     */
    @Override
    public List<RobotPower> list(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from robot_power where robot_id = ? order by heart_day, heart_time ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        return db.dbQuery(RobotPower.class);
    }

    /**
     * 删除过期数据 时间小于当前时间，日期小于当前日期
     * @param robotId
     * @param heartTime
     * @param hearDay
     * @return void
     * @author kliu
     * @date 2022/8/9 10:42
     */
    @Override
    public void deleteExpireData(long robotId, String heartTime, int hearDay) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from robot_power ");
        stringBuffer.append(" where robot_id = ? ");
        stringBuffer.append("   and heart_time < ? ");
        stringBuffer.append("   and heart_day < ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        db.set(index++, heartTime);
        db.set(index++, hearDay);
        db.dbUpdate();
    }
}
