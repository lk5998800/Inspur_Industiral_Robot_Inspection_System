package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.TaskDetectionSumDao;
import com.inspur.industrialinspection.entity.TaskDetectionSum;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * @author: kliu
 * @description: 任务检测项汇总dao实现
 * @date: 2022/4/8 15:25
 */
@Repository
public class TaskDetectionSumDaoImpl implements TaskDetectionSumDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加任务检测项汇总数据
     * @param taskDetectionSum
     * @author kliu
     * @date 2022/5/24 19:18
     */
    @Override
    public void add(TaskDetectionSum taskDetectionSum) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into task_detection_sum ");
        stringBuffer.append("  (instance_id, detection_id, avg, max, min, median, abnormal_count, count) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionSum.getInstanceId());
        db.set(index++, taskDetectionSum.getDetectionId());
        db.set(index++, taskDetectionSum.getAvg());
        db.set(index++, taskDetectionSum.getMax());
        db.set(index++, taskDetectionSum.getMin());
        db.set(index++, taskDetectionSum.getMedian());
        db.set(index++, taskDetectionSum.getAbnormalCount());
        db.set(index++, taskDetectionSum.getCount());
        db.dbUpdate();

    }

    /**
     * 更新任务检测项汇总
     * @param taskDetectionSum
     * @author kliu
     * @date 2022/5/24 19:18
     */
    @Override
    public void update(TaskDetectionSum taskDetectionSum) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update task_detection_sum ");
        stringBuffer.append("   set avg = ?, max = ?, min = ?, median = ?, abnormal_count = ?, count = ? ");
        stringBuffer.append(" where instance_id = ? and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionSum.getAvg());
        db.set(index++, taskDetectionSum.getMax());
        db.set(index++, taskDetectionSum.getMin());
        db.set(index++, taskDetectionSum.getMedian());
        db.set(index++, taskDetectionSum.getAbnormalCount());
        db.set(index++, taskDetectionSum.getCount());
        db.set(index++, taskDetectionSum.getInstanceId());
        db.set(index++, taskDetectionSum.getDetectionId());
        db.dbUpdate();

    }

    /**
     * 校验数据是否存在
     * @param taskDetectionSum
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:18
     */
    @Override
    public boolean checkExist(TaskDetectionSum taskDetectionSum) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from task_detection_sum ");
        stringBuffer.append(" where instance_id = ? and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionSum.getInstanceId());
        db.set(index++, taskDetectionSum.getDetectionId());
        if(db.dbQuery().size()>0){
            return true;
        }
        return false;
    }

    /**
     * 获取明细
     * @param taskDetectionSum
     * @return com.inspur.industrialinspection.entity.TaskDetectionSum
     * @author kliu
     * @date 2022/5/24 19:18
     */
    @Override
    public TaskDetectionSum getDetlById(TaskDetectionSum taskDetectionSum) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_detection_sum ");
        stringBuffer.append(" where instance_id = ? and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskDetectionSum.getInstanceId());
        db.set(index++, taskDetectionSum.getDetectionId());
        return (TaskDetectionSum) db.dbQuery(TaskDetectionSum.class);
    }

    /**
     * 根据任务实例获取任务检测项汇总数据
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.TaskDetectionSum>
     * @author kliu
     * @date 2022/5/24 19:19
     */
    @Override
    public List<TaskDetectionSum> list(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from task_detection_sum ");
        stringBuffer.append(" where instance_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        return db.dbQuery(TaskDetectionSum.class);
    }

    /**
     * 获取近期一段时间内的异常数据
     * param roomId
     * @param minDate
     * @return java.util.List
     * @author kliu
     * @date 2022/5/24 19:19
     */
    @Override
    public List getRecentAbnormalData(long roomId, String minDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select detection_date, cast(sum(abnormal_count) as SIGNED) abnormal_count ");
        stringBuffer.append("  from room_detection_sum_day a ");
        stringBuffer.append(" where a.detection_date >= ? ");
        stringBuffer.append("   and a.room_id = ? ");
        stringBuffer.append(" group by detection_date ");
        stringBuffer.append(" order by a.detection_date asc");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, minDate);
        db.set(index++, roomId);
        return db.dbQuery();
    }
}

