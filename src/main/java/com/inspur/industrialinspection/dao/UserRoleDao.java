package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.UserRole;


/**
 * @author wangzhaodi
 * @description 用户-角色关联信息
 * @date 2022/11/14 14:58
 */
public interface UserRoleDao {

    /**
     * 添加用户-角色关联信息
     * @param userRole
     * @return long
     * @author wangzhaodi
     * @date 2022/11/14 14:58
     */
    long addAndReturnId(UserRole userRole);

    /**
     * 删除用户-角色关联信息
     * @param roleId
     * @return long
     * @author wangzhaodi
     * @date 2022/11/14 14:58
     */
    void deleteByRoleId(long roleId);
    /**
     * 删除用户-角色关联信息-依据用户
     * @param userId
     * @return long
     * @author wangzhaodi
     * @date 2022/11/14 14:58
     */
    void deleteByUserId(long userId);
}
