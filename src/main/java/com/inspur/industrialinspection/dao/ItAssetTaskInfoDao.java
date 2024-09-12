package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * 资产任务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface ItAssetTaskInfoDao {
    /**
     * 获取列表
     * @param itAssetTaskInfo
     * @param pageNum
     * @param pageSize
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/8/29 10:23
     */
    PageBean list(ItAssetTaskInfo itAssetTaskInfo, int pageNum, int pageSize);
    /**
     * 添加
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    void add(ItAssetTaskInfo itAssetTaskInfo);
    /**
     * 更新
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    void update(ItAssetTaskInfo itAssetTaskInfo);
    /**
     * 删除
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    void delete(ItAssetTaskInfo itAssetTaskInfo);

    /**
     * 获取明细
     * @param id
     * @return com.inspur.industrialinspection.entity.ItAssetTaskInfo
     * @author kliu
     * @date 2022/7/25 17:55
     */
    ItAssetTaskInfo getDetlById(long id);

    /**
     * 获取需要比对的资产盘点任务
     * @return java.util.List
     * @author kliu
     * @date 2022/7/26 18:18
     */
    List pendingExecutionTask();

    /**
     * 批量删除
     * @param inPara
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void batchDelete(String inPara);
}
