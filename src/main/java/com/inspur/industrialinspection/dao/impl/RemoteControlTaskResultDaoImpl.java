package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RemoteControlTaskResultDao;
import com.inspur.industrialinspection.entity.RemoteControlTaskResult;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author: kliu
 * @description: 远程遥控结果数据
 * @date: 2022/9/7 14:44
 */
@Repository
public class RemoteControlTaskResultDaoImpl implements RemoteControlTaskResultDao {
    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加
     *
     * @param remoteControlTaskResult
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    @Override
    public void add(RemoteControlTaskResult remoteControlTaskResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into remote_control_task_result ");
        stringBuffer.append("  (instance_id, img_type, img_url, img_time) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, remoteControlTaskResult.getInstanceId());
        db.set(index++, remoteControlTaskResult.getImgType());
        db.set(index++, remoteControlTaskResult.getImgUrl());
        db.set(index++, remoteControlTaskResult.getImgTime());
        db.dbUpdate();

    }

    /**
     * 校验数据是否存在
     *
     * @param remoteControlTaskResult
     * @return boolean
     * @author kliu
     * @date 2022/9/7 14:46
     */
    @Override
    public boolean checkExist(RemoteControlTaskResult remoteControlTaskResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from remote_control_task_result ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and img_type = ? ");
        stringBuffer.append("   and img_url = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, remoteControlTaskResult.getInstanceId());
        db.set(index++, remoteControlTaskResult.getImgType());
        db.set(index++, remoteControlTaskResult.getImgUrl());
        return db.dbQuery().size()>0;
    }

    @Override
    public int picCount(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from remote_control_task_result ");
        stringBuffer.append(" where instance_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count").toString());
    }

    @Override
    public List<RemoteControlTaskResult> list(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from remote_control_task_result ");
        stringBuffer.append(" where instance_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        return db.dbQuery(RemoteControlTaskResult.class);
    }
}
