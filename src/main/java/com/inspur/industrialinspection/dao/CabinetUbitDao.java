package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.CabinetUbit;

import java.util.List;

/**
 * @author 
 * @description 
 * @date 
 */
public interface CabinetUbitDao {
    /**
     * 查询
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/11/5 15:01
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
    /**
     * 修改
     * @param cabinetUbit
     * @return void
     * @author kliu
     * @date 2022/11/9 20:21
     */
    void update(CabinetUbit cabinetUbit);
    /**
     * 校验是否存在
     * @param cabinetUbit
     * @return void
     * @author kliu
     * @date 2022/11/9 20:21
     */
    boolean checkExist(CabinetUbit cabinetUbit);
}
