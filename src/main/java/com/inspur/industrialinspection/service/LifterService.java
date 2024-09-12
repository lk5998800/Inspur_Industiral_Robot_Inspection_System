package com.inspur.industrialinspection.service;

/**
 * 升降杆相关服务
 * @author kliu
 * @date 2022/8/11 10:35
 */
public interface LifterService {
    /**
     * 下发升降杆任务
     * @param robotId
     * @param count
     * @return void
     * @author kliu
     * @date 2022/8/11 10:38
     */
    void issuedLifterTask(long robotId, int count);

}
