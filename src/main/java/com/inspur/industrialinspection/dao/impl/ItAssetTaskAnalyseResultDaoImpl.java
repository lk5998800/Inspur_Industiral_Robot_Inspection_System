package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ItAssetTaskAnalyseResultDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author: kliu
 * @description: 资产盘点任务分析结果
 * @date: 2022/8/2 15:51
 */
@Repository
public class ItAssetTaskAnalyseResultDaoImpl implements ItAssetTaskAnalyseResultDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * rfid正常资产 资产基本信息表存在数据，上传结果有数据--资产正常
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/8/2 15:56
     */
    @Override
    public void saveItAssetRfidNormal(long instanceId, long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_analyse_result ");
        stringBuffer.append("  (instance_id, it_asset_id, result, task_log_id) ");
        stringBuffer.append("  select ?, a.id, 'normal' ");
        stringBuffer.append("    from it_asset a, it_asset_task_result b ");
        stringBuffer.append("   where a.room_id = ? ");
        stringBuffer.append("     and b.instance_id = ? ");
        stringBuffer.append("     and b.qr_code = a.asset_no ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, roomId);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    /**
     * rfid资产丢失 资产基本信息表存在数据，上传结果无数据-资产缺失
     *
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/8/2 15:56
     */
    @Override
    public void saveItAssetRfidLack(long instanceId, long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_analyse_result ");
        stringBuffer.append("  (instance_id, it_asset_id, result) ");
        stringBuffer.append("  select ?, a.id, 'lack' ");
        stringBuffer.append("    from it_asset a ");
        stringBuffer.append("   where a.room_id = ? ");
        stringBuffer.append("     and not exists (select 1 ");
        stringBuffer.append("            from it_asset_task_result ");
        stringBuffer.append("           where instance_id = ? ");
        stringBuffer.append("             and qr_code = a.asset_no)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, roomId);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    /**
     * rfid不明资产 上传结果有数据，资产基本信息表无数据 -不明资产
     *
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/8/2 15:56
     */
    @Override
    public void saveItAssetRfidUnknown(long instanceId, long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_analyse_result ");
        stringBuffer.append("  (instance_id, it_asset_id, result, task_log_id) ");
        stringBuffer.append("  select ?, 0, 'unknown', task_log_id ");
        stringBuffer.append("    from it_asset_task_result a ");
        stringBuffer.append("   where a.instance_id = ? ");
        stringBuffer.append("     and not exists (select 1 ");
        stringBuffer.append("            from it_asset ");
        stringBuffer.append("           where room_id = ? ");
        stringBuffer.append("             and a.qr_code = asset_no)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, instanceId);
        db.set(index++, roomId);
        db.dbUpdate();
    }

    /**
     * qrcode正常资产 资产基本信息表存在数据，上传结果有数据，点位相同--资产正常
     *
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/8/2 15:56
     */
    @Override
    public void saveItAssetQrcodeNormal(long instanceId, long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_analyse_result ");
        stringBuffer.append("  (instance_id, it_asset_id, result, task_log_id) ");
        stringBuffer.append("  select ?, a.id, 'normal', b.task_log_id ");
        stringBuffer.append("    from it_asset a, it_asset_task_result b ");
        stringBuffer.append("   where a.room_id = ? ");
        stringBuffer.append("     and b.instance_id = ? ");
        stringBuffer.append("     and b.qr_code = a.asset_no ");
//        暂时去掉点位的限制，拍照有问题，同一个二维码可能出现在多个点位中
//        stringBuffer.append("     and b.point_name like CONCAT(a.point_name,'%') ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, roomId);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    /**
     * qrcode资产丢失 资产基本信息表存在数据，上传结果无数据-资产缺失
     *
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/8/2 15:56
     */
    @Override
    public void saveItAssetQrcodeLack(long instanceId, long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_analyse_result ");
        stringBuffer.append("  (instance_id, it_asset_id, result, task_log_id) ");
        stringBuffer.append("  select ?, a.id, 'lack', null ");
        stringBuffer.append("    from it_asset a ");
        stringBuffer.append("   where a.room_id = ? ");
        stringBuffer.append("     and not exists ");
        stringBuffer.append("   (select 1 ");
        stringBuffer.append("            from it_asset_task_result ");
        stringBuffer.append("           where instance_id = ? ");
        stringBuffer.append("             and qr_code = a.asset_no)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, roomId);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    /**
     * qrcode资产不明 上传结果有数据，资产基本信息表无数据-不明资产
     *
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/8/2 15:56
     */
    @Override
    public void saveItAssetQrcodeUnknown(long instanceId, long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_analyse_result ");
        stringBuffer.append("  (instance_id, it_asset_id, result, task_log_id) ");
        stringBuffer.append("  select ?, 0, 'unknown', task_log_id ");
        stringBuffer.append("    from it_asset_task_result a ");
        stringBuffer.append("   where a.instance_id = ? ");
        stringBuffer.append("     and not exists (select 1 ");
        stringBuffer.append("            from it_asset ");
        stringBuffer.append("           where room_id = ? ");
        stringBuffer.append("             and a.qr_code = asset_no)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, instanceId);
        db.set(index++, roomId);
        db.dbUpdate();
    }

    /**
     * qrcode资产移位 资产基本信息表有数据，上传结果有数据，但是点位不一样-资产移位
     * @param instanceId
     * @return void
     * @author kliu
     * @date 2022/8/2 15:56
     */
    @Override
    public void saveItAssetQrcodeShift(long instanceId, long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_analyse_result ");
        stringBuffer.append("  (instance_id, it_asset_id, result, task_log_id) ");
        stringBuffer.append("  select ?, a.id, 'shift', b.task_log_id ");
        stringBuffer.append("    from it_asset a, it_asset_task_result b ");
        stringBuffer.append("   where a.room_id = ? ");
        stringBuffer.append("     and b.instance_id = ? ");
        stringBuffer.append("     and b.qr_code = a.asset_no ");
        stringBuffer.append("     and b.point_name not like CONCAT(a.point_name,'%') ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        db.set(index++, roomId);
        db.set(index++, instanceId);
        db.dbUpdate();
    }

    @Override
    public boolean instanceNoraml(long instanceId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from it_asset_task_analyse_result ");
        stringBuffer.append(" where instance_id = ? ");
        stringBuffer.append("   and result <> 'normal'");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, instanceId);
        return db.dbQuery().size()==0;
    }
}
