package com.inspur.industrialinspection.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.inspur.industrialinspection.entity.UserInfo;
import com.inspur.industrialinspection.service.JwtService;
import com.inspur.industrialinspection.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author kliu
 * @description request服务
 * @date 2022/5/11 9:55
 */
@Service
@Slf4j
public class RequestServiceImpl implements RequestService {

    @Autowired
    JwtService jwtService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public int getParkIdByToken() {
        String token = httpServletRequest.getHeader("token");
        String tokenData = jwtService.getTokenData(token);
        UserInfo userInfo = JSONObject.parseObject(tokenData, UserInfo.class);
        int parkId = userInfo.getParkId();
        return parkId;
    }
    @Override
    public long getUserIdByToken() {
        String token = httpServletRequest.getHeader("token");
        if (token==null || token.length() == 24){
            return 0;
        }
        String tokenData = jwtService.getTokenData(token);
        UserInfo userInfo = JSONObject.parseObject(tokenData, UserInfo.class);
        long userId = userInfo.getUserId();
        return userId;
    }

    /**
     * 根据token获取用户信息
     *
     * @param token
     * @return int
     * @author kliu
     * @date 2022/6/27 20:10
     */
    @Override
    public long getUserIdByToken(String token) {
        String tokenData = jwtService.getTokenData(token);
        UserInfo userInfo = JSONObject.parseObject(tokenData, UserInfo.class);
        long userId = userInfo.getUserId();
        return userId;
    }
}
