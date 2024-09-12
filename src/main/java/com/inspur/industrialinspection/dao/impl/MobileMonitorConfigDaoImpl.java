package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.MobileMonitorConfigDao;
import com.inspur.industrialinspection.entity.MobileMonitorConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: kliu
 * @description: 机器人服务配置
 * @date: 2022/9/5 15:30
 */
@Repository
public class MobileMonitorConfigDaoImpl implements MobileMonitorConfigDao {
    @Autowired
    private BeanFactory beanFactory;

    @Override
    public List<MobileMonitorConfig> list() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select robot_id, service_url from mobile_monitor_config");
        db.setSql(stringBuffer.toString());
        return db.dbQuery(MobileMonitorConfig.class);
    }
}
