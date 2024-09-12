package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.BuildingInfoDao;
import com.inspur.industrialinspection.entity.BuildingInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 楼栋信息dao实现
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class BuildingInfoDaoImpl implements BuildingInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public List<BuildingInfo> list(int parkId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select building_id, building_name, park_id ");
        stringBuffer.append("  from building_info ");
        stringBuffer.append(" where park_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, parkId);
        return db.dbQuery(BuildingInfo.class);
    }

    @Override
    public void add(BuildingInfo buildingInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into building_info ");
        stringBuffer.append("  (building_name, park_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, buildingInfo.getBuildingName());
        db.set(index++, buildingInfo.getParkId());
        db.dbUpdate();
    }

    @Override
    public void update(BuildingInfo buildingInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update building_info ");
        stringBuffer.append("   set building_name = ? ");
        stringBuffer.append(" where building_id = ? ");
        stringBuffer.append("   and park_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, buildingInfo.getBuildingName());
        db.set(index++, buildingInfo.getBuildingId());
        db.set(index++, buildingInfo.getParkId());
        db.dbUpdate();
    }

    @Override
    public void delete(long buildingId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from industrial_robot.building_info where building_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, buildingId);
        db.dbUpdate();
    }

    @Override
    public BuildingInfo getDetlById(long buildingId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from building_info ");
        stringBuffer.append(" where building_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, buildingId);
        List list = db.dbQuery(BuildingInfo.class);
        if (list.size() == 0){
            return null;
        }else{
            return (BuildingInfo) list.get(0);
        }
    }
}
