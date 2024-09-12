package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.FunctionInfoDao;
import com.inspur.industrialinspection.entity.FunctionInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 功能信息dao实现
 * @author wangzhaodi
 * @date 2022/11/14 15:00
 */
@Repository
public class FunctionInfoDaoImpl implements FunctionInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public List<FunctionInfo> getFunctionsByUserId(long userId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from function_info fi ");
        stringBuffer.append(" where fi.function_id in ");
        stringBuffer.append("       (select rf.function_id ");
        stringBuffer.append("          from role_function rf ");
        stringBuffer.append("         where rf.role_id in ");
        stringBuffer.append("               (select ur.role_id from user_role ur where ur.user_id = ?))");
        db.setSql(stringBuffer.toString());
        db.set(1,userId);
        List<FunctionInfo> list = db.dbQuery(FunctionInfo.class);
        return list;
    }

    @Override
    public List<FunctionInfo> list(long roleId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from function_info a ");
        if (roleId>0){
            stringBuffer.append(" where exists (select 1 from role_function where role_id = ? and function_id = a.function_id ) ");
        }
        db.setSql(stringBuffer.toString());
        if (roleId>0){
            db.set(1, roleId);
        }
        List<FunctionInfo> list = db.dbQuery(FunctionInfo.class);
        return list;
    }
}
