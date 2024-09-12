package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.CabinetUbitDao;
import com.inspur.industrialinspection.entity.CabinetUbit;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 机柜u位
 * @author kliu
 * @date 2022/6/13 11:21
 */
@Repository
public class CabinetUbitDaoimpl implements CabinetUbitDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public List<CabinetUbit> list(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from cabinet_ubit where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        return db.dbQuery(CabinetUbit.class);
    }

    @Override
    public void add(CabinetUbit cabinetUbit) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into cabinet_ubit ");
        stringBuffer.append("  (room_id, ubit, use_ubit, free_ubit, point_name, usage_rate) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, cabinetUbit.getRoomId());
        db.set(index++, cabinetUbit.getUbit());
        db.set(index++, cabinetUbit.getUseUbit());
        db.set(index++, cabinetUbit.getFreeUbit());
        db.set(index++, cabinetUbit.getPointName());
        db.set(index++, cabinetUbit.getUsageRate());
        db.dbUpdate();
    }

    @Override
    public void update(CabinetUbit cabinetUbit) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update cabinet_ubit ");
        stringBuffer.append("   set ubit = ?, use_ubit = ?, free_ubit = ?, usage_rate = ? ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, cabinetUbit.getUbit());
        db.set(index++, cabinetUbit.getUseUbit());
        db.set(index++, cabinetUbit.getFreeUbit());
        db.set(index++, cabinetUbit.getUsageRate());
        db.set(index++, cabinetUbit.getRoomId());
        db.set(index++, cabinetUbit.getPointName());
        db.dbUpdate();
    }

    @Override
    public boolean checkExist(CabinetUbit cabinetUbit) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from cabinet_ubit where room_id = ? and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, cabinetUbit.getRoomId());
        db.set(index++, cabinetUbit.getPointName());
        return db.dbQuery().size()>0;
    }
}
