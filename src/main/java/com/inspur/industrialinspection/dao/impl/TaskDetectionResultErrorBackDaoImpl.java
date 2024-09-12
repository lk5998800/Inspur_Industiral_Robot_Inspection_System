package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.TaskDetectionResultErrorBackDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author: kliu
 * @description: 任务检测项结果dao实现
 * @date: 2022/4/8 15:25
 */
@Repository
public class TaskDetectionResultErrorBackDaoImpl implements TaskDetectionResultErrorBackDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public void add(long taskLogId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(" insert into task_detection_result_error_back ");
        stringBuffer.append(" select * from task_detection_result where task_log_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskLogId);
        db.dbUpdate();
    }

    /**
     * 校验数据是否存在
     *
     * @param taskLogId
     * @return boolean
     * @author kliu
     * @date 2022/8/6 10:18
     */
    @Override
    public boolean checkExist(long taskLogId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append(" select 1 from task_detection_result_error_back where task_log_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskLogId);
        return db.dbQuery().size()>0;
    }
}

