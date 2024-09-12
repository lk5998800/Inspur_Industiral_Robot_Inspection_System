package com.inspur.industrialinspection.dao;

import java.util.List;

/**
 * @author: kliu
 * @description: 机械臂参数
 * @date: 2022/9/8 11:08
 */
public interface MechanicalArmParaDao {
    /**
     * 根据机房获取机械臂参数
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/9/8 11:09
     */
    List list(long roomId);
    /**
     * 根据机房和检测项id获取机械臂参数
     * @param roomId
     * @param detectionId
     * @return java.util.List
     * @author kliu
     * @date 2022/9/26 17:41
     */
    List list(long roomId, String detectionId);
}
