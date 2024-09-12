package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.RoleInfo;

import java.util.List;

/**
 * 角色服务
 * @author wangzhaodi
 * @date 2022/11/15 9:33
 */
public interface RoleInfoService {

    /**
     * 获取角色信息
     * @param userId
     * @return java.util.List<com.inspur.industrialinspection.entity.RoleInfo>
     * @author kliu
     * @date 2022/11/21 13:45
     */
    List<RoleInfo> list(long userId);
    /**
     * 添加角色信息
     * @param jsonObject
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/15 9:29
     */
    void add(JSONObject jsonObject);
    /**
     * 添加用户的角色
     * @param jsonObject
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/15 9:29
     */
    void addUserRoles(JSONObject jsonObject);
    /**
     * 更新角色信息
     * @param jsonObject
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/15 9:29
     */
    void update(JSONObject jsonObject);
    /**
     * 删除角色信息
     * @param roleInfo
     * @return void
     * @author wangzhaodi
     * @date 2022/11/15 9:29
     */
    void delete(RoleInfo roleInfo);

}
