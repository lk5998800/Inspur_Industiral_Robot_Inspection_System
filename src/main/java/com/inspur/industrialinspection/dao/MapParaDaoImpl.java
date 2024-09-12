package com.inspur.industrialinspection.dao;

import com.inspur.db.Db;
import com.inspur.industrialinspection.entity.MapPara;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 地图参数
 * @author kliu
 * @date 2022/11/21 9:27
 */
@Repository
public class MapParaDaoImpl implements MapParaDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public MapPara getByRoomId(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select room_id, ");
        stringBuffer.append("       url, ");
        stringBuffer.append("       height, ");
        stringBuffer.append("       width, ");
        stringBuffer.append("       origin_x, ");
        stringBuffer.append("       origin_y, ");
        stringBuffer.append("       resolution, ");
        stringBuffer.append("       offset_x, ");
        stringBuffer.append("       offset_y ");
        stringBuffer.append("  from map_para ");
        stringBuffer.append(" where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        List<MapPara> list = db.dbQuery(MapPara.class);
        if (list.size() == 0){
            return null;
        }
        return list.get(0);
    }

    @Override
    public void add(MapPara mapPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into industrial_robot.map_para ");
        stringBuffer.append("  (room_id, url, height, width, origin_x, ");
        stringBuffer.append("   origin_y, resolution, offset_x, offset_y) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, mapPara.getRoomId());
        db.set(index++, mapPara.getUrl());
        db.set(index++, mapPara.getHeight());
        db.set(index++, mapPara.getWidth());
        db.set(index++, mapPara.getOriginX());
        db.set(index++, mapPara.getOriginY());
        db.set(index++, mapPara.getResolution());
        db.set(index++, mapPara.getOffsetX());
        db.set(index++, mapPara.getOffsetY());
        db.dbUpdate();
    }

    @Override
    public void update(MapPara mapPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update industrial_robot.map_para ");
        stringBuffer.append("   set url        = ?, ");
        stringBuffer.append("       height     = ?, ");
        stringBuffer.append("       width      = ?, ");
        stringBuffer.append("       origin_x   = ?, ");
        stringBuffer.append("       origin_y   = ?, ");
        stringBuffer.append("       resolution = ?, ");
        stringBuffer.append("       offset_x   = ?, ");
        stringBuffer.append("       offset_y   = ? ");
        stringBuffer.append(" where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, mapPara.getUrl());
        db.set(index++, mapPara.getHeight());
        db.set(index++, mapPara.getWidth());
        db.set(index++, mapPara.getOriginX());
        db.set(index++, mapPara.getOriginY());
        db.set(index++, mapPara.getResolution());
        db.set(index++, mapPara.getOffsetX());
        db.set(index++, mapPara.getOffsetY());
        db.set(index++, mapPara.getRoomId());
        db.dbUpdate();
    }

    @Override
    public void delete(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from industrial_robot.map_para where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.dbUpdate();
    }
}
