package com.inspur.industrialinspection.service;

/**
 * 电话通知
 * @author kliu
 * @date 2022/11/15 14:34
 */
public interface PhoneNoticeService {

    /**
     * 电话通知
     * @param calledNumber  手机号码
     * @param ttsParam  参数
     * @param ttsCode  语音通知文本模板
     * @return void
     * @author kliu
     * @date 2022/11/15 14:36
     */
    void phoneNotice(String calledNumber, String ttsCode, String ttsParam) throws Exception;
}
