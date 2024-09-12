package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;

import java.io.IOException;
/**
 * 检测点设置
 * @author kliu
 * @date 2022/5/25 8:58
 */
public interface PointSetService {
    /**
     * 获取检测点设置信息
     * @param roomId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/5/25 8:58
     */
    JSONObject list(long roomId);
    /**
     * 添加检测点设置信息
     * @param pointSetObject
     * @return void
     * @author kliu
     * @date 2022/5/25 8:58
     */
    void adds(JSONObject pointSetObject);
}
