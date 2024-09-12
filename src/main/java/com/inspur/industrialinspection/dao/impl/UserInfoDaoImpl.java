package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.UserInfoDao;
import com.inspur.industrialinspection.entity.UserInfo;
import com.inspur.industrialinspection.service.RequestService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户信息dao实现
 * @author kliu
 * @date 2022/4/21 17:56
 */
@Repository
public class UserInfoDaoImpl implements UserInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private RequestService requestService;

    /**
     * 依据登录名获取用户信息
     * @param loginName
     * @return com.inspur.industrialinspection.entity.UserInfo
     * @author kliu
     * @date 2022/5/24 20:12
     */
    @Override
    public UserInfo getUserByLoginName(String loginName) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("select * from user_info where login_name = ?");
        db.set(1, loginName);
        List<UserInfo> list = db.dbQuery(UserInfo.class);
        if(list.size()==0){
            return null;
        }
        return list.get(0);
    }
    /**
     * 获取机房下的用户信息
     * @param roomId
     * @return com.inspur.industrialinspection.entity.UserInfo
     * @author kliu
     * @date 2022/5/24 20:12
     */
    @Override
    public UserInfo getUserByRoomId(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("select * from user_info where room_id = ?");
        db.set(1, roomId);
        List<UserInfo> list = db.dbQuery(UserInfo.class);
        if(list.size()==0){
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取所有用户信息
     * @return java.util.List<com.inspur.industrialinspection.entity.UserInfo>
     * @author kliu
     * @date 2022/5/24 20:13
     */
    @Override
    public List<UserInfo> list() {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select user_id, login_name, user_name, user_email, user_tel, in_use, room_id, face_profile_url ");
        stringBuffer.append("  from user_info a ");
        stringBuffer.append(" where exists (select 1 ");
        stringBuffer.append("          from room_info ");
        stringBuffer.append("         where room_id = a.room_id ");
        stringBuffer.append("           and park_id = ?)");
        db.setSql(stringBuffer.toString());
        db.set(1,parkId);
        return db.dbQuery(UserInfo.class);
    }

    /**
     * 添加用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:13
     */
    @Override
    public long addAndReturnId(UserInfo userInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into user_info ");
        stringBuffer.append("  (user_id, login_name, user_name, user_pwd, user_email, user_tel, in_use, room_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ?, '0', ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, userInfo.getLoginName());
        db.set(index++, userInfo.getUserName());
        db.set(index++, userInfo.getUserPwd());
        db.set(index++, userInfo.getUserEmail());
        db.set(index++, userInfo.getUserTel());
        db.set(index++, userInfo.getRoomId());
        return db.dbUpdateAndReturnId();
    }

    /**
     * 更新用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:13
     */
    @Override
    public void update(UserInfo userInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update user_info ");
        stringBuffer.append("   set login_name = ?, user_name = ?, user_email = ?, user_tel = ?, room_id = ? ");
        stringBuffer.append(" where user_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, userInfo.getLoginName());
        db.set(index++, userInfo.getUserName());
        db.set(index++, userInfo.getUserEmail());
        db.set(index++, userInfo.getUserTel());
        db.set(index++, userInfo.getRoomId());
        db.set(index++, userInfo.getUserId());
        db.dbUpdate();
    }

    /**
     * 删除用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:13
     */
    @Override
    public void delete(UserInfo userInfo) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("delete from user_info where user_id = ?");
        db.set(1, userInfo.getUserId());
        db.dbUpdate();
    }

    /**
     * 校验用户id是否存在
     * @param userId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:13
     */
    @Override
    public boolean checkExist(long userId) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("select 1 from user_info where user_id = ?");
        db.set(1, userId);
        return db.dbQuery().size()>0;
    }

    /**
     * 保存保存人像图片url
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/6/25 16:02
     */
    @Override
    public void saveFaceProfileUrlAndFacialFeature(UserInfo userInfo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update user_info ");
        stringBuffer.append("   set face_profile_url = ?, facial_feature = ? ");
        stringBuffer.append(" where user_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, userInfo.getFaceProfileUrl());
        db.set(index++, userInfo.getFacialFeature());
        db.set(index++, userInfo.getUserId());
        db.dbUpdate();
    }

    @Override
    public UserInfo getDetlById(long userId) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("select * from user_info where user_id = ?");
        db.set(1, userId);
        List<UserInfo> list = db.dbQuery(UserInfo.class);
        if (list.size()==0){
            throw new RuntimeException("传入的用户id不存在，请检查");
        }
        return list.get(0);
    }

    @Override
    public void updatePwd(long userId, String userPwd){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update user_info ");
        stringBuffer.append("   set user_pwd = ? ");
        stringBuffer.append(" where user_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, userPwd);
        db.set(index++, userId);
        db.dbUpdate();
    }

    @Override
    public List<UserInfo> getByRobotIdPersonList(long robotId) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("select * from user_info where room_id in(select room_id from room_info where park_id = (select park_id FROM robot_room INNER JOIN room_info ON robot_room.room_id = room_info.room_id  WHERE robot_room.robot_id = ?))");
        db.set(1, robotId);
        List<UserInfo> list = db.dbQuery(UserInfo.class);
        if (list.size()==0){
           return null;
        }
        return list;
    }
}
