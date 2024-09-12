package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.AlongWorkDao;
import com.inspur.industrialinspection.entity.AlongWork;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 随工任务dao实现
 * @author kliu
 * @date 2022/6/13 11:21
 */
@Repository
public class AlongWorkDaoImpl implements AlongWorkDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 分页查看随工
     * @param roomId
     * @param pageSize
     * @param page
     * @param status
     * @param taskTime
     * @param keyword
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/6/28 9:35
     */
    @Override
    public PageBean pageList(long roomId, int pageSize, int page, String status, String taskTime, String keyword) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from along_work a, personnel_management b ");
        stringBuffer.append(" where a.room_id = ? ");
        stringBuffer.append("   and a.task_user_id = b.personnel_id ");
        if (!StringUtils.isEmpty(status)){
            stringBuffer.append(" and a.status = ? ");
        }
        if (!StringUtils.isEmpty(taskTime)){
            stringBuffer.append(" and a.task_time like ? ");
        }

        if (!StringUtils.isEmpty(keyword)){
            stringBuffer.append(" and ( a.task_name like ? or b.personnel_name like ? )");
        }

        String countSqlStr = stringBuffer.toString();

        stringBuffer.setLength(0);
        stringBuffer.append("select *, b.personnel_name task_user ");
        stringBuffer.append("  from along_work a, personnel_management b ");
        stringBuffer.append(" where a.room_id = ? ");
        stringBuffer.append("   and a.task_user_id = b.personnel_id ");
        if (!StringUtils.isEmpty(status)){
            stringBuffer.append(" and a.status = ? ");
        }

        if (!StringUtils.isEmpty(taskTime)){
            stringBuffer.append(" and a.task_time like ? ");
        }

        if (!StringUtils.isEmpty(keyword)){
            stringBuffer.append(" and ( a.task_name like ? or b.personnel_name like ? )");
        }

        stringBuffer.append(" order by field(replace(status, 'end', 'complete') ,'running','wait','no','end','complete'), task_time desc ");

        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);

        if (!StringUtils.isEmpty(status)){
            db.set(index++, status);
        }

        if (!StringUtils.isEmpty(taskTime)){
            db.set(index++, taskTime + '%');
        }

        if (!StringUtils.isEmpty(keyword)){
            db.set(index++, '%' + keyword + '%');
            db.set(index++, '%' + keyword + '%');
        }

        PageBean objectPageBean = db.dbQueryPage(AlongWork.class, countSqlStr, page, pageSize);
        return objectPageBean;
    }

    /**
     * 添加随工
     * @param alongWork
     * @return void
     * @author kliu
     * @date 2022/6/28 9:34
     */
    @Override
    public long addAndReturnId(AlongWork alongWork) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into along_work ");
        stringBuffer.append("  (id, room_id, task_name, task_type, task_time, task_describe, ");
        stringBuffer.append("   task_user_id, points, status, create_time, create_user_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?, ?, ?, ?) ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWork.getRoomId());
        db.set(index++, alongWork.getTaskName());
        db.set(index++, alongWork.getTaskType());
        db.set(index++, alongWork.getTaskTime());
        db.set(index++, alongWork.getTaskDescribe());

        db.set(index++, alongWork.getTaskUserId());
        db.set(index++, alongWork.getPoints());
        db.set(index++, alongWork.getStatus());
        db.set(index++, alongWork.getCreateTime());
        db.set(index++, alongWork.getCreateUserId());
        return db.dbUpdateAndReturnId();
    }

    /**
     * 更新随工
     * @param alongWork
     * @return void
     * @author kliu
     * @date 2022/6/28 9:34
     */
    @Override
    public void update(AlongWork alongWork) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update along_work ");
        stringBuffer.append("   set task_name = ? ,task_type = ? ,task_time = ? ,task_describe = ? ,task_user_id = ? ,points = ? ,start_time = ? ,end_time = ? ,status = ? ,reason = ? ");
        stringBuffer.append(" where id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWork.getTaskName());
        db.set(index++, alongWork.getTaskType());
        db.set(index++, alongWork.getTaskTime());
        db.set(index++, alongWork.getTaskDescribe());
        db.set(index++, alongWork.getTaskUserId());
        db.set(index++, alongWork.getPoints());
        db.set(index++, alongWork.getStartTime());
        db.set(index++, alongWork.getEndTime());
        db.set(index++, alongWork.getStatus());
        db.set(index++, alongWork.getReason());
        db.set(index++, alongWork.getId());
        db.dbUpdate();
    }

    /**
     * 删除随工
     * @param id
     * @return void
     * @author kliu
     * @date 2022/6/28 9:34
     */
    @Override
    public void delete(long id) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("delete from along_work where id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, id);
        db.dbUpdate();
    }

    /**
     * 获取随工明细
     * @param id
     * @return com.inspur.industrialinspection.entity.AlongWork
     * @author kliu
     * @date 2022/6/28 9:34
     */
    @Override
    public AlongWork getDtlById(long id) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from along_work where id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, id);
        List<AlongWork> list = db.dbQuery(AlongWork.class);
        if (list.size() == 0) {
            throw new RuntimeException("传入的id不存在，请检查传入的数据");
        }
        return list.get(0);
    }
    /**
     * 获取随工明细-锁表
     * @param id
     * @return com.inspur.industrialinspection.entity.AlongWork
     * @author kliu
     * @date 2022/6/28 9:34
     */
    @Override
    public AlongWork getDtlByIdForUpdate(long id) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from along_work where id = ? for update");
        db.setSql(stringBuffer.toString());
        db.set(1, id);
        List<AlongWork> list = db.dbQuery(AlongWork.class);
        if (list.size() == 0) {
            throw new RuntimeException("传入的id不存在，请检查传入的数据");
        }
        return list.get(0);
    }

    @Override
    public List<AlongWork> getList(Map<String, Object> params) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from along_work where 1=1 ");
        for (String key : params.keySet()) {
            stringBuffer.append(" and " + key + " = " + params.get(key));
        }
        db.setSql(stringBuffer.toString());
        List<AlongWork> list = db.dbQuery(AlongWork.class);
        return list;
    }

    @Override
    public List<AlongWork> getListByCron() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from along_work where status in ('wait', 'no')");
        db.setSql(stringBuffer.toString());
        List<AlongWork> list = db.dbQuery(AlongWork.class);
        return list;
    }

    /**
     * 获取正在运行的任务
     *
     * @return java.util.List<com.inspur.industrialinspection.entity.AlongWork>
     * @author kliu
     * @date 2022/6/29 13:32
     */
    @Override
    public List<AlongWork> getRunningAlongWork(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from along_work ");
        stringBuffer.append(" where status = 'running' ");
        if (roomId>0){
            stringBuffer.append("   and room_id = ?");
        }
        int index = 1;
        db.setSql(stringBuffer.toString());
        if (roomId>0){
            db.set(index++, roomId);
        }
        return db.dbQuery(AlongWork.class);
    }

    /**
     * 更新视频url
     * @param id
     * @param videoUrl
     * @return void
     * @author kliu
     * @date 2022/7/4 20:19
     */
    @Override
    public void updateVideoUrl(long id, String videoUrl) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update along_work ");
        stringBuffer.append("   set video_url = ? ");
        stringBuffer.append(" where id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, videoUrl);
        db.set(index++, id);
        db.dbUpdate();
    }

    @Override
    public List<AlongWork> getPictureList(long roomId, String startTime, String endTime, String taskName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from along_work ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and task_time between ? and ?");
        if (!StringUtils.isEmpty(taskName)){
            stringBuffer.append("   and task_name like ? ");
        }
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startTime);
        db.set(index++, DateUtil.offsetDay(DateUtil.parse(endTime, "yyyy-MM-dd"),1).toString("yyyy-MM-dd"));
        if (!StringUtils.isEmpty(taskName)){
            db.set(index++, "%"+taskName+"%");
        }
        return db.dbQuery(AlongWork.class);
    }

    @Override
    public int countByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from along_work ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and start_time > ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startDate);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public List<AlongWork> listByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from along_work a ");
        stringBuffer.append(" where a.room_id = ? ");
        stringBuffer.append("   and a.start_time > ?");
        stringBuffer.append("  order by a.start_time asc");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, dateStr);
        return db.dbQuery(AlongWork.class);
    }

    @Override
    public int workIssueCountByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from along_work ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and task_time > ?");
        stringBuffer.append("   and status <> 'no' ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startDate);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public List<AlongWork> listByRoomIdAndDateWithOutStartTime(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from along_work ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and task_time > ?");
        stringBuffer.append("   and status <> 'no' ");
        stringBuffer.append("   and start_time is null ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        return db.dbQuery(AlongWork.class);
    }

    @Override
    public List<AlongWork> listByRoomIdAndDateWithVisitorOverTime(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from along_work a ");
        stringBuffer.append(" where start_time > ? ");
        stringBuffer.append("   and a.room_id = ? ");
        stringBuffer.append("   and exists ");
        stringBuffer.append(" (select 1 ");
        stringBuffer.append("          from personnel_management b ");
        stringBuffer.append("         where a.task_user_id = b.personnel_id ");
        stringBuffer.append("           and b.personnel_type = 'visitor' ");
        stringBuffer.append("           and a.start_time > b.personnel_expiration_date || ' 00:00:00')");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, dateStr);
        db.set(index++, roomId);
        return db.dbQuery(AlongWork.class);
    }
}
