package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RoleFunction;


/**
 * @author wangzhaodi
 * @description 角色-功能关联信息
 * @date 2022/11/14 14:58
 */
public interface RoleFunctionDao {


    /**
     * 添加角色-功能关联信息
     * @param roleFunction
     * @return long
     * @author wangzhaodi
     * @date 2022/11/14 14:58
     */
    long addAndReturnId(RoleFunction roleFunction);

    /**
     * 删除角色-功能关联信息
     * @param roleId
     * @return long
     * @author wangzhaodi
     * @date 2022/11/14 14:58
     */
    void deleteByRoleId(long roleId);

}
