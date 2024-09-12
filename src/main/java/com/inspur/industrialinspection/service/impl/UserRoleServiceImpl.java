package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.dao.UserRoleDao;
import com.inspur.industrialinspection.entity.UserRole;
import com.inspur.industrialinspection.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * 用户-角色信息服务实现
 * @author wangzhaodi
 * @date 2022/11/15 19:43
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {
    @Autowired
    private UserRoleDao userRoleDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(UserRole userRole) {
        userRoleDao.addAndReturnId(userRole);
    }

    @Override
    public void deleteByUserId(long userId) {
        userRoleDao.deleteByUserId(userId);
    }
}
