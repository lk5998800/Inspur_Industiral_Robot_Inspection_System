package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.AlongWorkDtl;

import java.util.List;

/**
 * 随工明细
 * @author kliu
 * @date 2022/6/14 19:53
 */
public interface AlongWorkDtlDao {

    /**
     * 添加
     * @param alongWorkDtl
     * @return void
     * @author kliu
     * @date 2022/6/14 19:54
     */
    void add(AlongWorkDtl alongWorkDtl);
    /**
     * 修改
     * @param alongWorkDtl
     * @return void
     * @author kliu
     * @date 2022/6/14 16:56
     */
    void update(AlongWorkDtl alongWorkDtl);

    /**
     * 校验数据是否存在
     * @param alongWorkDtl
     * @return boolean
     * @author kliu
     * @date 2022/6/14 19:59
     */
    boolean checkExist(AlongWorkDtl alongWorkDtl);
    /**
     * 获取随工明细数据列表
     * @param id
     * @return java.util.List
     * @author kliu
     * @date 2022/6/30 14:11
     */
    List list(long id);
    /**
     * 获取随工明细数据
     * @param id
     * @param pointName
     * @return com.inspur.industrialinspection.entity.AlongWorkDtl
     * @author kliu
     * @date 2022/7/9 9:32
     */
    AlongWorkDtl getDetlById(long id, String pointName);
}
