package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ParticularPointInspectionTaskInstanceDao;
import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskInstance;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: kliu
 * @description: 特定点巡检
 * @date: 2022/9/7 16:54
 */
@Repository
public class ParticularPointInspectionTaskInstanceDaoImpl implements ParticularPointInspectionTaskInstanceDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public long addAndReturnId(ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into particular_point_inspection_task_instance ");
        stringBuffer.append("  (instance_id, start_time, exec_status, detection, point_name, room_id, robot_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, 'running', ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, particularPointInspectionTaskInstance.getStartTime());
        db.set(index++, particularPointInspectionTaskInstance.getDetection());
        db.set(index++, particularPointInspectionTaskInstance.getPointName());
        db.set(index++, particularPointInspectionTaskInstance.getRoomId());
        db.set(index++, particularPointInspectionTaskInstance.getRobotId());
        return db.dbUpdateAndReturnId();
    }

    @Override
    public boolean checkExist(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from particular_point_inspection_task_instance ");
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
        stringBuffer.append("update particular_point_inspection_task_instance set pic_count = ? ");
        stringBuffer.append(" where instance_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, count);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    @Override
    public List<ParticularPointInspectionTaskInstance> list(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select a.*, ");
        stringBuffer.append("       (select room_name from room_info where room_id = a.room_id) room_name ");
        stringBuffer.append("  from particular_point_inspection_task_instance a ");
        stringBuffer.append(" where a.room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        return db.dbQuery(ParticularPointInspectionTaskInstance.class);
    }

    @Override
    public ParticularPointInspectionTaskInstance getDetlById(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from particular_point_inspection_task_instance ");
        stringBuffer.append(" where instance_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        List<ParticularPointInspectionTaskInstance> list = db.dbQuery(ParticularPointInspectionTaskInstance.class);
        if (list.size()==0){
            throw new RuntimeException("特定点巡检实例不存在，请检查传入的数据");
        }
        return list.get(0);
    }

    @Override
    public void updateEndTime(long instanceId, String time) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update particular_point_inspection_task_instance set end_time = ?, exec_status = 'end' ");
        stringBuffer.append(" where instance_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, time);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    @Override
    public List<ParticularPointInspectionTaskInstance> getPictureList(long roomId, String startTime, String endTime) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from particular_point_inspection_task_instance ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and start_time between ? and ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startTime);
        db.set(index++, DateUtil.offsetDay(DateUtil.parse(endTime, "yyyy-MM-dd"),1).toString("yyyy-MM-dd"));
        return db.dbQuery(ParticularPointInspectionTaskInstance.class);
    }
}
