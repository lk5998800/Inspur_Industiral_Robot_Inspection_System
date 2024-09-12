package com.inspur.industrialinspection.service;
/**
 * request 服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RequestService {
    /**
     * 根据token获取园区信息
     * @param
     * @return int
     * @author kliu
     * @date 2022/6/14 16:52
     */
    int getParkIdByToken();
    /**
     * 根据token获取用户信息
     * @return int
     * @author kliu
     * @date 2022/6/27 20:10
     */
    long getUserIdByToken();

    /**
     * 根据token获取用户信息
     * @return int
     * @author kliu
     * @date 2022/6/27 20:10
     */
    long getUserIdByToken(String token);
}
