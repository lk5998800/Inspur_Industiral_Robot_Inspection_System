package com.inspur.industrialinspection.dao.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.AlongWorkPointInfoDao;
import com.inspur.industrialinspection.dao.ExplainPointInfoDao;
import com.inspur.industrialinspection.entity.ExplainPointInfo;
import com.inspur.industrialinspection.entity.PointInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: LiTan
 * @description:    检测点基本信息dao实现
 * @date:   2022-11-01 10:00:03
 */
@Repository
public class ExplainPointInfoDaoImpl implements ExplainPointInfoDao {

    @Autowired
    private BeanFactory beanFactory;


  /**
   * @author: LiTan
   * @description:    获取列表
   * @date:   2022-11-01 10:00:13
   */
    @Override
    public List<ExplainPointInfo> list(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from explain_point_info LEFT JOIN explain_point_skill ON explain_point_info.point_name = explain_point_skill.point_name where explain_point_info.room_id = ? and explain_point_skill.broadcast != ''");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        return db.dbQuery(ExplainPointInfo.class);
    }

  /**
   * @author: LiTan
   * @description:    删除
   * @date:   2022-11-01 10:32:57
   */
    @Override
    public void delete(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from explain_point_info where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.dbUpdate();
    }

    /**
     * 添加
     * @param array
     */
    @Override
    public void add(JSONArray array) {
        Db db = beanFactory.getBean(Db.class);
        JSONObject jsonObject;
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into along_work_point_info(room_id, point_name, location_x, location_y, location_z,orientation_x, orientation_y, orientation_z, orientation_w) values(?, ?, ?, ?, ?,?, ?, ?, ?) ");
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
     */
    @Override
    public void add(PointInfo pointInfo){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into explain_point_info(room_id, point_name, location_x, location_y, location_z,orientation_x, orientation_y, orientation_z, orientation_w)values (?, ?, ?, ?, ?,?, ?, ?, ?)");
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
     */
    @Override
    public void update(PointInfo pointInfo){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update explain_point_info set location_x = ?, location_y = ?, location_z = ?, orientation_x = ?, orientation_y = ?,orientation_z = ?, orientation_w = ? where room_id = ? and point_name = ?");
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
     * @return
     */
    @Override
    public boolean checkExist(PointInfo pointInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from explain_point_info where room_id = ? and point_name = ? ");
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

    @Override
    public ExplainPointInfo getPointInfo(long roomId, String point) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from explain_point_info where room_id = ? and point_name = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, point);
        List<ExplainPointInfo> list = db.dbQuery(ExplainPointInfo.class);
        if(list.size() == 0){
            return null;
        }
        return list.get(0);
    }
}