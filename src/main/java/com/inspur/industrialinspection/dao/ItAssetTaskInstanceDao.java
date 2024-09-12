package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * 资产盘点任务执行实例
 * @author kliu
 * @date 2022/7/27 17:05
 */
public interface ItAssetTaskInstanceDao {
    /**
     * 添加任务实例并返回id
     * @param itAssetTaskInstance
     * @return long
     * @author kliu
     * @date 2022/7/27 17:05
     */
    long addAndReturnId(ItAssetTaskInstance itAssetTaskInstance);
    /**
     * 更新任务执行实例
     * @param itAssetTaskInstance
     * @return void
     * @author kliu
     * @date 2022/7/27 17:05
     */
    void update(ItAssetTaskInstance itAssetTaskInstance);
    /**
     * 获取任务明细
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.ItAssetTaskInstance
     * @author kliu
     * @date 2022/7/27 17:05
     */
    ItAssetTaskInstance getDetlById(long instanceId);
    /**
     * 获取实例明细锁表
     * @param instanceId
     * @return com.inspur.industrialinspection.entity.TaskInstance
     * @author kliu
     * @date 2022/7/27 17:05
     */
    ItAssetTaskInstance getDetlByIdForUpdate(long instanceId);
    /**
     * 分页查询任务实例
     * @param roomId
     * @param robotId
     * @param taskName
     * @param cabinetRow
     * @param cabinetColumn
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/9/14 11:25
     */
    PageBean list(long roomId, long robotId, String taskName, String cabinetRow, long cabinetColumn, int pageSize, int pageNum);
    /**
     * 校验实例是否存在
     * @param instanceId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:10
     */
    boolean checkExist(long instanceId);

    /**
     * 获取未分析过的资产盘点任务
     * @return java.util.List<com.inspur.industrialinspection.entity.ItAssetTaskInstance>
     * @author kliu
     * @date 2022/8/2 15:17
     */
    List<ItAssetTaskInstance> unAnalyseList();

    /**
     * 获取资产盘点任务实例明细
     * @param cabinetRow
     * @param cabinetColumn
     * @param assetName
     * @param personInChargeId
     * @param instanceId
     * @param dataType
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/8/5 15:28
     */
    PageBean instanceDetlList(String cabinetRow, long cabinetColumn, String assetNo, String assetName, long personInChargeId, long instanceId, String dataType, int pageSize, int pageNum);

    /**
     * 批量删除
     * @param inPara
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void batchDelete(String inPara);

    /**
     * 依据机房id和日期获取次数
     * @param roomId
     * @param dateStr
     * @return int
     * @author kliu
     * @date 2022/10/29 11:49
     */
    int countByRoomIdAndDate(long roomId, String dateStr);


    /**
     * 依据机房id和日期获取列表，大于日期到当日
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.ItAssetTaskInstance>
     * @author kliu
     * @date 2022/10/31 8:46
     */
    List<ItAssetTaskInstance> listByRoomIdAndDate(long roomId, String dateStr);
}
