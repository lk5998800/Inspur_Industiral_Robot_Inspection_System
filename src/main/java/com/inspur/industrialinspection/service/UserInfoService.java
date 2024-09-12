package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.UserInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
/**
 * 用户信息服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface UserInfoService {
    /**
     * 校验用户密码并返回用户信息
     * @param userInfo
     * @return com.inspur.industrialinspection.entity.UserInfo
     * @author kliu
     * @date 2022/6/7 16:32
     */
    UserInfo checkPwdReturnUserInfo(UserInfo userInfo);
    /**
     * 获取用户信息
     * @return java.util.List<com.inspur.industrialinspection.entity.UserInfo>
     * @author kliu
     * @date 2022/6/7 16:33
     */
    List<UserInfo> list();
    /**
     * 添加用户信息
     * @param userInfo
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/13 14:23
     */
    void add(UserInfo userInfo) throws Exception;
    /**
     * 更新用户信息
     * @param userInfo
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/13 14:23
     */
    void update(UserInfo userInfo) throws Exception;
    /**
     * 删除用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:37
     */
    void delete(UserInfo userInfo);
    /**
     * 保存人脸照片到minio，同时记录到数据库表
     * @param multipartFile
     * @param userId
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/6/25 17:48
     */
    void saveFrofile(MultipartFile multipartFile, long userId) throws Exception;
    /**
     * 修改密码
     * @param userId
     * @param userPwdOrigin
     * @param userPwdNew
     * @return void
     * @author kliu
     * @date 2022/7/15 13:45
     */
    void changePwd(long userId, String userPwdOrigin, String userPwdNew);

    /**
     * 根据机器人唯一标识获取人员列表
     * @param robotId
     * @return
     */
    List<UserInfo> getByRobotIdPersonList(long robotId);
}
