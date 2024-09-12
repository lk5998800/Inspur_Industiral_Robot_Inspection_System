package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author: kliu
 * @description: 任务检测项结果dao实现
 * @date: 2022/4/8 15:25
 */
@Repository
public class TaskDetectionResultDaoImpl implements TaskDetectionResultDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加任务检测结果
     *
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    @Override
    public void add(TaskDetectionResult taskDetectionResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into task_detection_result ");
        stringBuffer.append("  (instance_id, point_name, sensor, infrared, alarm_light, update_time, analyse_flag, fire_extinguisher) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, now(), '1', ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionResult.getInstanceId());
        db.set(index++, taskDetectionResult.getPointName());
        db.set(index++, taskDetectionResult.getSensor());
        db.set(index++, taskDetectionResult.getInfrared());
        db.set(index++, taskDetectionResult.getAlarmLight());
        db.set(index++, taskDetectionResult.getFireExtinguisher());
        db.dbUpdate();
    }

    /**
     * 更新传感器数据
     *
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    @Override
    public void updateSensor(TaskDetectionResult taskDetectionResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_detection_result ");
        stringBuffer.append("   set sensor = ?, update_time = now(), analyse_flag = '1' ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionResult.getSensor());
        db.set(index++, taskDetectionResult.getInstanceId());
        db.set(index++, taskDetectionResult.getPointName());
        db.dbUpdate();
    }

    /**
     * 更新红外测温数据
     *
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    @Override
    public void updateInfrared(TaskDetectionResult taskDetectionResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_detection_result ");
        stringBuffer.append("   set infrared = ?, update_time = now(), analyse_flag = '1' ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionResult.getInfrared());
        db.set(index++, taskDetectionResult.getInstanceId());
        db.set(index++, taskDetectionResult.getPointName());
        db.dbUpdate();

    }

    /**
     * 更新报警灯数据
     *
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:11
     */
    @Override
    public void updateAlarmLight(TaskDetectionResult taskDetectionResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_detection_result ");
        stringBuffer.append("   set alarm_light = ?, update_time = now(), analyse_flag = '1' ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionResult.getAlarmLight());
        db.set(index++, taskDetectionResult.getInstanceId());
        db.set(index++, taskDetectionResult.getPointName());
        db.dbUpdate();
    }

    @Override
    public void updateFireExtinguisher(TaskDetectionResult taskDetectionResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_detection_result ");
        stringBuffer.append("   set fire_extinguisher = ?, update_time = now() ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionResult.getFireExtinguisher());
        db.set(index++, taskDetectionResult.getInstanceId());
        db.set(index++, taskDetectionResult.getPointName());
        db.dbUpdate();
    }

    /**
     * 设置检测项分析完成
     *
     * @param taskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:12
     */
    @Override
    public void updateAnalyseComplete(TaskDetectionResult taskDetectionResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_detection_result ");
        stringBuffer.append("   set analyse_flag = '0' ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionResult.getInstanceId());
        db.set(index++, taskDetectionResult.getPointName());
        db.dbUpdate();
    }

    /**
     * 判断当前机柜是否存在检测项数据
     *
     * @param taskDetectionResult
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:12
     */
    @Override
    public boolean checkExist(TaskDetectionResult taskDetectionResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 from task_detection_result ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, taskDetectionResult.getInstanceId());
        db.set(2, taskDetectionResult.getPointName());
        return db.dbQuery().size() > 0;
    }

    /**
     * 根据实例id和点位名称获取检测项结果
     *
     * @param instanceId
     * @param pointName
     * @return com.inspur.industrialinspection.entity.TaskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:12
     */
    @Override
    public TaskDetectionResult getDetlByInstanceIdAndPointName(long instanceId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_detection_result ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, pointName);
        return (TaskDetectionResult) db.dbQuery(TaskDetectionResult.class).get(0);
    }

    /**
     * 根据实例id和点位名称获取检测项结果-锁表
     *
     * @param instanceId
     * @param pointName
     * @return com.inspur.industrialinspection.entity.TaskDetectionResult
     * @author kliu
     * @date 2022/6/7 8:55
     */
    @Override
    public TaskDetectionResult getDetlByInstanceIdAndPointNameForUpdate(long instanceId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_detection_result ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and point_name = ? for update ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, pointName);
        return (TaskDetectionResult) db.dbQuery(TaskDetectionResult.class).get(0);
    }

    /**
     * 根据实例id获取检测项结果
     *
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/5/24 19:13
     */
    @Override
    public List<TaskDetectionResult> list(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_detection_result ");
        stringBuffer.append(" where instance_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        return (List<TaskDetectionResult>) db.dbQuery(TaskDetectionResult.class);
    }

    @Override
    public List<TaskDetectionResult> abnormalList(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_detection_result ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and (infrared like '%abnormal%' or alarm_light like '%abnormal%') ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        return (List<TaskDetectionResult>) db.dbQuery(TaskDetectionResult.class);
    }

    /**
     * 依据实例id获取机柜检测项数量
     *
     * @param instanceId
     * @return int
     * @author kliu
     * @date 2022/5/24 19:13
     */
    @Override
    public int count(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select count(1) count from task_detection_result ");
        stringBuffer.append(" where instance_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        List<Map<String, Object>> list = db.dbQuery();
        if (list.size() == 0) {
            return 0;
        }
        int count = Integer.parseInt(list.get(0).get("count") + "");
        return count;
    }

    /**
     * 根据机房和日期获取检测项结果
     *
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/5/24 19:13
     */
    @Override
    public List<TaskDetectionResult> listByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where a.instance_id = b.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info c ");
        stringBuffer.append("                 where c.task_id = b.task_id ");
        stringBuffer.append("                   and c.room_id = ?) ");
        stringBuffer.append("           and b.start_time > ?");
        stringBuffer.append("           and b.start_time < ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        db.set(index++, DateUtil.offsetDay(DateUtil.parse(dateStr, "yyyy-MM-dd"), 1).toString("yyyy-MM-dd"));
        return (List<TaskDetectionResult>) db.dbQuery(TaskDetectionResult.class);
    }

    @Override
    public List<TaskDetectionResult> listByRoomIdAndDate(long roomId, String dateStr, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where a.instance_id = b.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info c ");
        stringBuffer.append("                 where c.task_id = b.task_id ");
        stringBuffer.append("                   and c.room_id = ?) ");
        stringBuffer.append("           and b.start_time > ?");
        stringBuffer.append("           and b.start_time < ?)");
        stringBuffer.append("  and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        db.set(index++, DateUtil.offsetDay(DateUtil.parse(dateStr, "yyyy-MM-dd"), 1).toString("yyyy-MM-dd"));
        db.set(index++, pointName);
        return (List<TaskDetectionResult>) db.dbQuery(TaskDetectionResult.class);
    }

    /**
     * 根据机房和日期获取检测项机柜数量
     *
     * @param roomId
     * @param dateStr
     * @return int
     * @author kliu
     * @date 2022/5/24 19:13
     */
    @Override
    public int countByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where a.instance_id = b.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info c ");
        stringBuffer.append("                 where c.task_id = b.task_id ");
        stringBuffer.append("                   and c.room_id = ?) ");
        stringBuffer.append("           and b.start_time > ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        List<Map<String, Object>> list = db.dbQuery();
        if (list.size() == 0) {
            return 0;
        }
        int count = Integer.parseInt(list.get(0).get("count") + "");
        return count;
    }

    @Override
    public List pointCountByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select point_name, count(1) count ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where a.instance_id = b.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info c ");
        stringBuffer.append("                 where c.task_id = b.task_id ");
        stringBuffer.append("                   and c.room_id = ?) ");
        stringBuffer.append("           and b.start_time > ?)");
        stringBuffer.append(" group by point_name ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        List list = db.dbQuery();
        return list;
    }

    /**
     * 根据实例id获取最新检测结果数据
     *
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskDetectionResult
     * @author kliu
     * @date 2022/5/24 19:14
     */
    @Override
    public TaskDetectionResult getDetlByMaxUpdateTime(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and update_time = (select max(update_time) from task_detection_result where instance_id = ?) ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, instanceId);
        List list = db.dbQuery(TaskDetectionResult.class);
        if (list.size() == 0) {
            return null;
        }
        return (TaskDetectionResult) list.get(0);
    }

    /**
     * 获取未分析的检测项结果数据
     *
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionResult>
     * @author kliu
     * @date 2022/5/24 19:14
     */
    @Override
    public List<TaskDetectionResult> unAnalyseList() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where analyse_flag = '1' ");
        db.setSql(stringBuffer.toString());
        List<TaskDetectionResult> list = db.dbQuery(TaskDetectionResult.class);
        return list;
    }

    @Override
    public List<TaskDetectionResult> getRecentInfraredPic(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select a.infrared, a.update_time ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where b.instance_id = a.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info ");
        stringBuffer.append("                 where b.task_id = task_id ");
        stringBuffer.append("                   and robot_id = ?)) ");
        stringBuffer.append("   and a.infrared is not null ");
        stringBuffer.append(" order by update_time desc limit 1");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        return db.dbQuery(TaskDetectionResult.class);
    }

    @Override
    public List<TaskDetectionResult> getRecentAlarmLightPic(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select a.alarm_light, a.update_time ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where b.instance_id = a.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info ");
        stringBuffer.append("                 where b.task_id = task_id ");
        stringBuffer.append("                   and robot_id = ?)) ");
        stringBuffer.append("   and a.alarm_light is not null ");
        stringBuffer.append(" order by update_time desc limit 1");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        return db.dbQuery(TaskDetectionResult.class);
    }

    @Override
    public TaskDetectionResult getByInstanceIdNewestResult(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select *  from task_detection_result where instance_id = ?  ORDER BY update_time DESC");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        List<TaskDetectionResult> list = db.dbQuery(TaskDetectionResult.class);
        if (list == null) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public int normalCabinetcountByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where instance_id = a.instance_id ");
        stringBuffer.append("           and exists ");
        stringBuffer.append("         (select 1 ");
        stringBuffer.append("                  from task_info ");
        stringBuffer.append("                 where room_id = ? ");
        stringBuffer.append("                   and task_id = b.task_id) ");
        stringBuffer.append("           and b.start_time > ?) ");
        stringBuffer.append("   and not exists ");
        stringBuffer.append(" (select 1 from warn_info where task_log_id = a.task_log_id)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startDate);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public int cabinetCountByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(distinct point_name) count ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where b.start_time > ? ");
        stringBuffer.append("           and a.instance_id = b.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info ");
        stringBuffer.append("                 where room_id = ? ");
        stringBuffer.append("                   and task_id = b.task_id))");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, startDate);
        db.set(index++, roomId);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public int cabinetWarnCountByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(distinct point_name) count ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where instance_id = a.instance_id ");
        stringBuffer.append("           and exists ");
        stringBuffer.append("         (select 1 ");
        stringBuffer.append("                  from task_info ");
        stringBuffer.append("                 where room_id = ? ");
        stringBuffer.append("                   and task_id = b.task_id) ");
        stringBuffer.append("           and b.start_time > ?) ");
        stringBuffer.append("   and exists ");
        stringBuffer.append(" (select 1 from warn_info where task_log_id = a.task_log_id)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startDate);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public List list(long roomId, String pointName, String detectionId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where a.instance_id = b.instance_id ");
        stringBuffer.append("           and b.start_time > ? ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info c ");
        stringBuffer.append("                 where b.task_id = c.task_id ");
        stringBuffer.append("                   and c.room_id = ?)) ");
        stringBuffer.append("   and exists (select 1 ");
        stringBuffer.append("          from warn_info d ");
        stringBuffer.append("         where d.task_log_id = a.task_log_id ");
        stringBuffer.append("           and d.detection_id = ?) ");
        stringBuffer.append("   and a.point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, dateStr);
        db.set(index++, roomId);
        db.set(index++, detectionId);
        db.set(index++, pointName);
        return db.dbQuery(TaskDetectionResult.class);
    }

    @Override
    public List<TaskDetectionResult> getFireExtinguishers(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_detection_result where instance_id = ? and fire_extinguisher is not null");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        return db.dbQuery(TaskDetectionResult.class);
    }

    @Override
    public List listFireExtinguisher(long roomId, String pointName, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where a.instance_id = b.instance_id ");
        stringBuffer.append("           and b.start_time > ? ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info c ");
        stringBuffer.append("                 where b.task_id = c.task_id ");
        stringBuffer.append("                   and c.room_id = ?)) ");
        stringBuffer.append("   and a.point_name = ? ");
        stringBuffer.append("   and a.fire_extinguisher is not null ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, dateStr);
        db.set(index++, roomId);
        db.set(index++, pointName);
        return db.dbQuery(TaskDetectionResult.class);
    }
}

