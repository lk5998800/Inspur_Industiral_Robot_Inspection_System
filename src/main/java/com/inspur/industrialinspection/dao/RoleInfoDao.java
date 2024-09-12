package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.RoleInfo;

import java.util.List;

/**
 * @author wangzhaodi
 * @description 角色信息
 * @date 2022/11/14 14:58
 */
public interface RoleInfoDao {
    /**
     * 获取所有角色信息
     * @return java.util.List
     * @author wangzhaodi
     * @date 2022/11/15 9:47
     */
    List list(long userId);
    /**
     * 添加角色信息
     * @param roleInfo
     * @return void
     * @author wangzhaodi
     * @date 2022/11/15 9:47
     */
    long addAndReturnId(RoleInfo roleInfo);
    /**
     * 更新角色信息
     * @param roleInfo
     * @return void
     * @author wangzhaodi
     * @date 2022/11/15 9:47
     */
    void update(RoleInfo roleInfo);
    /**
     * 删除角色信息
     * @param roleInfo
     * @return void
     * @author wangzhaodi
     * @date 2022/11/15 9:47
     */
    void delete(RoleInfo roleInfo);

}
