package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.WarnInfoDao;
import com.inspur.industrialinspection.entity.WarnInfo;
import com.inspur.industrialinspection.entity.vo.WarnInfoCountVo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 告警信息dao实现
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class WarnInfoDaoImpl implements WarnInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 添加告警信息
     * @param warnInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:15
     */
    @Override
    public void add(WarnInfo warnInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into warn_info ");
        stringBuffer.append("  (task_log_id, point_name, detection_id, level, warn_time) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, warnInfo.getTaskLogId());
        db.set(index++, warnInfo.getPointName());
        db.set(index++, warnInfo.getDetectionId());
        db.set(index++, warnInfo.getLevel());
        db.set(index++, warnInfo.getWarnTime());
        db.dbUpdate();
    }

    /**
     * 更新告警信息
     * @param warnInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:15
     */
    @Override
    public void update(WarnInfo warnInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update warn_info ");
        stringBuffer.append("   set level = ?, warn_time = ? ");
        stringBuffer.append(" where task_log_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        stringBuffer.append("   and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, warnInfo.getLevel());
        db.set(index++, warnInfo.getWarnTime());
        db.set(index++, warnInfo.getTaskLogId());
        db.set(index++, warnInfo.getPointName());
        db.set(index++, warnInfo.getDetectionId());
        db.dbUpdate();
    }

    /**
     * 校验告警信息是否存在
     * @param warnInfo
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:15
     */
    @Override
    public boolean checkExist(WarnInfo warnInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from warn_info ");
        stringBuffer.append(" where task_log_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        stringBuffer.append("   and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, warnInfo.getTaskLogId());
        db.set(index++, warnInfo.getPointName());
        db.set(index++, warnInfo.getDetectionId());
        if(db.dbQuery().size()>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取实例下的告警信息
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.WarnInfo>
     * @author kliu
     * @date 2022/5/24 20:16
     */
    @Override
    public List<WarnInfo> listByInstanceId(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from warn_info a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_detection_result ");
        stringBuffer.append("         where task_log_id = a.task_log_id ");
        stringBuffer.append("           and instance_id = ?) ");
        stringBuffer.append(" order by warn_time desc");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        return db.dbQuery(WarnInfo.class);
    }

    /**
     * 获取某个日期之后的所有告警信息
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.WarnInfo>
     * @author kliu
     * @date 2022/5/24 20:16
     */
    @Override
    public List<WarnInfo> listByDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from warn_info a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from task_detection_result b ");
        stringBuffer.append("         where b.task_log_id = a.task_log_id ");
        stringBuffer.append("           and exists ");
        stringBuffer.append("         (select 1 ");
        stringBuffer.append("                  from task_instance c ");
        stringBuffer.append("                 where c.instance_id = b.instance_id ");
        stringBuffer.append("                   and c.start_time > ? ");
        stringBuffer.append("                   and EXISTS (select 1 ");
        stringBuffer.append("                          from task_info ");
        stringBuffer.append("                         where room_id = ? ");
        stringBuffer.append("                           and task_id = c.task_id)))");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, dateStr);
        db.set(index++, roomId);
        return db.dbQuery(WarnInfo.class);
    }

    @Override
    public List<WarnInfoCountVo> getCountWarnInfo(Long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select warn_info.detection_id as name,count(*) as 'value' from warn_info  INNER JOIN task_detection_result ON task_detection_result.task_log_id = warn_info.task_log_id WHERE  task_detection_result.instance_id = ? GROUP BY warn_info.detection_id ORDER BY value ASC");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        List<WarnInfoCountVo> list = db.dbQuery(WarnInfoCountVo.class);
        if(list.size() == 0){
            return null;
        }
        return list;
    }

    @Override
    public List abnormalCabinetcountByRoomIdAndDate(long roomId, String startDate) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select detection_id, count(1) count ");
        stringBuffer.append("  from warn_info d ");
        stringBuffer.append(" where EXISTS ");
        stringBuffer.append(" (select 1 ");
        stringBuffer.append("          from task_detection_result a ");
        stringBuffer.append("         where a.task_log_id = d.task_log_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from task_instance b ");
        stringBuffer.append("                 where instance_id = a.instance_id ");
        stringBuffer.append("                   and exists (select 1 ");
        stringBuffer.append("                          from task_info ");
        stringBuffer.append("                         where room_id = ? ");
        stringBuffer.append("                           and task_id = b.task_id) ");
        stringBuffer.append("                   and b.start_time > ?)) ");
        stringBuffer.append(" group by detection_id");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, startDate);
        List<Map> list = db.dbQuery();
        return list;
    }
}
