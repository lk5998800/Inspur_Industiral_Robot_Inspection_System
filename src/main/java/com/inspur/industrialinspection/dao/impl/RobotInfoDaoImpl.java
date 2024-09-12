package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RobotInfoDao;
import com.inspur.industrialinspection.entity.RobotInfo;
import com.inspur.industrialinspection.service.RequestService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: kliu
 * @description: 机器人基本信息dao实现
 * @date: 2022/4/16 10:21
 */
@Repository
public class RobotInfoDaoImpl implements RobotInfoDao {

    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    RequestService requestService;

    /**
     * 获取机器人明细数据
     * @param robotId
     * @return com.inspur.industrialinspection.entity.RobotInfo
     * @author kliu
     * @date 2022/5/24 18:20
     */
    @Override
    public RobotInfo getDetlById(long robotId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select robot_id, robot_name ");
        stringBuffer.append("  from robot_info ");
        stringBuffer.append(" where robot_id = ? ");
        db.setSql(stringBuffer.toString());
        db.set(1, robotId);
        List<RobotInfo> list = db.dbQuery(RobotInfo.class);
        if(list.size()==0){
            throw new RuntimeException("传入的robotId【"+robotId+"】不存在，请检查传入的数据");
        }
        return list.get(0);
    }

    /**
     * 依据机房获取机器人信息
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.RobotInfo>
     * @author kliu
     * @date 2022/5/24 18:21
     */
    @Override
    public List<RobotInfo> list(int parkId, long roomId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select robot_id, robot_name, in_use ");
        stringBuffer.append("  from robot_info a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from robot_room b ");
        stringBuffer.append("         where b.robot_id = a.robot_id ");
        stringBuffer.append("           and exists (select 1 ");
        stringBuffer.append("                  from room_info ");
        stringBuffer.append("                 where room_id = b.room_id ");
        if(roomId>0){
            stringBuffer.append("               and room_id = ? ");
        }
        stringBuffer.append("                   and park_id = ?))");
        db.setSql(stringBuffer.toString());
        int index = 1;
        if(roomId>0){
            db.set(index++, roomId);
        }
        db.set(index++, parkId);
        return db.dbQuery(RobotInfo.class);
    }

    /**
     * 获取所有机器人信息
     * @return java.util.List<com.inspur.industrialinspection.entity.RobotInfo>
     * @author kliu
     * @date 2022/6/7 14:03
     */
    @Override
    public List<RobotInfo> listWithoutPark(){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select robot_id, robot_name, in_use ");
        stringBuffer.append("  from robot_info a ");
        db.setSql(stringBuffer.toString());
        return db.dbQuery(RobotInfo.class);
    }

    /**
     * 判断机器人是否存在
     * @param robotId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:21
     */
    @Override
    public boolean checkIsExist(long robotId){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 from robot_info where robot_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, robotId);
        if(db.dbQuery().size()==0){
            return false;
        }
        return true;
    }

    /**
     * 添加机房信息
     * @param robotInfo
     * @author kliu
     * @date 2022/5/24 18:21
     */
    @Override
    public void add(RobotInfo robotInfo){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into robot_info ");
        stringBuffer.append("  (robot_id, robot_name, in_use) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, '0') ");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, robotInfo.getRobotId());
        db.set(index++,robotInfo.getRobotName());
        db.dbUpdate();
    }

}
