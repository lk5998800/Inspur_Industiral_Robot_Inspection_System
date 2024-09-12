package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
/**
 * Ai Agent Service
 * @author kliu
 * @date 2022/5/9 17:26
 */
public interface AiAgentService {
    /**
     * 公共调用ai service方法
     * @param httpUrl
     * @param para
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/5/25 8:38
     */
    JSONObject invokeHttp(String httpUrl, String para);
}
