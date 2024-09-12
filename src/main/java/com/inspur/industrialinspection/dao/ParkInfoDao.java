package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ParkInfo;

/**
 * @author kliu
 * @description 园区信息dao
 * @date 2022/5/6 16:42
 */
public interface ParkInfoDao {
    /**
     * 依据园区id获取园区明细
     * @param parkId
     * @return com.inspur.industrialinspection.entity.ParkInfo
     * @author kliu
     * @date 2022/5/24 18:16
     */
    ParkInfo getDetlById(int parkId);
}
