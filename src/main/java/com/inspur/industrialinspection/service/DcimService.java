package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * DCIM调用服务
 * @author kliu
 * @date 2022/9/1 15:47
 */
public interface DcimService {
    /**
     * dcim调用
     * @param jsonObject
     * @param request
     * @return Object
     * @author kliu
     * @date 2022/9/1 15:47
     */
    Object dcimInvoke(JSONObject jsonObject, HttpServletRequest request);
}
