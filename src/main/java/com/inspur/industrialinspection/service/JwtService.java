package com.inspur.industrialinspection.service;

/**
 * jwt 服务
 * @author kliu
 * @date 2022/5/9 17:26
 */
public interface JwtService {
    /**
     * 创建服务
     * @param sub
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:53
     */
    String createToken(String sub);
    /**
     * 获取token中的数据
     * @param token
     * @return java.lang.String
     * @author kliu
     * @date 2022/5/25 8:53
     */
    String getTokenData(String token);
    /**
     * 是否需要更新token
     * @param token
     * @return boolean
     * @author kliu
     * @date 2022/5/25 8:54
     */
    boolean isNeedUpdate(String token);
    /**
     * token是否失效
     * @param token
     * @return boolean
     * @author kliu
     * @date 2022/5/25 8:54
     */
    boolean isTokenValid(String token);
}
