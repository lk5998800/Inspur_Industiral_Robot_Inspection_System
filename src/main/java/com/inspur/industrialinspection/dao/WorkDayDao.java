package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.GatingPara;
import com.inspur.industrialinspection.entity.WorkDay;

/**
 * 工作日信息
 * @author kliu
 * @date 2022/7/27 15:20
 */
public interface WorkDayDao {
    /**
     * 获取明细数据
     * @param dateStr
     * @return com.inspur.industrialinspection.entity.WorkDay
     * @author kliu
     * @date 2022/7/27 15:21
     */
    WorkDay getDetlById(String dateStr);
}
