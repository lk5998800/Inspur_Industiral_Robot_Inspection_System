package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.dao.RoleFunctionDao;
import com.inspur.industrialinspection.entity.RoleFunction;
import com.inspur.industrialinspection.service.RoleFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户-角色信息服务实现
 * @author wangzhaodi
 * @date 2022/11/15 19:43
 */
@Service
public class RoleFunctionServiceImpl implements RoleFunctionService {
    @Autowired
    private RoleFunctionDao roleFunctionDao;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RoleFunction roleFunction) {
        roleFunctionDao.addAndReturnId(roleFunction);
    }
}
