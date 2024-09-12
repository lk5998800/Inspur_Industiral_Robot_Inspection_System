package com.inspur.industrialinspection.dao.impl;

import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ParticularPointInspectionTaskResultDao;
import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskResult;
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
public class ParticularPointInspectionTaskResultDaoImpl implements ParticularPointInspectionTaskResultDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加
     *
     * @param particularPointInspectionTaskResult
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    @Override
    public void add(ParticularPointInspectionTaskResult particularPointInspectionTaskResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into particular_point_inspection_task_result ");
        stringBuffer.append("  (instance_id, ");
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getSensor())){
            stringBuffer.append("   sensor, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getInfrared())){
            stringBuffer.append("   infrared, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
            stringBuffer.append("   alarm_light, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
            stringBuffer.append("   front, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
            stringBuffer.append("   after, ");
        }
        stringBuffer.append("   point_name) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ");
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getSensor())){
            stringBuffer.append("   ?, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getInfrared())){
            stringBuffer.append("   ?, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
            stringBuffer.append("   ?, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
            stringBuffer.append("   ?, ");
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
            stringBuffer.append("   ?, ");
        }
        stringBuffer.append("   ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, particularPointInspectionTaskResult.getInstanceId());

        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getSensor())){
            db.set(index++, particularPointInspectionTaskResult.getSensor());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getInfrared())){
            db.set(index++, particularPointInspectionTaskResult.getInfrared());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
            db.set(index++, particularPointInspectionTaskResult.getAlarmLight());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
            db.set(index++, particularPointInspectionTaskResult.getFront());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
            db.set(index++, particularPointInspectionTaskResult.getAfter());
        }
        db.set(index++, particularPointInspectionTaskResult.getPointName());
        db.dbUpdate();

    }

    /**
     * 添加
     *
     * @param particularPointInspectionTaskResult
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    @Override
    public void update(ParticularPointInspectionTaskResult particularPointInspectionTaskResult) {
        boolean hasBeforeData = false;
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update particular_point_inspection_task_result set ");
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getSensor())){
            stringBuffer.append("   sensor = ? ");
            hasBeforeData = true;
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getInfrared())){
            if (hasBeforeData){
                stringBuffer.append(" , ");
            }
            stringBuffer.append("   infrared = ? ");
            hasBeforeData = true;
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
            if (hasBeforeData){
                stringBuffer.append(" , ");
            }
            stringBuffer.append("   alarm_light = ? ");
            hasBeforeData = true;
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
            if (hasBeforeData){
                stringBuffer.append(" , ");
            }
            stringBuffer.append("   front = ? ");
            hasBeforeData = true;
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
            if (hasBeforeData){
                stringBuffer.append(" , ");
            }
            stringBuffer.append("   after = ? ");
        }
        stringBuffer.append(" where instance_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getSensor())){
            db.set(index++, particularPointInspectionTaskResult.getSensor());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getInfrared())){
            db.set(index++, particularPointInspectionTaskResult.getInfrared());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
            db.set(index++, particularPointInspectionTaskResult.getAlarmLight());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
            db.set(index++, particularPointInspectionTaskResult.getFront());
        }
        if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
            db.set(index++, particularPointInspectionTaskResult.getAfter());
        }
        db.set(index++, particularPointInspectionTaskResult.getInstanceId());
        db.dbUpdate();
    }

    /**
     * 校验数据是否存在
     *
     * @param particularPointInspectionTaskResult
     * @return boolean
     * @author kliu
     * @date 2022/9/7 14:46
     */
    @Override
    public boolean checkExist(ParticularPointInspectionTaskResult particularPointInspectionTaskResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from particular_point_inspection_task_result ");
        stringBuffer.append(" where instance_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, particularPointInspectionTaskResult.getInstanceId());
        return db.dbQuery().size()>0;
    }

    @Override
    public ParticularPointInspectionTaskResult getDetlById(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from particular_point_inspection_task_result ");
        stringBuffer.append(" where instance_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        List<ParticularPointInspectionTaskResult> list = db.dbQuery(ParticularPointInspectionTaskResult.class);
        if(list.size()==0){
            return null;
        }else{
            return list.get(0);
        }
    }
}
