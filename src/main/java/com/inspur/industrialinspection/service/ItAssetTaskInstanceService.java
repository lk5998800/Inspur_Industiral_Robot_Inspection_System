package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import com.inspur.page.PageBean;

/**
 * @author: kliu
 * @description: 资产盘点任务执行实例
 * @date: 2022/7/27 17:04
 */
public interface ItAssetTaskInstanceService {
    /**
     * 获取任务实例
     * @param roomId
     * @param robotId
     * @param taskName
     * @param cabinetRow
     * @param cabinetColumn
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/9/14 11:24
     */
    PageBean list(long roomId, long robotId, String taskName, String cabinetRow, long cabinetColumn, int pageSize, int pageNum);
    /**
     * 终止任务
     * @param itAssetTaskInstance
     * @return void
     * @author kliu
     * @date 2022/6/14 16:38
     */
    void terminate(ItAssetTaskInstance itAssetTaskInstance);
    /**
     * 接收任务终止结果
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/14 16:38
     */
    void receiveTerminateResult(String json);
    /**
     * 机器人结束任务
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/14 16:39
     */
    void industrialRobotEndTask(String json);

    /**
     * 获取资产盘点任务实例明细
     * @param cabinetRow
     * @param cabinetColumn
     * @param assetNo
     * @param assetName
     * @param personInChargeId
     * @param instanceId
     * @param dataType
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/10/1 14:40
     */
    PageBean instanceDetlList(String cabinetRow, long cabinetColumn, String assetNo, String assetName, long personInChargeId, long instanceId, String dataType, int pageSize, int pageNum);

    /**
     * 批量删除
     * @param jsonArray
     * @return void
     * @author kliu
     * @date 2022/9/13 11:53
     */
    void batchDelete(JSONArray jsonArray);
}
