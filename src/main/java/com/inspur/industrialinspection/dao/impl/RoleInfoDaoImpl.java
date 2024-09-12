package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.RoleInfoDao;
import com.inspur.industrialinspection.entity.RoleInfo;
import com.inspur.industrialinspection.service.RequestService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色信息dao实现
 * @author wangzhaodi
 * @date 2022/11/14 15:00
 */
@Repository
public class RoleInfoDaoImpl implements RoleInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private RequestService requestService;

    /**
     * 获取角色信息
     * @param userId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoleInfo>
     * @author kliu
     * @date 2022/11/21 13:45
     */
    @Override
    public List<RoleInfo> list(long userId) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select a.role_id, ");
        stringBuffer.append("       a.role_name, ");
        stringBuffer.append("       group_concat(c.chinese_name order by c.function_id asc) as chinese_name, ");
        stringBuffer.append("       group_concat(c.function_id order by c.function_id asc) as function_id ");
        stringBuffer.append("  from role_info a, role_function b, function_info c ");
        stringBuffer.append(" where a.role_id = b.role_id ");
        stringBuffer.append("   and b.function_id = c.function_id ");
        stringBuffer.append("   and a.park_id = ? ");
        if (userId > 0){
            stringBuffer.append("   and exists (select 1 from user_role where user_id = ? and role_id = a.role_id) ");
        }
        stringBuffer.append(" group by a.role_id, a.role_name");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, parkId);
        if (userId > 0){
            db.set(index++, userId);
        }
        return db.dbQuery(RoleInfo.class);
    }
    /**
     * 添加角色信息
     * @param roleInfo
     * @return void
     * @author wangzhaodi
     * @date 2022/11/14 15:00
     */
    @Override
    public long addAndReturnId(RoleInfo roleInfo) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into role_info ");
        stringBuffer.append("  (role_id, role_name, park_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roleInfo.getRoleName());
        db.set(index++, parkId);
        return db.dbUpdateAndReturnId();
    }

    @Override
    public void update(RoleInfo roleInfo) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update role_info ");
        stringBuffer.append("   set role_name = ? ,park_id = ?");
        stringBuffer.append(" where role_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roleInfo.getRoleName());
        db.set(index++, parkId);
        db.set(index++, roleInfo.getRoleId());
        db.dbUpdate();
    }

    @Override
    public void delete(RoleInfo roleInfo) {
        Db db = beanFactory.getBean(Db.class);
        db.setSql("delete from role_info where role_id = ?");
        db.set(1, roleInfo.getRoleId());
        db.dbUpdate();
    }

}
