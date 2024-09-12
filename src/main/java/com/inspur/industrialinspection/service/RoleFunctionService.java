package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.RoleFunction;
import org.springframework.stereotype.Service;


/**
 * 角色-功能关联服务
 * @author wangzhaodi
 * @date 2022/11/14 15:22
 */
@Service
public interface RoleFunctionService {

    /**
     * 添加角色-功能关联信息
     * @param roleFunction
     * @author wangzhaodi
     * @date 2022/11/14 15:29
     */
    void add(RoleFunction roleFunction);
}
