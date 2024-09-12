package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.TaskInfoDao;
import com.inspur.industrialinspection.entity.TaskInfo;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务信息dao实现
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class TaskInfoDaoImpl implements TaskInfoDao {

    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private RequestService requestService;

    /**
     * 更新任务基本信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 19:58
     */
    @Override
    public void update(TaskInfo taskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update task_info ");
        stringBuffer.append("   set end_time = ?, exec_status = ? ");
        stringBuffer.append(" where task_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, taskInfo.getTaskId());
        db.set(3, taskInfo.getTaskId());
        db.dbUpdate();
    }
    /**
     * 获取任务明细
     * @param taskId
     * @return com.inspur.industrialinspection.entity.TaskInfo
     * @author kliu
     * @date 2022/5/24 19:59
     */
    @Override
    public TaskInfo getDetlById(long taskId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_info where task_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, taskId);
        List<TaskInfo> list = db.dbQuery(TaskInfo.class);
        if (list.size()==0) {
            throw new RuntimeException("获取任务失败，请检查传入的任务id");
        }
        return list.get(0);
    }

    /**
     * 根据机房id和日期获取任务执行实例，取大于日期的数据
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskInstance>
     * @author kliu
     * @date 2022/5/24 19:59
     * @update wzj 2022.6.9 按照开始时间升序
     */
    @Override
    public List<TaskInstance> listByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and b.room_id = ? ");
        stringBuffer.append("   and a.start_time > ?");
        stringBuffer.append("   order by a.start_time asc");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, dateStr);
        return db.dbQuery(TaskInstance.class);
    }

    /**
     * 分页查询任务信息
     * @param roomId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/5/24 20:00
     */
    @Override
    public PageBean list(long roomId, long robotId, int pageSize, int page) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from task_info a, room_info b ");
        stringBuffer.append(" where a.room_id = b.room_id ");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if(roomId>0){
            stringBuffer.append(" and b.room_id = ?");
        }
        if(robotId>0){
            stringBuffer.append(" and a.robot_id = ?");
        }
        String countSqlStr = stringBuffer.toString();

        stringBuffer.setLength(0);
        stringBuffer.append("select a.inspect_type_id, ");
        stringBuffer.append("       a.robot_id, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       a.task_id, ");
        stringBuffer.append("       (select robot_name from robot_info where robot_id = a.robot_id) robot_name, ");
        stringBuffer.append("       b.room_name, ");
        stringBuffer.append("       a.in_use, ");
        stringBuffer.append("       a.exec_time ");
        stringBuffer.append("  from task_info a, room_info b ");
        stringBuffer.append(" where a.room_id = b.room_id ");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if(roomId>0){
            stringBuffer.append(" and b.room_id = ?");
        }
        if(robotId>0){
            stringBuffer.append(" and a.robot_id = ?");
        }
        stringBuffer.append("   order by task_id desc ");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, parkId);
        if(roomId>0){
            db.set(index++, roomId);
        }
        if(robotId>0){
            db.set(index++, robotId);
        }
        PageBean objectPageBean = db.dbQueryPage(TaskInfo.class, countSqlStr, page, pageSize);
        return objectPageBean;
    }

    /**
     * 分页获取所有任务
     * @param roomId
     * @param robotId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/6/8 11:27
     */
    @Override
    public PageBean listWithoutPark(long roomId, long robotId, int pageSize, int page) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from task_info a, room_info b ");
        stringBuffer.append(" where a.room_id = b.room_id ");
        if(roomId>0){
            stringBuffer.append(" and b.room_id = ?");
        }
        if(robotId>0){
            stringBuffer.append(" and a.robot_id = ?");
        }
        String countSqlStr = stringBuffer.toString();

        stringBuffer.setLength(0);
        stringBuffer.append("select a.inspect_type_id, ");
        stringBuffer.append("       a.robot_id, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       a.task_id, ");
        stringBuffer.append("       (select robot_name from robot_info where robot_id = a.robot_id) robot_name, ");
        stringBuffer.append("       b.room_name, ");
        stringBuffer.append("       a.in_use, ");
        stringBuffer.append("       a.exec_time ");
        stringBuffer.append("  from task_info a, room_info b ");
        stringBuffer.append(" where a.room_id = b.room_id ");
        if(roomId>0){
            stringBuffer.append(" and b.room_id = ?");
        }
        if(robotId>0){
            stringBuffer.append(" and a.robot_id = ?");
        }
        stringBuffer.append("   order by task_id desc ");
        db.setSql(stringBuffer.toString());
        int index = 1;
        if(roomId>0){
            db.set(index++, roomId);
        }
        if(robotId>0){
            db.set(index++, robotId);
        }
        PageBean objectPageBean = db.dbQueryPage(TaskInfo.class, countSqlStr, page, pageSize);
        return objectPageBean;
    }

    /**
     * 添加任务信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:00
     */
    @Override
    public void add(TaskInfo taskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into task_info ");
        stringBuffer.append("  (inspect_type_id, robot_id, exec_time, in_use, room_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskInfo.getInspectTypeId());
        db.set(index++, taskInfo.getRobotId());
        db.set(index++, taskInfo.getExecTime());
        db.set(index++, taskInfo.getInUse());
        db.set(index++, taskInfo.getRoomId());
        db.dbUpdate();
    }

    /**
     * 更新任务执行时间
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:01
     */
    @Override
    public void updateExecTime(TaskInfo taskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update task_info set exec_time = ?, in_use = ? where task_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskInfo.getExecTime());
        db.set(index++, taskInfo.getInUse());
        db.set(index++, taskInfo.getTaskId());
        db.dbUpdate();
    }

    /**
     * 更新任务所有信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:01
     */
    @Override
    public void updateAll(TaskInfo taskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update task_info ");
        stringBuffer.append("   set inspect_type_id = ?, robot_id = ?, room_id = ?, exec_time = ?, in_use = ? ");
        stringBuffer.append(" where task_id = ?");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, taskInfo.getInspectTypeId());
        db.set(index++, taskInfo.getRobotId());
        db.set(index++, taskInfo.getRoomId());
        db.set(index++, taskInfo.getExecTime());
        db.set(index++, taskInfo.getInUse());
        db.set(index++, taskInfo.getTaskId());
        db.dbUpdate();
    }

    /**
     * 删除任务信息
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:01
     */
    @Override
    public void delete(TaskInfo taskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from task_info where task_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskInfo.getTaskId());
        db.dbUpdate();
    }

    /**
     * 获取启用中的任务
     * @return java.util.List
     * @author kliu
     * @date 2022/5/24 20:01
     */
    @Override
    public List list() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_info a where a.in_use  = '0' ");
        db.setSql(stringBuffer.toString());
        List<TaskInfo> list = db.dbQuery(TaskInfo.class);
        return list;
    }

    /**
     * 校验同类型的任务是否已经存在
     * @param taskInfo
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:02
     */
    @Override
    public boolean checkExistByInspectTypeRobot(TaskInfo taskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from task_info a where a.inspect_type_id = ? and robot_id = ? and room_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, taskInfo.getInspectTypeId());
        db.set(2, taskInfo.getRobotId());
        db.set(3, taskInfo.getRoomId());
        return db.dbQuery().size()>0;
    }

    /**
     * 校验巡检类型是否已经创建任务
     * @param inspectTypeId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:02
     */
    @Override
    public boolean checkExistByInspectType(long roomId, long inspectTypeId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from task_info a where a.inspect_type_id = ? and a.room_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, inspectTypeId);
        db.set(2, roomId);
        return db.dbQuery().size()>0;
    }

    @Override
    public void updateTaskStopExecute(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update task_info set in_use = '1' where robot_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, robotId);
        db.dbUpdate();
    }

    @Override
    public List<TaskInfo> getTasks() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_info");
        db.setSql(stringBuffer.toString());
        List<TaskInfo> list = db.dbQuery(TaskInfo.class);
        if(list.size() == 0){
            return null;
        }
        return list;
    }

    @Override
    public List<TaskInfo> getByRoomId(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_info where room_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1,roomId);
        List<TaskInfo> list = db.dbQuery(TaskInfo.class);
        if(list.size() == 0){
            return null;
        }
        return list;
    }
}
