package com.inspur.industrialinspection.service;

/**
 * 告警发送短信服务
 * @author kliu
 * @date 2022/9/15 14:10
 */
public interface SendSmsService {

    /**
     * 发送短信
     * @param phoneNumbers
     * @param templateParam
     * @param templateCode
     * @return void
     * @author kliu
     * @date 2022/11/15 18:57
     */
    void sendSms(String phoneNumbers, String templateCode, String templateParam) throws Exception;
}
