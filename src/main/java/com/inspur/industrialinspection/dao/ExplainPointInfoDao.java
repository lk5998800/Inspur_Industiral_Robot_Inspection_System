package com.inspur.industrialinspection.dao;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.ExplainPointInfo;
import com.inspur.industrialinspection.entity.PointInfo;

import java.util.List;

/**
 * @author: LiTan
 * @description:    讲解点
 * @date:   2022-11-01 09:58:32
 */
public interface ExplainPointInfoDao {

    /**
     * 获取列表
     * @param roomId
     * @return
     */
    List<ExplainPointInfo> list(long roomId);

    /**
     * 删除
     * @param roomId
     */
    void delete(long roomId);

    /**
     * 添加
     * @param array
     */
    void add(JSONArray array);

    /**
     * 添加监测点位姿信息
     * @param pointInfo
     */
    void add(PointInfo pointInfo);

    /**
     * 更新监测点位姿信息
     * @param pointInfo
     */
    void update(PointInfo pointInfo);

    /**
     * 校验检测点位姿是否存在
     * @param pointInfo
     * @return
     */
    boolean checkExist(PointInfo pointInfo);


    /**
     * 查询单个点位信息
     * @param roomId
     * @param point
     * @return
     */
    ExplainPointInfo getPointInfo(long roomId, String point);
}
