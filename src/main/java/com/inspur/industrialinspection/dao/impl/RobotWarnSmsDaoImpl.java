package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RobotWarnSmsDao;
import com.inspur.industrialinspection.entity.RobotWarnSms;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 机器人告警短信发送
 * @author kliu
 * @date 2022/9/15 16:09
 */
@Repository
public class RobotWarnSmsDaoImpl implements RobotWarnSmsDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加
     *
     * @param robotWarnSms
     * @return void
     * @author kliu
     * @date 2022/9/15 16:11
     */
    @Override
    public void add(RobotWarnSms robotWarnSms) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into robot_warn_sms (robot_id, sms_time) values(?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotWarnSms.getRobotId());
        db.set(index++, robotWarnSms.getSmsTime());
        db.dbUpdate();
    }

    /**
     * 获取最近一次机器人异常短信发送记录
     *
     * @param robotId
     * @return com.inspur.industrialinspection.entity.RobotWarnSms
     * @author kliu
     * @date 2022/9/15 16:12
     */
    @Override
    public String getRecentTime(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select max(sms_time) sms_time ");
        stringBuffer.append("  from industrial_robot.robot_warn_sms ");
        stringBuffer.append(" where robot_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        List<Map> list = db.dbQuery();
        if (list.size() > 0) {
            Object smsTime = list.get(0).get("sms_time");
            if (smsTime==null){
                return null;
            }else{
                return smsTime.toString();
            }
        }
        return null;
    }

    @Override
    public boolean checkExist(RobotWarnSms robotWarnSms) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from industrial_robot.robot_warn_sms ");
        stringBuffer.append(" where robot_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotWarnSms.getRobotId());
        return db.dbQuery().size()>0;
    }

    @Override
    public void update(RobotWarnSms robotWarnSms) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update robot_warn_sms set sms_time = ? where robot_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotWarnSms.getSmsTime());
        db.set(index++, robotWarnSms.getRobotId());
        db.dbUpdate();
    }
}
