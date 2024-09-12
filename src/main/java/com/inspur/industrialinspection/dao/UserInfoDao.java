package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.UserInfo;
import java.util.List;

/**
 * @author kliu
 * @description 用户信息
 * @date 2022/4/21 17:57
 */
public interface UserInfoDao {
    /**
     * 依据登录名获取用户信息
     * @param loginName
     * @return com.inspur.industrialinspection.entity.UserInfo
     * @author kliu
     * @date 2022/5/24 20:12
     */
    UserInfo getUserByLoginName(String loginName);
    /**
     * 获取机房下的用户信息
     * @param roomId
     * @return com.inspur.industrialinspection.entity.UserInfo
     * @author kliu
     * @date 2022/5/24 20:12
     */
    UserInfo getUserByRoomId(long roomId);
    /**
     * 获取所有用户信息
     * @return java.util.List<com.inspur.industrialinspection.entity.UserInfo>
     * @author kliu
     * @date 2022/5/24 20:13
     */
    List<UserInfo> list();
    /**
     * 添加用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:13
     */
    long addAndReturnId(UserInfo userInfo);
    /**
     * 更新用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:13
     */
    void update(UserInfo userInfo);
    /**
     * 删除用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:13
     */
    void delete(UserInfo userInfo);
    /**
     * 校验用户id是否存在
     * @param userId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:13
     */
    boolean checkExist(long userId);

    /**
     * 保存保存人像图片url
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/6/25 16:02
     */
    void saveFaceProfileUrlAndFacialFeature(UserInfo userInfo);

    /**
     * 获取用户明细根据id
     * @param userId
     * @return com.inspur.industrialinspection.entity.UserInfo
     * @author kliu
     * @date 2022/6/25 17:03
     */
    UserInfo getDetlById(long userId);

    /**
     * 更新密码
     * @param userId
     * @param userPwd
     * @return void
     * @author kliu
     * @date 2022/7/15 14:00
     */
    void updatePwd(long userId, String userPwd);

    /**
     * 根据机器人唯一标识获取人员列表
     * @param robotId
     * @return
     */
    List<UserInfo> getByRobotIdPersonList(long robotId);
}
