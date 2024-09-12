package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskResult;

/**
 * @author: kliu
 * @description: 特定点巡检
 * @date: 2022/9/7 16:54
 */
public interface ParticularPointInspectionTaskResultDao {

    /**
     * 添加
     * @param particularPointInspectionTaskResult
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    void add(ParticularPointInspectionTaskResult particularPointInspectionTaskResult);
    /**
     * 添加
     * @param particularPointInspectionTaskResult
     * @return void
     * @author kliu
     * @date 2022/9/7 14:46
     */
    void update(ParticularPointInspectionTaskResult particularPointInspectionTaskResult);
    /**
     * 校验数据是否存在
     * @param particularPointInspectionTaskResult
     * @return boolean
     * @author kliu
     * @date 2022/9/7 14:46
     */
    boolean checkExist(ParticularPointInspectionTaskResult particularPointInspectionTaskResult);

    /**
     * 获取明细数据
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.ParticularPointInspectionTaskResult
     * @author kliu
     * @date 2022/9/14 10:41
     */
    ParticularPointInspectionTaskResult getDetlById(long instanceId);
}
