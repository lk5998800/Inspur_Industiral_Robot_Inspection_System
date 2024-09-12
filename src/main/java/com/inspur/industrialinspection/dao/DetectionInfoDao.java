package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.DetectionInfo;

import java.util.List;
/**
 * 检测项信息
 * @author: kliu
 * @date: 2022/4/18 20:21
 */
public interface DetectionInfoDao {
    /**
     * 获取所有列表
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionInfo>
     * @author kliu
     * @date 2022/5/24 18:09
     */
    List<DetectionInfo> list();
    /**
     * 依据id获取明细
     * @param detectionId
     * @return com.inspur.industrialinspection.entity.DetectionInfo
     * @author kliu
     * @date 2022/5/24 18:09
     */
    DetectionInfo getDetlById(String detectionId);
    /**
     * 校验是否存在
     * @param detectionId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:10
     */
    boolean checkExist(String detectionId);
}
