package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.dao.UserInfoDao;
import com.inspur.industrialinspection.entity.UserInfo;
import com.inspur.industrialinspection.service.AiAgentService;
import com.inspur.industrialinspection.service.UserInfoService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
/**
 * 用户信息服务实现
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.endpoint}")
    private String minioBasic;
    @Autowired
    private AiAgentService aiAgentService;
    @Value("${aiagent.service.url}")
    private String aiagentUrl;
    @Value("${aiagent.service.faceverifyfeature.url}")
    private String faceverifyfeatureUrl;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final String FACE_PROFILE_MINIO_BUCKET = "faceprofile";

    /**
     * 校验密码返回用户信息
     * @param userInfo
     * @return com.inspur.industrialinspection.entity.UserInfo
     * @author kliu
     * @date 2022/6/13 18:28
     */
    @Override
    public UserInfo checkPwdReturnUserInfo(UserInfo userInfo) {
        String userPwd = userInfo.getUserPwd();
        String loginName = userInfo.getLoginName();
        UserInfo dbUserInfo = userInfoDao.getUserByLoginName(loginName);
        if (dbUserInfo==null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String dbUserPwd = dbUserInfo.getUserPwd();
        boolean passwordCheckSuccess = bCryptPasswordEncoder.matches(userPwd, dbUserPwd);
        if (!passwordCheckSuccess) {
            throw new RuntimeException("用户名或密码错误");
        }

        dbUserInfo.setUserPwd("");

        long roomId = dbUserInfo.getRoomId();
        int parkId = roomInfoDao.getDetlById(roomId).getParkId();
        dbUserInfo.setParkId(parkId);
        return dbUserInfo;
    }

    /**
     * 获取用户列表
     * @param
     * @return java.util.List<com.inspur.industrialinspection.entity.UserInfo>
     * @author kliu
     * @date 2022/6/13 18:28
     */
    @Override
    public List<UserInfo> list() {
        return userInfoDao.list();
    }

    /**
     * 添加用户
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/6/13 18:28
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(UserInfo userInfo) throws Exception {
        long roomId = userInfo.getRoomId();
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入机房id不存在，请检查传入的数据");
        }
        String defaultPwd = "12345678";
        String userPwd = userInfo.getUserPwd();
        if (StringUtils.isEmpty(userPwd)){
            userPwd = defaultPwd;
        }
        String nullStr = "null";
        if (nullStr.equals(userInfo.getUserTel())){
            userInfo.setUserTel("");
        }
        if (nullStr.equals(userInfo.getUserEmail())){
            userInfo.setUserEmail("");
        }
        String userPwdEncode = bCryptPasswordEncoder.encode(userPwd);
        userInfo.setUserPwd(userPwdEncode);
        long userId = userInfoDao.addAndReturnId(userInfo);
        //saveFrofile(file, userId);
    }

    /**
     * 更新用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/6/13 18:29
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserInfo userInfo) throws Exception {
        long roomId = userInfo.getRoomId();
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入机房id不存在，请检查传入的数据");
        }

        String nullStr = "null";

        if (nullStr.equals(userInfo.getUserTel())){
            userInfo.setUserTel("");
        }
        if (nullStr.equals(userInfo.getUserEmail())){
            userInfo.setUserEmail("");
        }

        userInfoDao.update(userInfo);

//        if (file != null){
//            saveFrofile(file, userInfo.getUserId());
//        }
    }

    /**
     * 删除用户信息
     * @param userInfo
     * @return void
     * @author kliu
     * @date 2022/6/13 18:29
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UserInfo userInfo) {
        userInfoDao.delete(userInfo);
    }

    /**
     * 保存人脸照片到minio，同时记录到数据库表
     * @param multipartFile
     * @param userId
     * @return void
     * @author kliu
     * @date 2022/6/25 15:36
     */
    @Override
    public void saveFrofile(MultipartFile multipartFile, long userId) throws Exception {
        String imgType = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        InputStream in = multipartFile.getInputStream();
        String fileName = userId+imgType;
        minioClient.putObject(FACE_PROFILE_MINIO_BUCKET, userId+imgType, in, new PutObjectOptions(in.available(), -1));
        in.close();

        String faceprofileUrl = minioBasic+"/faceprofile/"+fileName;
        //调用ai获取人脸特征值
        String url = aiagentUrl+faceverifyfeatureUrl;
        JSONObject serviceObject = new JSONObject();
        serviceObject.set("image_url", new String[]{faceprofileUrl});
        JSONObject serviceResult = aiAgentService.invokeHttp(url, serviceObject.toString());
        Object facialFeature = serviceResult.get("face_feature");
        //做一层转化存到数据库中
        JSONObject json = new JSONObject();
        json.set("face_feature", facialFeature);

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setFaceProfileUrl(faceprofileUrl);
        userInfo.setFacialFeature(json.toString());
        userInfoDao.saveFaceProfileUrlAndFacialFeature(userInfo);
    }

    /**
     * 修改密码
     *
     * @param userId
     * @param userPwdOrigin
     * @param userPwdNew
     * @return void
     * @author kliu
     * @date 2022/7/15 13:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePwd(long userId, String userPwdOrigin, String userPwdNew) {
        UserInfo userInfo = userInfoDao.getDetlById(userId);
        String userPwd = userInfo.getUserPwd();

        boolean passwordCheckSuccess = bCryptPasswordEncoder.matches(userPwdOrigin, userPwd);
        if (!passwordCheckSuccess) {
            throw new RuntimeException("原密码错误，请输入正确的原密码");
        }

        String userPwdNewEncode = bCryptPasswordEncoder.encode(userPwdNew);
        userInfoDao.updatePwd(userId, userPwdNewEncode);
    }

    @Override
    public List<UserInfo> getByRobotIdPersonList(long robotId) {
        return userInfoDao.getByRobotIdPersonList(robotId);
    }
}
