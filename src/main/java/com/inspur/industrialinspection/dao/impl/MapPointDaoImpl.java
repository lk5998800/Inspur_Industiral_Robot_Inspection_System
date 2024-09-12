package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.MapPointDao;
import com.inspur.industrialinspection.entity.MapPoint;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 地图点位
 * @author kliu
 * @date 2022/6/1 15:51
 */
@Repository
public class MapPointDaoImpl implements MapPointDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public List<MapPoint> list(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from map_point where room_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        return db.dbQuery(MapPoint.class);
    }

    @Override
    public void add(MapPoint mapPoint) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into map_point ");
        stringBuffer.append("  (room_id, point_name, point_type, point_x, point_y) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, mapPoint.getRoomId());
        db.set(index++, mapPoint.getPointName());
        db.set(index++, mapPoint.getPointType());
        db.set(index++, mapPoint.getPointX());
        db.set(index++, mapPoint.getPointY());
        db.dbUpdate();
    }

    @Override
    public void update(MapPoint mapPoint) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update map_point ");
        stringBuffer.append("   set point_type = ?, point_x = ?, point_y = ? ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, mapPoint.getPointType());
        db.set(index++, mapPoint.getPointX());
        db.set(index++, mapPoint.getPointY());
        db.set(index++, mapPoint.getRoomId());
        db.set(index++, mapPoint.getPointName());
        db.dbUpdate();
    }

    @Override
    public void delete(MapPoint mapPoint) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from map_point ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, mapPoint.getRoomId());
        db.set(index++, mapPoint.getPointName());
        db.dbUpdate();
    }

    @Override
    public boolean checkIsExist(MapPoint mapPoint) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 from map_point where room_id = ? and point_name = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, mapPoint.getRoomId());
        db.set(2, mapPoint.getPointName());
        return db.dbQuery().size()>0;
    }
}
