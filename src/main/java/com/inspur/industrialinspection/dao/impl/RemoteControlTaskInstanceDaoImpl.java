package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RemoteControlTaskInstanceDao;
import com.inspur.industrialinspection.entity.RemoteControlTaskInstance;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author: kliu
 * @description: 远程控制任务实例
 * @date: 2022/9/7 14:15
 */
@Repository
public class RemoteControlTaskInstanceDaoImpl implements RemoteControlTaskInstanceDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public long addAndReturnId(RemoteControlTaskInstance remoteControlTaskInstance) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into remote_control_task_instance ");
        stringBuffer.append("  (instance_id, start_time, exec_status, user_id, room_id, robot_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, remoteControlTaskInstance.getStartTime());
        db.set(index++, remoteControlTaskInstance.getExecStatus());
        db.set(index++, remoteControlTaskInstance.getUserId());
        db.set(index++, remoteControlTaskInstance.getRoomId());
        db.set(index++, remoteControlTaskInstance.getRobotId());
        return db.dbUpdateAndReturnId();

    }

    @Override
    public void updateTaskEndByUserId(long userId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update remote_control_task_instance ");
        stringBuffer.append("   set end_time = ?, exec_status = 'end' ");
        stringBuffer.append(" where user_id = ? ");
        stringBuffer.append("   and exec_status = 'running' ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, DateUtil.now());
        db.set(index++, userId);
        db.dbUpdate();
    }

    @Override
    public boolean checkExist(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from remote_control_task_instance ");
        stringBuffer.append(" where instance_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        return db.dbQuery().size()>0;
    }

    @Override
    public void updatePicCount(long instanceId, int count) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update remote_control_task_instance ");
        stringBuffer.append("   set pic_count = ? ");
        stringBuffer.append(" where instance_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, count);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    @Override
    public List<RemoteControlTaskInstance> list(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select a.*, ");
        stringBuffer.append("       (select room_name from room_info where room_id = a.room_id) room_name ");
        stringBuffer.append("  from remote_control_task_instance a ");
        stringBuffer.append(" where a.room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        return db.dbQuery(RemoteControlTaskInstance.class);
    }

    @Override
    public long getInstanceIdByUserId(long userId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select instance_id from remote_control_task_instance ");
        stringBuffer.append(" where user_id = ?");
        stringBuffer.append("   and exec_status = 'running'");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, userId);
        List<Map> list = db.dbQuery();
        if (list.size() > 0) {
            return Long.parseLong(list.get(0).get("instance_id").toString());
        }else{
            throw new RuntimeException("远程控制权限已过期，请重新申请权限");
        }
    }

    @Override
    public List<RemoteControlTaskInstance> getPictureList(long roomId, String startTime, String endTime) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from remote_control_task_instance ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and start_time between ? and ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startTime);
        db.set(index++, DateUtil.offsetDay(DateUtil.parse(endTime, "yyyy-MM-dd"),1).toString("yyyy-MM-dd"));
        return db.dbQuery(RemoteControlTaskInstance.class);
    }

    @Override
    public int countByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from remote_control_task_instance ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and start_time > ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startDate);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public List<RemoteControlTaskInstance> listByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from remote_control_task_instance a ");
        stringBuffer.append(" where a.room_id = ? ");
        stringBuffer.append("   and a.start_time > ?");
        stringBuffer.append("  order by a.start_time asc");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, dateStr);
        return db.dbQuery(RemoteControlTaskInstance.class);
    }
}
