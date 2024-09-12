package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.ItAsset;
import com.inspur.page.PageBean;

/**
 * 资产
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface ItAssetService {
    /**
     * 获取列表
     * @param itAsset
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/8/29 10:23
     */
    PageBean list(ItAsset itAsset, int pageSize, int pageNum);
    /**
     * 添加
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    void add(ItAsset itAsset);
    /**
     * 更新
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    void update(ItAsset itAsset);
    /**
     * 删除
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    void delete(ItAsset itAsset);

    /**
     * 获取资产盘点code信息
     * @param
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/7 11:16
     */
    JSONArray getItAssetCode();
}
