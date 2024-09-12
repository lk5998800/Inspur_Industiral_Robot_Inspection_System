package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ItAssetTaskResult;

/**
 * 资产盘点任务结果
 * @author kliu
 * @date 2022/7/29 8:36
 */
public interface ItAssetTaskResultDao {

    /**
     * 判断数据是否存在
     * @param itAssetTaskResult
     * @return boolean
     * @author kliu
     * @date 2022/7/29 8:37
     */
    boolean checkExist(ItAssetTaskResult itAssetTaskResult);

    /**
     * 添加
     * @param itAssetTaskResult
     * @return void
     * @author kliu
     * @date 2022/7/29 8:38
     */
    void add(ItAssetTaskResult itAssetTaskResult);
}
