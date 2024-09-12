package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.UserRoleDao;
import com.inspur.industrialinspection.entity.UserRole;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;



/**
 * 用户-角色关联信息dao实现
 * @author wangzhaodi
 * @date 2022/11/14 15:00
 */
@Repository
public class UserRoleDaoImpl implements UserRoleDao {

    @Autowired
    private BeanFactory beanFactory;


    @Override
    public long addAndReturnId(UserRole userRole) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into user_role ");
        stringBuffer.append("  (user_role_id, user_id, role_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, userRole.getUserId());
        db.set(index++, userRole.getRoleId());
        return db.dbUpdateAndReturnId();
    }

    @Override
    public void deleteByRoleId(long roleId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from user_role where role_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roleId);
        db.dbUpdate();
    }

    @Override
    public void deleteByUserId(long userId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from user_role where user_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, userId);
        db.dbUpdate();
    }
}
