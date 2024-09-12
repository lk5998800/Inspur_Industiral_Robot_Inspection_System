package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.CabinetUbit;

import java.util.List;

/**
 * 机柜u位
 * @author kliu
 * @date 2022/11/9 20:20
 */
public interface CabinetUbitService {
    /**
     * 获取列表
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/11/9 20:21
     */
    List list(long roomId);
    /**
     * 添加
     * @param cabinetUbit
     * @return void
     * @author kliu
     * @date 2022/11/9 20:21
     */
    void add(CabinetUbit cabinetUbit);
}
