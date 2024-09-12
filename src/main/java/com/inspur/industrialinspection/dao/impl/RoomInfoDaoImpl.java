package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.service.RequestService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 机房基本信息dao实现
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class RoomInfoDaoImpl implements RoomInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private RequestService requestService;

    /**
     * 根据机器人id获取机房信息
     * @param robotId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomInfo>
     * @author kliu
     * @date 2022/5/24 19:03
     */
    @Override
    public List<RoomInfo> list(long robotId) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from room_info a ");
        stringBuffer.append(" where park_id = ? ");
        if(robotId>0){
            stringBuffer.append(" and exists (select 1 from robot_room where robot_id = ? and room_id = a.room_id)");
        }
        db.setSql(stringBuffer.toString());
        db.set(1, parkId);
        if(robotId>0){
            db.set(2, robotId);
        }
        return db.dbQuery(RoomInfo.class);
    }

    /**
     * 根据机器人id、楼栋获取机房信息
     * @param robotId
     * @param buildingId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomInfo>
     * @author kliu
     * @date 2022/6/17 19:50
     */
    @Override
    public List<RoomInfo> listWithBuilding(long robotId, long buildingId, int parkId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select a.*, ifnull((select robot_id from robot_room where room_id = a.room_id),0) robot_id ");
        stringBuffer.append("  from room_info a ");
        stringBuffer.append(" where park_id = ? ");
        if(buildingId>0){
            stringBuffer.append("   and building_id = ? ");
        }
        if(robotId>0){
            stringBuffer.append(" and exists (select 1 from robot_room where robot_id = ? and room_id = a.room_id)");
        }
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, parkId);
        if(buildingId>0){
            db.set(index++, buildingId);
        }
        if(robotId>0){
            db.set(index++, robotId);
        }
        return db.dbQuery(RoomInfo.class);
    }
    /**
     * 根据机器人id、园区获取机房信息
     * @param robotId
     * @param parkId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoomInfo>
     * @author kliu
     * @date 2022/6/17 19:50
     */
    @Override
    public List<RoomInfo> list(long robotId, long parkId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from room_info a ");
        stringBuffer.append(" where park_id = ? ");
        if(robotId>0){
            stringBuffer.append(" and exists (select 1 from robot_room where robot_id = ? and room_id = a.room_id)");
        }
        db.setSql(stringBuffer.toString());
        db.set(1, parkId);
        if(robotId>0){
            db.set(2, robotId);
        }
        return db.dbQuery(RoomInfo.class);
    }

    /**
     * 添加机房并返回id
     * @param roomInfo
     * @return long
     * @author kliu
     * @date 2022/5/24 19:04
     */
    @Override
    public long addAndReturnId(RoomInfo roomInfo){
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into room_info ");
        stringBuffer.append("  (room_id, park_id, room_name, room_addr, in_use, building_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, '0', ?) ");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, parkId);
        db.set(index++, roomInfo.getRoomName());
        db.set(index++, roomInfo.getRoomAddr());
        db.set(index++, roomInfo.getBuildingId());
        return db.dbUpdateAndReturnId();
    }

    /**
     * 更新机房信息
     * @param roomInfo
     * @author kliu
     * @date 2022/5/24 19:04
     */
    @Override
    public void update(RoomInfo roomInfo){
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" update room_info                        ");
        stringBuffer.append("    set room_name = ?, room_addr = ?, building_id = ? ,thumbnail_url = ?  ");
        stringBuffer.append("  where room_id = ?                               ");
        stringBuffer.append("    and park_id = ?                               ");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, roomInfo.getRoomName());
        db.set(index++, roomInfo.getRoomAddr());
        db.set(index++, roomInfo.getBuildingId());
        db.set(index++, roomInfo.getThumbnailUrl());
        db.set(index++, roomInfo.getRoomId());
        db.set(index++, parkId);
        db.dbUpdate();
    }

    /**
     * 删除机房信息
     * @param roomId
     * @author kliu
     * @date 2022/5/24 19:05
     */
    @Override
    public void delete(long roomId){
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" delete from room_info ");
        stringBuffer.append("  where room_id = ?  ");
        stringBuffer.append("    and park_id = ?  ");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, roomId);
        db.set(index++, parkId);
        db.dbUpdate();
    }

    /**
     * 校验数据是否存在
     * @param roomId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 19:05
     */
    @Override
    public boolean checkExist(long roomId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" select 1 from room_info ");
        stringBuffer.append("  where room_id = ? ");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, roomId);
        if(db.dbQuery().size()>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 根据机房id获取明细信息
     * @param roomId
     * @return com.inspur.industrialinspection.entity.RoomInfo
     * @author kliu
     * @date 2022/5/24 19:07
     */
    @Override
    public RoomInfo getDetlById(long roomId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from room_info ");
        stringBuffer.append(" where room_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        List list = db.dbQuery(RoomInfo.class);
        if(list.size()==0){
            throw new RuntimeException("传入的roomId【"+roomId+"】不存在，请检查传入的数据");
        }
        return (RoomInfo) list.get(0);
    }

    @Override
    public RoomInfo getByRoomNameAndParkId(String roomName, long parkId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from room_info ");
        stringBuffer.append(" where room_name = ? and park_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomName);
        db.set(2, parkId);
        List list = db.dbQuery(RoomInfo.class);
        if(list.size()==0){
            return null;
        }
        return (RoomInfo) list.get(0);
    }
}
