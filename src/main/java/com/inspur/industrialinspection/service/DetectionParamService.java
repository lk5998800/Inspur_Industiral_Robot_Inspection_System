package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.DetectionParam;

import java.io.IOException;
import java.util.List;

/**
 * 检测项参数
 * @author kliu
 * @date 2022/5/25 8:45
 */
public interface DetectionParamService {
    /**
     * 获取检测项参数
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionParam>
     * @author kliu
     * @date 2022/6/14 16:55
     */
    List<DetectionParam> list(long roomId);
    /**
     * 添加检测项参数
     * @param detectionParam
     * @return void
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:55
     */
    void add(DetectionParam detectionParam) throws IOException;
}
