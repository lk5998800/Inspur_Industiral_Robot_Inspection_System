package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.dao.RoleFunctionDao;
import com.inspur.industrialinspection.dao.RoleInfoDao;
import com.inspur.industrialinspection.dao.UserRoleDao;
import com.inspur.industrialinspection.entity.RoleFunction;
import com.inspur.industrialinspection.entity.RoleInfo;
import com.inspur.industrialinspection.entity.UserRole;
import com.inspur.industrialinspection.service.RoleFunctionService;
import com.inspur.industrialinspection.service.RoleInfoService;
import com.inspur.industrialinspection.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色信息服务实现
 * @author wangzhaodi
 * @date 2022/11/15 9:37
 */
@Service
@Slf4j
public class RoleInfoServiceImpl implements RoleInfoService {
    @Autowired
    private RoleInfoDao roleInfoDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private RoleFunctionDao roleFunctionDao;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RoleFunctionService roleFunctionService;

    /**
     * 获取角色信息
     * @return java.util.List<com.inspur.industrialinspection.entity.RoleInfo>
     * @author wangzhaodi
     * @date 2022/11/15 9:37
     */
    @Override
    public List<RoleInfo> list(long userId) {
        return roleInfoDao.list(userId);
    }
    /**
     * 添加角色信息
     * @param jsonObject
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/15 9:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(JSONObject jsonObject){
        String roleName = jsonObject.getStr("roleName");
        JSONArray jsonArray = jsonObject.getJSONArray("functionIds");

        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRoleName(roleName);
        long roleId = roleInfoDao.addAndReturnId(roleInfo);
        RoleFunction roleFunction;
        for (int i = 0; i < jsonArray.size(); i++) {
            roleFunction = new RoleFunction();
            roleFunction.setRoleId(roleId);
            roleFunction.setFunctionId(jsonArray.getInt(i));
            roleFunctionService.add(roleFunction);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserRoles(JSONObject jsonObject){
        JSONArray roleIds = jsonObject.getJSONArray("roleIds");
        Long userId = jsonObject.getLong("userId");
        UserRole userRole = new UserRole();
        userRoleService.deleteByUserId(userId);
        for (int i = 0; i < roleIds.size(); i++) {
            userRole.setRoleId(roleIds.getLong(i));
            userRole.setUserId(userId);
            userRoleService.add(userRole);
        }
    }

    /**
     * 更新角色信息
     * @param jsonObject
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/15 9:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(JSONObject jsonObject){
        Long roleId = jsonObject.getLong("roleId");
        String roleName = jsonObject.getStr("roleName");
        JSONArray jsonArray = jsonObject.getJSONArray("functionIds");

        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRoleId(roleId);
        roleInfo.setRoleName(roleName);

        roleInfoDao.update(roleInfo);
        roleFunctionDao.deleteByRoleId(roleId);
        RoleFunction roleFunction = new RoleFunction();
        roleFunction.setRoleId(roleId);
        for (int i = 0; i < jsonArray.size(); i++) {
            roleFunction.setFunctionId(jsonArray.getInt(i));
            roleFunctionService.add(roleFunction);
        }
    }

    /**
     * 删除角色信息
     * @param roleInfo
     * @return void
     * @author wangzhaodi
     * @date 2022/11/15 9:37
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(RoleInfo roleInfo) {
        userRoleDao.deleteByRoleId(roleInfo.getRoleId());
        roleFunctionDao.deleteByRoleId(roleInfo.getRoleId());
        roleInfoDao.delete(roleInfo);
    }

}
