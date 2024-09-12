package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.TaskDao;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.entity.TaskInstance;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务dao实现
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class TaskDaoImpl implements TaskDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 根据机房id获取最近一次任务实例信息
     * @param roomId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 19:09
     */
    @Override
    public TaskInstance getRecentTaskByRoomId(long roomId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a ");
        stringBuffer.append(" where exists ");
        stringBuffer.append(" (select 1 ");
        stringBuffer.append("          from task_info b ");
        stringBuffer.append("         where a.task_id = b.task_id ");
        stringBuffer.append("           and b.room_id = ?) ");
        stringBuffer.append("   and exec_status in ('running', 'terminate','end') ");
        stringBuffer.append(" order by start_time desc limit 1");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if(list.size()==0){
            throw new RuntimeException("当前机房未运行任务");
        }
        return list.get(0);

    }

    /**
     * 根据机房id获取最近一次有数据的任务实例信息
     *
     * @param roomId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/5/24 19:09
     */
    @Override
    public TaskInstance getRecentHasDataTaskByRoomId(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from task_instance a ");
        stringBuffer.append(" where exists ");
        stringBuffer.append(" (select 1 ");
        stringBuffer.append("          from task_info b ");
        stringBuffer.append("         where a.task_id = b.task_id ");
        stringBuffer.append("           and b.room_id = ?) ");
        stringBuffer.append("   and exec_status in ('running', 'terminate','end') ");
        stringBuffer.append("   and exists (select 1 from task_detection_result where instance_id = a.instance_id) ");
        stringBuffer.append(" order by start_time desc limit 1");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        List<TaskInstance> list = db.dbQuery(TaskInstance.class);
        if(list.size()==0){
            throw new RuntimeException("当前机房未运行任务");
        }
        return list.get(0);
    }

    /**
     * 判断当前机房是否存在执行的任务
     * @param roomId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:09
     */
    @Override
    public boolean checkTaskExistByRoomId(long roomId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from task_instance a, task_info b ");
        stringBuffer.append(" where a.task_id = b.task_id ");
        stringBuffer.append("   and b.room_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        List list = db.dbQuery();
        if(list.size()==0){
            return false;
        }
        return true;
    }

    /**
     * 依据任务实例获取机房id
     * @param instanceId
     * @return long
     * @author kliu
     * @date 2022/5/24 19:09
     */
    @Override
    public long getRoomIdByTaskId(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select c.room_id ");
        stringBuffer.append("  from task_instance a, task_info c ");
        stringBuffer.append(" where a.task_id = c.task_id ");
        stringBuffer.append("   and a.instance_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        List<RoomInfo> list = db.dbQuery(RoomInfo.class);
        if(list.size()==0){
            throw new RuntimeException("根据任务实例id【"+instanceId+"】获取机房信息失败，请检查传入的任务id");
        }
        return list.get(0).getRoomId();

    }
}
