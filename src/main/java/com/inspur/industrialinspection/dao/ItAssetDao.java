package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ItAsset;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * @author kliu
 * @description 资产信息
 * @date 2022/4/18 20:25
 */
public interface ItAssetDao {
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
     * 根据资产编号判断数据是否存在
     * @param assetNo
     * @return boolean
     * @author kliu
     * @date 2022/7/30 9:04
     */
    boolean checkExistByAssetNo(String assetNo);

    /**
     * 获取明细
     * @param id
     * @return com.inspur.industrialinspection.entity.ItAsset
     * @author kliu
     * @date 2022/7/30 9:14
     */
    ItAsset getDetlById(long id);

    /**
     * 获取明细
     * @param assetNo
     * @return com.inspur.industrialinspection.entity.ItAsset
     * @author kliu
     * @date 2022/7/30 9:14
     */
    ItAsset getDetlByAssetNo(String assetNo);

    /**
     * 依据机房id获取总数
     * @param roomId
     * @return int
     * @author kliu
     * @date 2022/10/28 20:14
     */
    int countByRoomId(long roomId);
}
