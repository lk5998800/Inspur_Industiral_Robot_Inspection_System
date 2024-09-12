package com.inspur.industrialinspection.dao.impl;

import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ExplainTaskDao;
import com.inspur.industrialinspection.entity.ExplainTask;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author: LiTan
 * @description:    导览讲解dao层实现
 * @date:   2022-10-31 10:24:01
 */
@Repository
public class ExplainTaskDaoImpl implements ExplainTaskDao {
    @Autowired
    private BeanFactory beanFactory;
    @Override
    public ExplainTask getExplainTask(long id) {

        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from explain_task where id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, id);
        List<ExplainTask> list = db.dbQuery(ExplainTask.class);
        if (list.size() == 0) {
            throw new RuntimeException("传入的id不存在，请检查传入的数据");
        }
        return list.get(0);
    }

    @Override
    public PageBean pageList(long roomId, int pageSize, int pageNum, String status, String taskTime, String keyword) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select count(1) count from explain_task where room_id = ?");
        if (!StringUtils.isEmpty(keyword)){
            stringBuffer.append(" and task_name LIKE ? ");
        }
        if (!StringUtils.isEmpty(status)){
            stringBuffer.append(" and status = ? ");
        }
        if (!StringUtils.isEmpty(taskTime)){
            stringBuffer.append(" and task_time LIKE ? ");
        }
        String countSqlStr = stringBuffer.toString();
        stringBuffer.setLength(0);
        stringBuffer.append("SELECT * FROM explain_task where room_id = ?");
        if (!StringUtils.isEmpty(keyword)){
            stringBuffer.append(" and task_name LIKE ? ");
        }
        if (!StringUtils.isEmpty(status)){
            stringBuffer.append(" and status = ? ");
        }
        if (!StringUtils.isEmpty(taskTime)){
            stringBuffer.append(" and task_time LIKE ? ");
        }
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, roomId);
        if (!StringUtils.isEmpty(keyword)){
            db.set(index++, '%' + keyword + '%');
        }
        if (!StringUtils.isEmpty(status)){
            db.set(index++, status);
        }
        if (!StringUtils.isEmpty(taskTime)){
            db.set(index++, taskTime + '%');
        }
        PageBean objectPageBean = db.dbQueryPage(ExplainTask.class, countSqlStr, pageNum, pageSize);
        return objectPageBean;
    }

    @Override
    public void update(ExplainTask explainTask) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update explain_task set task_name = ? ,task_type = ? ,task_time = ? ,task_describe = ? ,points = ? ,start_time = ? ,end_time = ? ,status = ? ,reason = ? where id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, explainTask.getTaskName());
        db.set(index++, explainTask.getTaskType());
        db.set(index++, explainTask.getTaskTime());
        db.set(index++, explainTask.getTaskDescribe());
        db.set(index++, explainTask.getPoints());
        db.set(index++, explainTask.getStartTime());
        db.set(index++, explainTask.getEndTime());
        db.set(index++, explainTask.getStatus());
        db.set(index++, explainTask.getReason());
        db.set(index++, explainTask.getId());
        db.dbUpdate();
    }

    @Override
    public long addAndReturnId(ExplainTask explainTask) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into explain_task (id, room_id, task_name, task_type, task_time, task_describe,points, status, create_time, create_user_id) values (?, ?, ?, ?, ?,  ?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, explainTask.getRoomId());
        db.set(index++, explainTask.getTaskName());
        db.set(index++, explainTask.getTaskType());
        db.set(index++, explainTask.getTaskTime());
        db.set(index++, explainTask.getTaskDescribe());
        db.set(index++, explainTask.getPoints());
        db.set(index++, explainTask.getStatus());
        db.set(index++, explainTask.getCreateTime());
        db.set(index++, explainTask.getCreateUserId());
        return db.dbUpdateAndReturnId();
    }

    @Override
    public void delete(int id) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("delete from explain_task where id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, id);
        db.dbUpdate();
    }

    @Override
    public List<ExplainTask> getListByCron() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from explain_task where status in ('wait', 'no')");
        db.setSql(stringBuffer.toString());
        List<ExplainTask> list = db.dbQuery(ExplainTask.class);
        return list;
    }

    @Override
    public List<ExplainTask> List(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        if(roomId == 0){
            stringBuffer.append("select * from explain_task");
            db.setSql(stringBuffer.toString());
        }else{
            stringBuffer.append("select * from explain_task where room_id = ?");
            db.setSql(stringBuffer.toString());
            db.set(1, roomId);
        }
        List<ExplainTask> list = db.dbQuery(ExplainTask.class);
        return list;
    }
}
