package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.inspur.industrialinspection.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * jwt 服务
 * @author kliu
 * @date 2022/5/9 17:26
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expireTime}")
    private String expireTime;

    /**
     * 创建服务
     * @param sub
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:53
     */
    @Override
    public String createToken(String sub){
        return JWT.create()
                .withSubject(sub)
                .withExpiresAt(DateUtil.offset(DateUtil.date(), DateField.MINUTE, Integer.parseInt(expireTime)))
                .sign(Algorithm.HMAC512(secret));
    }

    /**
     * 获取token中的数据
     * @param token
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:53
     */
    @Override
    public String getTokenData(String token){
        try {
            return JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (TokenExpiredException e){
            throw new TokenExpiredException("token已经过期");
        } catch (Exception e){
            throw new RuntimeException("token验证失败"+e.getMessage());
        }
    }
    /**
     * 是否需要更新token
     * @param token
     * @return boolean
     * @author kliu
     * @date 2022/5/25 8:54
     */
    @Override
    public boolean isNeedUpdate(String token){
        //获取token过期时间
        Date expiresAt;
        try {
            expiresAt = JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(token)
                    .getExpiresAt();
        } catch (TokenExpiredException e){
            throw new TokenExpiredException("token已过期，请重新登录");
        } catch (Exception e){
            throw new RuntimeException("token验证失败"+e.getMessage());
        }
        //如果剩余过期时间少于过期时常的一半时 需要更新
        return (expiresAt.getTime()-System.currentTimeMillis())/1000/60 > Integer.parseInt(expireTime)/2;
    }

    /**
     * token是否失效
     * @param token
     * @return boolean
     * @author kliu
     * @date 2022/5/25 8:54
     */
    @Override
    public boolean isTokenValid(String token){
        if (token == null){
            return false;
        }
        //获取token过期时间
        Date expiresAt;
        try {
            expiresAt = JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(token)
                    .getExpiresAt();
        } catch (TokenExpiredException e){
            throw new TokenExpiredException("token已过期，请重新登录");
        } catch (Exception e){
            throw new RuntimeException("token验证失败"+e.getMessage());
        }
        //如果剩余过期时间少于过期时常的一半时 需要更新
        return true;
    }
}
