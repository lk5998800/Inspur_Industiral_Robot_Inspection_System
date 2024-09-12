package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.UserRole;
import org.springframework.stereotype.Service;

/**
 * 用户-角色关联服务
 * @author wangzhaodi
 * @date 2022/11/14 15:22
 */
@Service
public interface UserRoleService {

    /**
     * 添加用户-角色关联信息
     * @param userRole
     * @author wangzhaodi
     * @date 2022/11/14 15:29
     */
    void add(UserRole userRole);
    /**
     * 依据用户id删除
     * @param userId
     * @return void
     * @author kliu
     * @date 2022/11/21 12:04
     */
    void deleteByUserId(long userId);
}
