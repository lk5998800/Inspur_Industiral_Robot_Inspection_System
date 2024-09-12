package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.entity.RobotInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: kliu
 * @description: 机器人机房信息dao实现
 * @date: 2022/4/16 10:21
 */
@Repository
public class RobotRoomDaoImpl implements RobotRoomDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 根据机房id获取机器人id
     * @param roomId
     * @return long
     * @author kliu
     * @date 2022/5/24 18:23
     */
    @Override
    public long getRobotIdByRoomId(long roomId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" select robot_id           ");
        stringBuffer.append("   from robot_room         ");
        stringBuffer.append("  where room_id = ?        ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        List<RobotInfo> list = db.dbQuery(RobotInfo.class);
        if(list.size()==0){
            throw new RuntimeException("传入的roomId【"+roomId+"】不存在，请检查传入的数据");
        }
        return list.get(0).getRobotId();
    }

    /**
     * 添加
     * @param roomId
     * @param robotId
     * @author kliu
     * @date 2022/5/24 18:23
     */
    @Override
    public void add(long roomId, long robotId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into robot_room (room_id, robot_id) values (?, ?) ");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, robotId);
        db.dbUpdate();
    }

    /**
     * 依据机器人id获取机房id
     * @param robotId
     * @return long
     * @author kliu
     * @date 2022/5/24 18:23
     */
    @Override
    public long getRoomIdByRobotId(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(" select room_id           ");
        stringBuffer.append("   from robot_room         ");
        stringBuffer.append("  where robot_id = ?        ");
        db.setSql(stringBuffer.toString());
        db.set(1, robotId);
        List<RoomInfo> list = db.dbQuery(RoomInfo.class);
        if(list.size()==0){
            throw new RuntimeException("传入的robotId【"+robotId+"】不存在，请检查传入的数据");
        }
        return list.get(0).getRoomId();
    }
}
