package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * 资产任务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface ItAssetTaskInfoService {
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
     * 保存资产盘点结果数据
     * @param json
     * @return void
     * @author kliu
     * @date 2022/7/28 20:03
     */
    void receiveItAssetResult(String json);

    /**
     * 批量删除
     * @param jsonArray
     * @return void
     * @author kliu
     * @date 2022/9/13 11:53
     */
    void batchDelete(JSONArray jsonArray);
}
