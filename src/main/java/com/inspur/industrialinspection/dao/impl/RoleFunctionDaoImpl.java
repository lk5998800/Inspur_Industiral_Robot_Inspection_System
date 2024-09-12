package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RoleFunctionDao;
import com.inspur.industrialinspection.entity.RoleFunction;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;



/**
 * 角色-功能关联信息dao实现
 * @author wangzhaodi
 * @date 2022/11/14 15:00
 */
@Repository
public class RoleFunctionDaoImpl implements RoleFunctionDao {

    @Autowired
    private BeanFactory beanFactory;


    @Override
    public long addAndReturnId(RoleFunction roleFunction) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into role_function ");
        stringBuffer.append("  (role_function_id,role_id, function_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roleFunction.getRoleId());
        db.set(index++, roleFunction.getFunctionId());
        return db.dbUpdateAndReturnId();
    }

    @Override
    public void deleteByRoleId(long roleId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from role_function where role_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roleId);
        db.dbUpdate();
    }
}
