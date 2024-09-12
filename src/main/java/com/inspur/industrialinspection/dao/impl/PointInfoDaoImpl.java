package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.PointInfoDao;
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
public class PointInfoDaoImpl implements PointInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 获取机房下所有检测点位姿信息
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/5/24 18:17
     */
    @Override
    public List list(long roomId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from point_info where room_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        return db.dbQuery(PointInfo.class);
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
        stringBuffer.append("insert into point_info ");
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
        stringBuffer.append("update point_info ");
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
        stringBuffer.append("select 1 from point_info where room_id = ? and point_name = ? ");
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

    /**
     * 获取检测点位姿明细
     * @param pointInfo
     * @return com.inspur.industrialinspection.entity.PointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    @Override
    public PointInfo getDetlById(PointInfo pointInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from point_info where room_id = ? and point_name = ? ");
        db.setSql(stringBuffer.toString());
        int index=1;
        db.set(index++, pointInfo.getRoomId());
        db.set(index++, pointInfo.getPointName());
        List<PointInfo> list = db.dbQuery(PointInfo.class);
        if(list.size()==0){
            throw new RuntimeException("未查询到机房【"+pointInfo.getRoomId()+"】对应检测点【"+pointInfo.getPointName()+"】信息，请检查");
        }
        return list.get(0);
    }
}
