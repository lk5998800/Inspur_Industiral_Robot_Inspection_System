package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;

/**
 * 工作日信息
 * @author kliu
 * @date 2022/7/27 15:23
 */
public interface WorkDayService {
    /**
     * 判断是否是工作日
     * @param dateStr
     * @return boolean
     * @author kliu
     * @date 2022/7/27 15:23
     */
    boolean isWorkDay(String dateStr);
}
