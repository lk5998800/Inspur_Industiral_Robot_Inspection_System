package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.WarnSms;

/**
 * @author kliu
 * @description 短信发送
 * @date 2022/4/29 10:15
 */
public interface WarnSmsDao {
    /**
     * 添加告警短信记录
     * @param warnSms
     * @return void
     * @author kliu
     * @date 2022/5/24 19:32
     */
    void add(WarnSms warnSms);
    /**
     * 校验数据是否存在
     * @param taskId
     * @param pointName
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:17
     */
    boolean checkExist(long taskId, String pointName);
}
