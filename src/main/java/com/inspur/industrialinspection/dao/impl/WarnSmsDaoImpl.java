package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.WarnSmsDao;
import com.inspur.industrialinspection.entity.WarnSms;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author: kliu
 * @description: 检测项组合dao实现
 * @date: 2022/4/16 10:21
 */
@Repository
public class WarnSmsDaoImpl implements WarnSmsDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加告警短信记录
     * @param warnSms
     * @return void
     * @author kliu
     * @date 2022/5/24 19:32
     */
    @Override
    public void add(WarnSms warnSms) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into warn_sms ");
        stringBuffer.append("  (task_id, point_name, user_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, warnSms.getTaskId());
        db.set(index++, warnSms.getPointName());
        db.set(index++, warnSms.getUserId());
        db.dbUpdate();
    }

    /**
     * 校验数据是否存在
     * @param taskId
     * @param pointName
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:17
     */
    @Override
    public boolean checkExist(long taskId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from warn_sms a");
        stringBuffer.append(" where a.task_id = ?");
        stringBuffer.append("   and a.point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskId);
        db.set(index++, pointName);
        return db.dbQuery().size()>0;
    }
}
