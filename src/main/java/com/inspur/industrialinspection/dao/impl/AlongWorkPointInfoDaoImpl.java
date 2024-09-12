package com.inspur.industrialinspection.dao.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.AlongWorkPointInfoDao;
import com.inspur.industrialinspection.entity.PointInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 检测点基本信息dao实现
 * @author: kliu
 * @date: 2022/4/16 10:21
 */
@Repository
public class AlongWorkPointInfoDaoImpl implements AlongWorkPointInfoDao {

    @Autowired
    private BeanFactory beanFactory;


    /**
     * 获取列表
     *
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/8/5 14:10
     */
    @Override
    public List list(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from along_work_point_info where room_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        return db.dbQuery(PointInfo.class);
    }

    /**
     * 删除
     *
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 14:11
     */
    @Override
    public void delete(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from along_work_point_info ");
        stringBuffer.append(" where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.dbUpdate();
    }

    /**
     * 添加
     *
     * @param array
     * @return void
     * @author kliu
     * @date 2022/8/5 14:11
     */
    @Override
    public void add(JSONArray array) {
        Db db = beanFactory.getBean(Db.class);
        JSONObject jsonObject;
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into along_work_point_info ");
        stringBuffer.append("  (room_id, point_name, location_x, location_y, location_z, ");
        stringBuffer.append("   orientation_x, orientation_y, orientation_z, orientation_w) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?, ?, ?) ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        for (int i = 0; i < array.size(); i++) {
            jsonObject = array.getJSONObject(i);
            index = 1;
            db.set(index++, jsonObject.getLong("roomId"));
            db.set(index++, jsonObject.getStr("pointName"));
            db.set(index++, jsonObject.getBigDecimal("locationX"));
            db.set(index++, jsonObject.getBigDecimal("locationY"));
            db.set(index++, jsonObject.getBigDecimal("locationZ"));
            db.set(index++, jsonObject.getBigDecimal("orientationX"));
            db.set(index++, jsonObject.getBigDecimal("orientationY"));
            db.set(index++, jsonObject.getBigDecimal("orientationZ"));
            db.set(index++, jsonObject.getBigDecimal("orientationW"));
            db.addBatch();
        }
        db.dbBatchUpdate();
    }

    /**
     * 添加监测点位姿信息
     * @param pointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    @Override
    public void add(PointInfo pointInfo){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into along_work_point_info ");
        stringBuffer.append("  (room_id, point_name, location_x, location_y, location_z, ");
        stringBuffer.append("   orientation_x, orientation_y, orientation_z, orientation_w) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?, ?, ?) ");
        db.setSql(stringBuffer.toString());
        int index=1;
        db.set(index++, pointInfo.getRoomId());
        db.set(index++, pointInfo.getPointName());
        db.set(index++, pointInfo.getLocationX());
        db.set(index++, pointInfo.getLocationY());
        db.set(index++, pointInfo.getLocationZ());

        db.set(index++, pointInfo.getOrientationX());
        db.set(index++, pointInfo.getOrientationY());
        db.set(index++, pointInfo.getOrientationZ());
        db.set(index++, pointInfo.getOrientationW());
        db.dbUpdate();
    }

    /**
     * 更新监测点位姿信息
     * @param pointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    @Override
    public void update(PointInfo pointInfo){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update along_work_point_info ");
        stringBuffer.append("   set location_x = ?, location_y = ?, location_z = ?, orientation_x = ?, orientation_y = ?, ");
        stringBuffer.append("       orientation_z = ?, orientation_w = ? ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ? ");
        db.setSql(stringBuffer.toString());
        int index=1;
        db.set(index++, pointInfo.getLocationX());
        db.set(index++, pointInfo.getLocationY());
        db.set(index++, pointInfo.getLocationZ());
        db.set(index++, pointInfo.getOrientationX());
        db.set(index++, pointInfo.getOrientationY());

        db.set(index++, pointInfo.getOrientationZ());
        db.set(index++, pointInfo.getOrientationW());
        db.set(index++, pointInfo.getRoomId());
        db.set(index++, pointInfo.getPointName());
        db.dbUpdate();
    }

    /**
     * 校验检测点位姿是否存在
     * @param pointInfo
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:18
     */
    @Override
    public boolean checkExist(PointInfo pointInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 from along_work_point_info where room_id = ? and point_name = ? ");
        db.setSql(stringBuffer.toString());
        int index=1;
        db.set(index++, pointInfo.getRoomId());
        db.set(index++, pointInfo.getPointName());
        List list = db.dbQuery();
        if(list.size()==0){
            return false;
        }
        return true;
    }
}