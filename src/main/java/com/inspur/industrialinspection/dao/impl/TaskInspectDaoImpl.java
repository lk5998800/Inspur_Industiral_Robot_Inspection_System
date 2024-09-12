package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.TaskInspectDao;
import com.inspur.industrialinspection.entity.TaskInspect;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 巡检任务信息dao实现
 * @author wangzhaodi
 * @date 2022/11/16 14:17
 */
@Repository
public class TaskInspectDaoImpl implements TaskInspectDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public TaskInspect getByRoomName(String roomName) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("select * from task_inspect where room_name = ? and status = ?");
        int index = 1;
        db.set(index++, roomName);
        db.set(index++, 1);
        List<TaskInspect> list = db.dbQuery(TaskInspect.class);
        if (list.size()==0){
            return null;
        }
        return list.get(0);
    }

    @Override
    public void endTask(TaskInspect taskInspect) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update task_inspect ");
        stringBuffer.append("   set status = ?, end_time = ? ");
        stringBuffer.append(" where task_inspect_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, 0);
        db.set(index++, taskInspect.getEndTime());
        db.set(index++, taskInspect.getTaskInspectId());
        db.dbUpdate();
    }

    @Override
    public void addTask(TaskInspect taskInspect) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into task_inspect ");
        stringBuffer.append("  (room_name, start_time, status) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskInspect.getRoomName());
        db.set(index++, taskInspect.getStartTime());
        db.set(index++, 1);
        db.dbUpdate();
    }

    @Override
    public List<TaskInspect> list(int parkId, String startTime, String endTime) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_inspect where start_time between ? and ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, startTime);
        db.set(index++, endTime);
        return db.dbQuery(TaskInspect.class);
    }

    @Override
    public TaskInspect getDetlById(long taskInspectId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_inspect ");
        stringBuffer.append(" where task_inspect_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskInspectId);
        List list = db.dbQuery(TaskInspect.class);
        if (list.size() == 0){
            return null;
        }else{
            return (TaskInspect) list.get(0);
        }
    }
}
