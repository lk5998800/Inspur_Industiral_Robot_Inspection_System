package com.inspur.industrialinspection.dao;

/**
 * @author kliu
 * @description 任务检测项结果
 * @date 2022/4/18 20:25
 */
public interface TaskDetectionResultErrorBackDao {
    /**
     * 保存数据
     * @param taskLogId
     * @return void
     * @author kliu
     * @date 2022/8/6 10:05
     */
    void add(long taskLogId);
    /**
     * 校验数据是否存在
     * @param taskLogId
     * @return boolean
     * @author kliu
     * @date 2022/8/6 10:18
     */
    boolean checkExist(long taskLogId);
}
