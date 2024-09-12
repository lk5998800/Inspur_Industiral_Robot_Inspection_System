package com.inspur.industrialinspection.service;

import java.io.IOException;
import java.util.List;
/**
 * 检测项信息服务
 * @author kliu
 * @date 2022/5/25 8:44
 */
public interface DetectionInfoService {
    /**
     * 获取检测项信息带阈值
     * @param roomId
     * @param combinationCode
     * @return java.util.List
     * @author kliu
     * @date 2022/11/9 14:55
     */
    List list(long roomId, String combinationCode) throws IOException;
}
