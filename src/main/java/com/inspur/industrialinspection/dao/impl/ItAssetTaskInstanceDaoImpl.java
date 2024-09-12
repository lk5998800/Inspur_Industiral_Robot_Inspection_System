package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ItAssetTaskInstanceDao;
import com.inspur.industrialinspection.entity.AlongWork;
import com.inspur.industrialinspection.entity.ItAsset;
import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author: kliu
 * @description: 资产盘点任务执行实例
 * @date: 2022/7/27 17:03
 */
@Repository
public class ItAssetTaskInstanceDaoImpl implements ItAssetTaskInstanceDao {

    private static String WARN_DATA_TYPE_VALUE = "warn";

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private RequestService requestService;
    /**
     * 添加任务实例并返回id
     *
     * @param itAssetTaskInstance
     * @return long
     * @author kliu
     * @date 2022/7/27 17:05
     */
    @Override
    public long addAndReturnId(ItAssetTaskInstance itAssetTaskInstance) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_instance ");
        stringBuffer.append("  (instance_id, task_id, start_time, exec_status) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?)");
        db.setSql(stringBuffer.toString());
        db.set(1, itAssetTaskInstance.getTaskId());
        db.set(2, itAssetTaskInstance.getStartTime());
        db.set(3, itAssetTaskInstance.getExecStatus());
        return db.dbUpdateAndReturnId();
    }

    /**
     * 更新任务执行实例
     *
     * @param itAssetTaskInstance
     * @return void
     * @author kliu
     * @date 2022/7/27 17:05
     */
    @Override
    public void update(ItAssetTaskInstance itAssetTaskInstance) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update it_asset_task_instance ");
        stringBuffer.append("   set end_time = ?, exec_status = ?, task_result = ? ");
        stringBuffer.append(" where instance_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, itAssetTaskInstance.getEndTime());
        db.set(2, itAssetTaskInstance.getExecStatus());
        db.set(3, itAssetTaskInstance.getTaskResult());
        db.set(4, itAssetTaskInstance.getInstanceId());
        db.dbUpdate();
    }

    /**
     * 获取任务明细
     *
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.ItAssetTaskInstance
     * @author kliu
     * @date 2022/7/27 17:05
     */
    @Override
    public ItAssetTaskInstance getDetlById(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from it_asset_task_instance where instance_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        List<ItAssetTaskInstance> list = db.dbQuery(ItAssetTaskInstance.class);
        if (list.size() == 0) {
            throw new RuntimeException("实例id【"+instanceId+"】未获取到数据，请检查");
        }
        return list.get(0);
    }

    /**
     * 获取实例明细锁表
     *
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/7/27 17:05
     */
    @Override
    public ItAssetTaskInstance getDetlByIdForUpdate(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from it_asset_task_instance where instance_id = ? for update ");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        List<ItAssetTaskInstance> list = db.dbQuery(ItAssetTaskInstance.class);
        if (list.size() == 0) {
            throw new RuntimeException("实例id【"+instanceId+"】未获取到数据，请检查");
        }
        return list.get(0);
    }

    /**
     * 分页查询任务实例
     * @param roomId
     * @param robotId
     * @param taskName
     * @param cabinetRow
     * @param cabinetColumn
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/9/14 11:25
     */
    @Override
    public PageBean list(long roomId, long robotId, String taskName, String cabinetRow, long cabinetColumn, int pageSize, int pageNum) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from it_asset_task_instance a, it_asset_task_info b ");
        stringBuffer.append(" where a.task_id = b.id ");
        stringBuffer.append("   and a.exec_status <> 'create'");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if(roomId>0){
            stringBuffer.append("   and b.room_id = ?");
        }
        if(robotId>0){
            stringBuffer.append("   and b.robot_id = ?");
        }
        if(!StringUtils.isEmpty(taskName)){
            stringBuffer.append("   and b.task_name like ?");
        }
        if(!StringUtils.isEmpty(cabinetRow)){
            stringBuffer.append("   and exists (select 1 from it_asset where room_id = b.room_id and cabinet_row = ?)");
        }
        if(cabinetColumn>0){
            stringBuffer.append("   and exists (select 1 from it_asset where room_id = b.room_id and cabinet_column = ?)");
        }
        String countSqlStr = stringBuffer.toString();

        stringBuffer.setLength(0);
        stringBuffer.append("select a.instance_id, ");
        stringBuffer.append("       a.start_time, ");
        stringBuffer.append("       a.end_time, ");
        stringBuffer.append("       a.exec_status, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       a.task_id, ");
        stringBuffer.append("       (select robot_name from robot_info where robot_id = b.robot_id) robot_name, ");
        stringBuffer.append("       (select room_name from room_info where room_id = b.room_id) room_name, ");
        stringBuffer.append("       b.task_name, ");
        stringBuffer.append("       (select user_name from user_info where user_id = b.create_user_id) create_user_name ");
        stringBuffer.append("  from it_asset_task_instance a, it_asset_task_info b ");
        stringBuffer.append(" where a.task_id = b.id ");
        stringBuffer.append("   and a.exec_status <> 'create'");
        stringBuffer.append("   and exists (select 1 from room_info where room_id = b.room_id and park_id = ? )");
        if(roomId>0){
            stringBuffer.append("   and b.room_id = ?");
        }
        if(robotId>0){
            stringBuffer.append("   and b.robot_id = ?");
        }
        if(!StringUtils.isEmpty(taskName)){
            stringBuffer.append("   and b.task_name like ?");
        }
        if(!StringUtils.isEmpty(cabinetRow)){
            stringBuffer.append("   and exists (select 1 from it_asset where room_id = b.room_id and cabinet_row = ?)");
        }
        if(cabinetColumn>0){
            stringBuffer.append("   and exists (select 1 from it_asset where room_id = b.room_id and cabinet_column = ?)");
        }
        stringBuffer.append(" order by if (exec_status ='running',0,1), start_time desc");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, parkId);
        if(roomId>0){
            db.set(index++, roomId);
        }
        if(robotId>0){
            db.set(index++, robotId);
        }
        if(!StringUtils.isEmpty(taskName)){
            db.set(index++, "%"+taskName+"%");
        }
        if(!StringUtils.isEmpty(cabinetRow)){
            db.set(index++, cabinetRow);
        }
        if(cabinetColumn>0){
            db.set(index++, cabinetColumn);
        }
        PageBean objectPageBean = db.dbQueryPage(ItAssetTaskInstance.class, countSqlStr, pageNum, pageSize);
        return objectPageBean;
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
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from it_asset_task_instance where instance_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, instanceId);
        return db.dbQuery().size()>0;
    }

    @Override
    public List<ItAssetTaskInstance> unAnalyseList() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from it_asset_task_instance ");
        stringBuffer.append(" where exec_status = 'end' ");
        stringBuffer.append("   and task_result is null");
        db.setSql(stringBuffer.toString());
        return db.dbQuery(ItAssetTaskInstance.class);
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public PageBean instanceDetlList(String cabinetRow, long cabinetColumn, String assetNo, String assetName, long personInChargeId, long instanceId, String dataType, int pageSize, int pageNum) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.setLength(0);
        stringBuffer.append(" select count(1) count from ( ");
        stringBuffer.append("select b.id, ");
        stringBuffer.append("       b.asset_no, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       b.person_in_charge_id, ");
        stringBuffer.append("       b.cabinet_column, ");
        stringBuffer.append("       b.asset_name, ");
        stringBuffer.append("       b.brand, ");
        stringBuffer.append("       b.model, ");
        stringBuffer.append("       b.asset_desc, ");
        stringBuffer.append("       b.u_bit, ");
        stringBuffer.append("       b.cabinet_row, ");
        stringBuffer.append("       b.create_time, ");
        stringBuffer.append("       b.ewm_rfid, ");
        stringBuffer.append("       b.point_name, d.room_name, a.result,");
        stringBuffer.append("       (select personnel_name from personnel_management where personnel_id = b.person_in_charge_id ) personnel_name ");
        stringBuffer.append("  from it_asset_task_analyse_result a, it_asset b, room_info d ");
        stringBuffer.append(" where a.it_asset_id = b.id ");
        stringBuffer.append("   and b.room_id = d.room_id ");
        stringBuffer.append("   and a.instance_id = ? ");
        if (!StringUtils.isEmpty(cabinetRow)){
            stringBuffer.append("  and b.cabinet_row = ? ");
        }
        if (cabinetColumn>0){
            stringBuffer.append("  and b.cabinet_column = ? ");
        }
        if (personInChargeId>0){
            stringBuffer.append("  and b.person_in_charge_id = ? ");
        }
        if (!StringUtils.isEmpty(assetNo)){
            stringBuffer.append("  and b.asset_no like ? ");
        }
        if (!StringUtils.isEmpty(assetName)){
            stringBuffer.append("  and b.asset_name like ? ");
        }
        if (WARN_DATA_TYPE_VALUE.equals(dataType)){
            stringBuffer.append("  and a.result <> 'normal' ");
        }

        if (!StringUtils.isEmpty(cabinetRow) || cabinetColumn>0 || personInChargeId>0 || !StringUtils.isEmpty(assetName)|| !StringUtils.isEmpty(assetNo)){
            stringBuffer.append("  ) z");
        }else{
            stringBuffer.append("union all ");
            stringBuffer.append("select 0 id, ");
            stringBuffer.append("       '' asset_no, ");
            stringBuffer.append("       0 room_id, ");
            stringBuffer.append("       0 person_in_charge_id, ");
            stringBuffer.append("       0 cabinet_column, ");
            stringBuffer.append("       '' asset_name, ");
            stringBuffer.append("       '' brand, ");
            stringBuffer.append("       '' model, ");
            stringBuffer.append("       '' asset_desc, ");
            stringBuffer.append("       '' u_bit, ");
            stringBuffer.append("       '' cabinet_row, ");
            stringBuffer.append("       '' create_time, ");
            stringBuffer.append("       e.qr_code ewm_rfid, ");
            stringBuffer.append("       '' point_name, '' room_name, 'unknown' result, '' personnel_name");
            stringBuffer.append("  from it_asset_task_analyse_result d, it_asset_task_result e ");
            stringBuffer.append(" where d.result = 'unknown' ");
            stringBuffer.append("   and d.task_log_id = e.task_log_id");
            stringBuffer.append("  ) z");
        }

        String countSqlStr = stringBuffer.toString();
        stringBuffer.setLength(0);
        stringBuffer.append(" select * from ( ");
        stringBuffer.append("select b.id, ");
        stringBuffer.append("       b.asset_no, ");
        stringBuffer.append("       b.room_id, ");
        stringBuffer.append("       b.person_in_charge_id, ");
        stringBuffer.append("       b.cabinet_column, ");
        stringBuffer.append("       b.asset_name, ");
        stringBuffer.append("       (select content from code_config where code ='BRAND' and b.brand = value) brand, ");
        stringBuffer.append("       (select content from code_config where code ='MODEL' and b.model = value) model, ");
        stringBuffer.append("       b.asset_desc, ");
        stringBuffer.append("       b.u_bit, ");
        stringBuffer.append("       b.cabinet_row, ");
        stringBuffer.append("       b.create_time, ");
        stringBuffer.append("       b.ewm_rfid, ");
        stringBuffer.append("       b.point_name, d.room_name, a.result, ");
        stringBuffer.append("       (select personnel_name from personnel_management where personnel_id = b.person_in_charge_id ) personnel_name ");
        stringBuffer.append("  from it_asset_task_analyse_result a, it_asset b, room_info d");
        stringBuffer.append(" where a.it_asset_id = b.id ");
        stringBuffer.append("   and b.room_id = d.room_id ");
        stringBuffer.append("   and a.instance_id = ? ");
        if (!StringUtils.isEmpty(cabinetRow)){
            stringBuffer.append("  and b.cabinet_row = ? ");
        }
        if (cabinetColumn>0){
            stringBuffer.append("  and b.cabinet_column = ? ");
        }
        if (personInChargeId>0){
            stringBuffer.append("  and b.person_in_charge_id = ? ");
        }
        if (!StringUtils.isEmpty(assetNo)){
            stringBuffer.append("  and b.asset_no like ? ");
        }
        if (!StringUtils.isEmpty(assetName)){
            stringBuffer.append("  and b.asset_name like ? ");
        }
        if (WARN_DATA_TYPE_VALUE.equals(dataType)){
            stringBuffer.append("  and a.result <> 'normal' ");
        }

        if (!StringUtils.isEmpty(cabinetRow) || cabinetColumn>0 || personInChargeId>0 || !StringUtils.isEmpty(assetName)){
            stringBuffer.append("  ) z");
        }else{
            stringBuffer.append("union all ");
            stringBuffer.append("select 0 id, ");
            stringBuffer.append("       '' asset_no, ");
            stringBuffer.append("       0 room_id, ");
            stringBuffer.append("       0 person_in_charge_id, ");
            stringBuffer.append("       0 cabinet_column, ");
            stringBuffer.append("       '' asset_name, ");
            stringBuffer.append("       '' brand, ");
            stringBuffer.append("       '' model, ");
            stringBuffer.append("       '' asset_desc, ");
            stringBuffer.append("       '' u_bit, ");
            stringBuffer.append("       '' cabinet_row, ");
            stringBuffer.append("       '' create_time, ");
            stringBuffer.append("       e.qr_code ewm_rfid, ");
            stringBuffer.append("       '' point_name, '' room_name, 'unknown' result, '' personnel_name ");
            stringBuffer.append("  from it_asset_task_analyse_result d, it_asset_task_result e ");
            stringBuffer.append(" where d.result = 'unknown' ");
            stringBuffer.append("   and d.task_log_id = e.task_log_id");
            stringBuffer.append("  ) z");
        }

        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        if (!StringUtils.isEmpty(cabinetRow)){
            db.set(index++, cabinetRow);
        }
        if (cabinetColumn>0){
            db.set(index++, cabinetColumn);
        }
        if (personInChargeId>0){
            db.set(index++, personInChargeId);
        }
        if (!StringUtils.isEmpty(assetNo)){
            db.set(index++, "%"+assetNo+"%");
        }
        if (!StringUtils.isEmpty(assetName)){
            db.set(index++, "%"+assetName+"%");
        }
        PageBean objectPageBean = db.dbQueryPage(ItAsset.class, countSqlStr, pageNum, pageSize);
        return objectPageBean;
    }

    @Override
    public void batchDelete(String inPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from it_asset_task_instance where instance_id in ("+inPara+") ");
        db.setSql(stringBuffer.toString());
        db.dbUpdate();
    }

    @Override
    public int countByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from it_asset_task_instance ");
        stringBuffer.append(" where exists (select 1 from it_asset_task_info where room_id = ?) ");
        stringBuffer.append("   and start_time > ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, dateStr);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }

    @Override
    public List<ItAssetTaskInstance> listByRoomIdAndDate(long roomId, String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from it_asset_task_instance a, it_asset_task_info b ");
        stringBuffer.append(" where a.task_id = b.id ");
        stringBuffer.append("   and b.room_id = ? ");
        stringBuffer.append("   and a.start_time > ?");
        stringBuffer.append("  order by a.start_time asc");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, dateStr);
        return db.dbQuery(ItAssetTaskInstance.class);
    }
}
