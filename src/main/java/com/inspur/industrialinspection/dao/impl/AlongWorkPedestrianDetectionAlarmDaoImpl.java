package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.AlongWorkPedestrianDetectionAlarmDao;
import com.inspur.industrialinspection.entity.AlongWorkPedestrianDetectionAlarm;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: kliu
 * @description: 随工行人检测dao实现
 * @date: 2022/6/27 19:53
 */
@Repository
public class AlongWorkPedestrianDetectionAlarmDaoImpl implements AlongWorkPedestrianDetectionAlarmDao {
    @Autowired
    private BeanFactory beanFactory;

    @Override
    public void add(AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into along_work_pedestrian_detection_alarm ");
        stringBuffer.append("  (pid, point_name, alarm_time, alarm_information, url) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getPid());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getPointName());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getAlarmTime());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getAlarmInformation());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getUrl());
        db.dbUpdate();
    }

    @Override
    public void update(AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update along_work_pedestrian_detection_alarm ");
        stringBuffer.append("   set alarm_time = ?, alarm_information = ?, url = ? ");
        stringBuffer.append(" where pid = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getAlarmTime());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getAlarmInformation());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getUrl());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getPid());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getPointName());
        db.dbUpdate();
    }

    @Override
    public boolean checkExist(AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from along_work_pedestrian_detection_alarm ");
        stringBuffer.append(" where pid = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getPid());
        db.set(index++, alongWorkPedestrianDetectionAlarm.getPointName());
        return db.dbQuery().size()>0;
    }

    @Override
    public List<AlongWorkPedestrianDetectionAlarm> listByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from along_work_pedestrian_detection_alarm a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from along_work ");
        stringBuffer.append("         where id = a.pid ");
        stringBuffer.append("           and start_time > ? ");
        stringBuffer.append("           and room_id = ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, dateStr);
        db.set(index++, roomId);
        return db.dbQuery(AlongWorkPedestrianDetectionAlarm.class);
    }
}
