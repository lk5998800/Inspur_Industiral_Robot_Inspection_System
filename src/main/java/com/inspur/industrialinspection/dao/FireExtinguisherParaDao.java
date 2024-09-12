package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.FireExtinguisherPara;

/**
 * 灭火器检测参数业务层
 *
 * @author kliu
 * @since 2022-11-25 11:23:54
 */
public interface FireExtinguisherParaDao {

    /**
     * 灭火器检测参数添加
     *
     * @author kliu
     * @since 2022-11-25 11:23:54
     */
    void add(FireExtinguisherPara fireExtinguisherPara);
    /**
     * 灭火器检测参数修改
     *
     * @author kliu
     * @since 2022-11-25 11:23:54
     */
    void update(FireExtinguisherPara fireExtinguisherPara);
    /**
     * 灭火器检测参数查询明细
     *
     * @author kliu
     * @since 2022-11-25 11:23:54
     */
    FireExtinguisherPara findById(Long roomId, String pointName);

    boolean checkExist(Long roomId, String pointName);
}

