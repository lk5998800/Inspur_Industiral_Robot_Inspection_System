package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 任务实例信息dao实现
 *
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class TaskInstanceDaoImpl implements TaskInstanceDao {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private RequestService requestService;

    /**
     * 添加任务实例并返回id
     *
     * @param taskInstance
     * @return long
     * @author kliu
     * @date 2022/5/24 20:09
     */
    @Override
    public long addAndReturnId(TaskInstance taskInstance) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into task_instance ");
        stringBuffer.append("  (instance_id, task_id, start_time, exec_status) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?)");
        db.setSql(stringBuffer.toString());
        db.set(1, taskInstance.getTaskId());
        db.set(2, taskInstance.getStartTime());
        db.set(3, taskInstance.getExecStatus());
        return db.dbUpdateAndReturnId();
    }

    /**
     * 更新任务执行实例
     *
     * @param taskInstance
     * @return void
     * @author kliu
     * @date 2022/5/24 20:09
     */
    @Override
    public void update(TaskInstance taskInstance) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_instance ");
        stringBuffer.append("   set end_time = ?, exec_status = ? ");
        stringBuffer.append(" where instance_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, taskInstance.getEndTime());
        db.set(2, taskInstance.getExecStatus());
        db.set(3, taskInstance.getInstanceId());
        db.dbUpdate();
    }

    @Override
    public void updateTaskJsonCompress(TaskInstance taskInstance) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_instance ");
        stringBuffer.append("   set task_json_compress = ? ");
        stringBuffer.append(" where instance_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, taskInstance.getTaskJsonCompress());
        db.set(2, taskInstance.getInstanceId());
        db.dbUpdate();
    }

    /**
     * 获取实例明细
     *
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 20:10
     */
    @Override
    public TaskInstance getDetlById(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_instance where instance_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if (list.size() == 0) {
            throw new RuntimeException("实例id【" + instanceId + "】未获取到数据，请检查");
        }
        return list.get(0);
    }

    /**
     * 获取实例明细-锁表
     *
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 20:10
     */
    @Override
    public TaskInstance getDetlByIdForUpdate(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_instance where instance_id = ? for update ");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if (list.size() == 0) {
            throw new RuntimeException("实例id【" + instanceId + "】未获取到数据，请检查");
        }
        return list.get(0);
    }

    /**
     * 根据机房和日期获取实例
     *
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInfo>
     * @author kliu
     * @date 2022/5/24 20:10
     */
    @Override
    public List<TaskInstance> getTaskByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_info ");
        stringBuffer.append("         where room_id = ? ");
        stringBuffer.append("           and a.task_id = task_id) ");
        stringBuffer.append("   and a.start_time > ?");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, dateStr);
        return db.dbQuery(TaskInstance.class);
    }

    /**
     * 分页查询任务实例
     *
     * @param roomId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/5/24 20:10
     */
    @Override
    public PageBean list(long roomId, long robotId, int pageSize, int page) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and a.exec_status <> 'create'");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if (roomId > 0) {
            stringBuffer.append("   and b.room_id = ?");
        }
        if (robotId > 0) {
            stringBuffer.append("   and b.robot_id = ?");
        }
        String countSqlStr = stringBuffer.toString();

        stringBuffer.setLength(0);
        stringBuffer.append("select a.instance_id, ");
        stringBuffer.append("       a.start_time, ");
        stringBuffer.append("       a.end_time, ");
        stringBuffer.append("       a.exec_status, ");
        stringBuffer.append("       b.inspect_type_id, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       b.task_id, ");
        stringBuffer.append("       (select robot_name from robot_info where robot_id = b.robot_id) robot_name, ");
        stringBuffer.append("       (select room_name from room_info where room_id = b.room_id) room_name ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and a.exec_status <> 'create'");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if (roomId > 0) {
            stringBuffer.append("   and b.room_id = ?");
        }
        if (robotId > 0) {
            stringBuffer.append("   and b.robot_id = ?");
        }
        stringBuffer.append(" order by if (exec_status ='running',0,1), start_time desc");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, parkId);
        if (roomId > 0) {
            db.set(index++, roomId);
        }
        if (robotId > 0) {
            db.set(index++, robotId);
        }
        PageBean objectPageBean = db.dbQueryPage(TaskInstance.class, countSqlStr, page, pageSize);
        return objectPageBean;
    }

    @Override
    public List list(int parkId, long roomId, long robotId, String startTime, String endTime) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.setLength(0);
        stringBuffer.append("select a.instance_id, ");
        stringBuffer.append("       a.start_time, ");
        stringBuffer.append("       a.end_time, ");
        stringBuffer.append("       a.exec_status, ");
        stringBuffer.append("       b.inspect_type_id, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       b.robot_id, ");
        stringBuffer.append("       b.task_id, ");
        stringBuffer.append("       (select robot_name from robot_info where robot_id = b.robot_id) robot_name, ");
        stringBuffer.append("       (select room_name from room_info where room_id = b.room_id) room_name ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and a.exec_status <> 'create'");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        stringBuffer.append("   and a.start_time between ? and ? ");
        if (roomId > 0) {
            stringBuffer.append("   and b.room_id = ?");
        }
        if (robotId > 0) {
            stringBuffer.append("   and b.robot_id = ?");
        }
        stringBuffer.append(" order by if (exec_status ='running',0,1), start_time desc");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, parkId);
        db.set(index++, startTime);
        db.set(index++, endTime);
        if (roomId > 0) {
            db.set(index++, roomId);
        }
        if (robotId > 0) {
            db.set(index++, robotId);
        }
        return db.dbQuery(TaskInstance.class);
    }

    /**
     * 校验实例是否存在
     *
     * @param instanceId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:10
     */
    @Override
    public boolean checkExist(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 from task_instance where instance_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        return db.dbQuery().size() > 0;
    }

    /**
     * 校验任务是否执行过实例
     *
     * @param taskId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:11
     */
    @Override
    public boolean checkExistByTaskId(long taskId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 from task_instance where task_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, taskId);
        return db.dbQuery().size() > 0;
    }

    @Override
    public TaskInstance getLatestTaskInstanceByRobotId(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and b.robot_id = ? ");
        stringBuffer.append("   and a.start_time = (select max(start_time) ");
        stringBuffer.append("                         from task_instance c, task_info d ");
        stringBuffer.append("                        where c.task_id = d.task_id ");
        stringBuffer.append("                          and d.robot_id = ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        db.set(index++, robotId);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<TaskInstance> getRunningTaskInstanceByRobotId(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a ");
        stringBuffer.append(" where a.exec_status = 'running' ");
        stringBuffer.append("   and exists (select 1 ");
        stringBuffer.append("          from task_info ");
        stringBuffer.append("         where robot_id = ? ");
        stringBuffer.append("           and task_id = a.task_id)");
        stringBuffer.append("  order by start_time asc ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        return db.dbQuery(TaskInstance.class);
    }

    @Override
    public List<TaskInstance> getTaskInstanceByRoomIdAndDate(long roomId, String qsrq, String zzrq) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a ");
        stringBuffer.append(" where EXISTS (select 1 ");
        stringBuffer.append("          from task_info ");
        stringBuffer.append("         where task_id = a.task_id ");
        stringBuffer.append("           and room_id = ?) ");
        stringBuffer.append("   and start_time between ? and ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, qsrq);
        db.set(index++, zzrq);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        return list;
    }

    @Override
    public List getAllInstanceByRoomId(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select distinct instance_id ");
        stringBuffer.append("  from task_detection_result a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_instance b ");
        stringBuffer.append("         where a.instance_id = b.instance_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_info c ");
        stringBuffer.append("                 where c.task_id = b.task_id ");
        stringBuffer.append("                   and c.room_id = ?))");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        return db.dbQuery(TaskInstance.class);
    }

    @Override
    public List<TaskInstance> getPictureList(long roomId, String startTime, String endTime, String inspectTypeArr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where b.room_id = ? ");
        stringBuffer.append("   and a.task_id = b.task_id ");
        if (!StringUtils.isEmpty(inspectTypeArr)) {
            stringBuffer.append("     b. inspect_type_id in (" + inspectTypeArr + ")) ");
        }
        stringBuffer.append("   and a.start_time between ? and ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startTime);
        db.set(index++, DateUtil.offsetDay(DateUtil.parse(endTime, "yyyy-MM-dd"), 1).toString("yyyy-MM-dd"));
        return db.dbQuery(TaskInstance.class);
    }

    @Override
    public TaskInstance getTaskById(Long taskId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from task_instance where instance_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, taskId);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public void updateById(TaskInstance task) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update task_instance ");
        stringBuffer.append("   set end_time = ?, exec_status = ?");
        stringBuffer.append("   where instance_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, task.getEndTime());
        db.set(2, task.getExecStatus());
        db.dbUpdate();
    }

    @Override
    public TaskInstance getSuspendTask(long robotId, String taskStatus) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT s.* FROM `task_info` i LEFT JOIN task_instance s on i.task_id = s.task_id  where i.robot_id = ? and s.exec_status = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, robotId);
        db.set(2, taskStatus);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public TaskInstance getByRoomIDNewTask(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select task_instance.* from task_info INNER JOIN task_instance ON task_info.task_id = task_instance.task_id WHERE task_info.room_id = ? order by task_instance.start_time DESC LIMIT 1");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public int countByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from task_instance a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_info ");
        stringBuffer.append("         where room_id = ? ");
        stringBuffer.append("           and task_id = a.task_id) ");
        stringBuffer.append("   and a.start_time > ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startDate);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public List<TaskInstance> getTaskInstances(long robotId, String taskStatus) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a ");
        stringBuffer.append(" where a.exec_status = ? ");
        stringBuffer.append("   and exists (select 1 ");
        stringBuffer.append("          from task_info ");
        stringBuffer.append("         where robot_id = ? ");
        stringBuffer.append("           and task_id = a.task_id)");
        stringBuffer.append("  order by start_time asc ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskStatus);
        db.set(index++, robotId);
        return db.dbQuery(TaskInstance.class);
    }

    @Override
    public PageBean list(int parkId, long roomId, String startTimeStart, String startTimeEnd, String endTimeStart, String endTimeEnd, String inspectTypeIdIn, int pageSize, int page) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and a.exec_status <> 'create'");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if (roomId > 0) {
            stringBuffer.append("   and b.room_id = ?");
        }
        if (!StringUtils.isEmpty(startTimeStart)){
            stringBuffer.append("   and a.start_time between ? and ? ");
        }
        if (!StringUtils.isEmpty(endTimeStart)){
            stringBuffer.append("   and a.end_time between ? and ? ");
        }
        if (!StringUtils.isEmpty(inspectTypeIdIn)){
            stringBuffer.append("   and b.inspect_type_id in ("+inspectTypeIdIn+") ");
        }
        String countSqlStr = stringBuffer.toString();

        stringBuffer.setLength(0);
        stringBuffer.append("select a.instance_id, ");
        stringBuffer.append("       a.start_time, ");
        stringBuffer.append("       a.end_time, ");
        stringBuffer.append("       a.exec_status, ");
        stringBuffer.append("       b.inspect_type_id, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       b.task_id, ");
        stringBuffer.append("       b.cycle_type, ");
        stringBuffer.append("       (select robot_name from robot_info where robot_id = b.robot_id) robot_name, ");
        stringBuffer.append("       (select room_name from room_info where room_id = b.room_id) room_name ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and a.exec_status <> 'create'");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if (roomId > 0) {
            stringBuffer.append("   and b.room_id = ?");
        }
        if (!StringUtils.isEmpty(startTimeStart)){
            stringBuffer.append("   and a.start_time between ? and ? ");
        }
        if (!StringUtils.isEmpty(endTimeStart)){
            stringBuffer.append("   and a.end_time between ? and ? ");
        }
        if (!StringUtils.isEmpty(inspectTypeIdIn)){
            stringBuffer.append("   and b.inspect_type_id in ("+inspectTypeIdIn+") ");
        }
        stringBuffer.append(" order by if (exec_status ='running',0,1), start_time desc");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, parkId);
        if (roomId > 0) {
            db.set(index++, roomId);
        }
        if (!StringUtils.isEmpty(startTimeStart)){
            db.set(index++, startTimeStart);
            db.set(index++, startTimeEnd);
        }
        if (!StringUtils.isEmpty(endTimeStart)){
            db.set(index++, endTimeStart);
            db.set(index++, endTimeEnd);
        }
        if (!StringUtils.isEmpty(inspectTypeIdIn)){
            stringBuffer.append("   and b.inspect_type_id in ("+inspectTypeIdIn+") ");
        }
        PageBean objectPageBean = db.dbQueryPage(TaskInstance.class, countSqlStr, page, pageSize);
        return objectPageBean;
    }
}
