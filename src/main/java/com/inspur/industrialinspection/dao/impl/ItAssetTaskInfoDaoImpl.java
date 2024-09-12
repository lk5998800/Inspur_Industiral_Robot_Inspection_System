package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ItAssetTaskInfoDao;
import com.inspur.industrialinspection.entity.ItAsset;
import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: kliu
 * @description: 资产管理服务
 * @date: 2022/7/25 17:18
 */
@Repository
public class ItAssetTaskInfoDaoImpl implements ItAssetTaskInfoDao {

    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private RequestService requestService;
    /**
     * 获取列表
     *
     * @param itAssetTaskInfo
     * @return java.util.List
     * @author kliu
     * @date 2022/7/25 13:48
     */
    @Override
    public PageBean list(ItAssetTaskInfo itAssetTaskInfo, int pageNum, int pageSize) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from it_asset_task_info a, room_info b, robot_info c, user_info d ");
        stringBuffer.append(" where a.room_id = b.room_id ");
        stringBuffer.append("   and a.robot_id = c.robot_id ");
        stringBuffer.append("   and a.create_user_id = d.user_id ");
        if (!StringUtils.isEmpty(itAssetTaskInfo.getTaskName())){
            stringBuffer.append("   and a.task_name like ? ");
        }
        if (itAssetTaskInfo.getRoomId()>0) {
            stringBuffer.append("   and a.room_id = ? ");
        }
        if (itAssetTaskInfo.getRobotId()>0) {
            stringBuffer.append("   and a.robot_id = ? ");
        }
        if (!StringUtils.isEmpty(itAssetTaskInfo.getExecType())){
            stringBuffer.append("   and a.exec_type = ? ");
        }
        if (!StringUtils.isEmpty(itAssetTaskInfo.getCreateTime())){
            stringBuffer.append("   and a.create_time like ?");
        }
        String countSqlStr = stringBuffer.toString();

        stringBuffer.setLength(0);
        stringBuffer.append("select a.id, a.task_name, a.room_id, a.robot_id, b.room_name, ");
        stringBuffer.append("       c.robot_name, a.exec_type, a.exec_time, a.inventory_method, a.task_desc, ");
        stringBuffer.append("       a.create_time, a.cycle_type, a.cycle_value, d.user_name create_user ");
        stringBuffer.append("  from it_asset_task_info a, room_info b, robot_info c, user_info d ");
        stringBuffer.append(" where a.room_id = b.room_id ");
        stringBuffer.append("   and a.robot_id = c.robot_id ");
        stringBuffer.append("   and a.create_user_id = d.user_id ");
        if (!StringUtils.isEmpty(itAssetTaskInfo.getTaskName())){
            stringBuffer.append("   and a.task_name like ? ");
        }
        if (itAssetTaskInfo.getRoomId()>0) {
            stringBuffer.append("   and a.room_id = ? ");
        }
        if (itAssetTaskInfo.getRobotId()>0) {
            stringBuffer.append("   and a.robot_id = ? ");
        }
        if (!StringUtils.isEmpty(itAssetTaskInfo.getExecType())){
            stringBuffer.append("   and a.exec_type = ? ");
        }
        if (!StringUtils.isEmpty(itAssetTaskInfo.getCreateTime())){
            stringBuffer.append("   and a.create_time like ?");
        }

        stringBuffer.append(" order by create_time desc ");

        int index = 1;
        db.setSql(stringBuffer.toString());

        if (!StringUtils.isEmpty(itAssetTaskInfo.getTaskName())){
            db.set(index++, "%"+itAssetTaskInfo.getTaskName()+"%");
        }
        if (itAssetTaskInfo.getRoomId()>0) {
            db.set(index++, itAssetTaskInfo.getRoomId());
        }
        if (itAssetTaskInfo.getRobotId()>0) {
            db.set(index++, itAssetTaskInfo.getRobotId());
        }
        if (!StringUtils.isEmpty(itAssetTaskInfo.getExecType())){
            db.set(index++, itAssetTaskInfo.getExecType());
        }
        if (!StringUtils.isEmpty(itAssetTaskInfo.getCreateTime())){
            db.set(index++, "%"+itAssetTaskInfo.getCreateTime()+"%");
        }

        PageBean objectPageBean = db.dbQueryPage(ItAssetTaskInfo.class, countSqlStr, pageNum, pageSize);
        return objectPageBean;
    }

    /**
     * 添加
     *
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    public void add(ItAssetTaskInfo itAssetTaskInfo) {
        long userId = requestService.getUserIdByToken();
        String createTime = DateUtil.now();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_info ");
        stringBuffer.append("  (task_name, room_id, robot_id, exec_type, exec_time, ");
        stringBuffer.append("   inventory_method, task_desc, create_time, cycle_type, cycle_value, ");
        stringBuffer.append("   create_user_id, next_exec_time) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAssetTaskInfo.getTaskName());
        db.set(index++, itAssetTaskInfo.getRoomId());
        db.set(index++, itAssetTaskInfo.getRobotId());
        db.set(index++, itAssetTaskInfo.getExecType());
        db.set(index++, itAssetTaskInfo.getExecTime());

        db.set(index++, itAssetTaskInfo.getInventoryMethod());
        db.set(index++, itAssetTaskInfo.getTaskDesc());
        db.set(index++, createTime);
        db.set(index++, itAssetTaskInfo.getCycleType());
        db.set(index++, itAssetTaskInfo.getCycleValue());

        db.set(index++, userId);
        db.set(index++, itAssetTaskInfo.getNextExecTime());
        db.dbUpdate();
    }

    /**
     * 更新
     *
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    public void update(ItAssetTaskInfo itAssetTaskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update it_asset_task_info ");
        stringBuffer.append("   set task_name = ?, room_id = ?, robot_id = ?, exec_type = ?, exec_time = ?, ");
        stringBuffer.append("       inventory_method = ?, task_desc = ?, cycle_type = ?, cycle_value = ?, next_exec_time = ? ");
        stringBuffer.append(" where id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAssetTaskInfo.getTaskName());
        db.set(index++, itAssetTaskInfo.getRoomId());
        db.set(index++, itAssetTaskInfo.getRobotId());
        db.set(index++, itAssetTaskInfo.getExecType());
        db.set(index++, itAssetTaskInfo.getExecTime());

        db.set(index++, itAssetTaskInfo.getInventoryMethod());
        db.set(index++, itAssetTaskInfo.getTaskDesc());
        db.set(index++, itAssetTaskInfo.getCycleType());
        db.set(index++, itAssetTaskInfo.getCycleValue());
        db.set(index++, itAssetTaskInfo.getNextExecTime());

        db.set(index++, itAssetTaskInfo.getId());
        db.dbUpdate();
    }

    /**
     * 删除
     *
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    public void delete(ItAssetTaskInfo itAssetTaskInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from it_asset_task_info where id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAssetTaskInfo.getId());
        db.dbUpdate();
    }

    /**
     * 获取资产盘点任务明细
     * @param id
     * @return com.inspur.industrialinspection.entity.ItAssetTaskInfo
     * @author kliu
     * @date 2022/7/25 17:57
     */
    @Override
    public ItAssetTaskInfo getDetlById(long id) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select a.id, a.task_name, a.room_id, a.robot_id, b.room_name, ");
        stringBuffer.append("       c.robot_name, a.exec_type, a.exec_time, a.inventory_method, a.task_desc, ");
        stringBuffer.append("       a.create_time, a.cycle_type, a.cycle_value, d.user_name create_user ");
        stringBuffer.append("  from it_asset_task_info a, room_info b, robot_info c, user_info d ");
        stringBuffer.append(" where a.room_id = b.room_id ");
        stringBuffer.append("   and a.robot_id = c.robot_id ");
        stringBuffer.append("   and a.create_user_id = d.user_id ");
        stringBuffer.append("   and a.id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, id);
        List<ItAssetTaskInfo> list = db.dbQuery(ItAssetTaskInfo.class);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List pendingExecutionTask() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from it_asset_task_info ");
        stringBuffer.append(" where (exec_type = 'regular' and next_exec_time is not null) ");
        stringBuffer.append("    or exec_type = 'cycle'");
        int index = 1;
        db.setSql(stringBuffer.toString());
        return db.dbQuery(ItAssetTaskInfo.class);
    }

    @Override
    public void batchDelete(String inPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from it_asset_task_info where id in ("+inPara+") ");
        db.setSql(stringBuffer.toString());
        db.dbUpdate();
    }
}
