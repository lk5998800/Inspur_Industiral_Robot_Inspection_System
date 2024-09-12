package com.inspur.industrialinspection.dao;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.PointInfo;

import java.util.List;

/**
 * 随工点
 * @author kliu
 * @date 2022/8/5 14:10
 */
public interface AlongWorkPointInfoDao {
    /**
     * 获取列表
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/8/5 14:10
     */
    List list(long roomId);
    /**
     * 删除
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/8/5 14:11
     */
    void delete(long roomId);
    /**
     * 添加
     * @param array
     * @return void
     * @author kliu
     * @date 2022/8/5 14:11
     */
    void add(JSONArray array);

    /**
     * 添加监测点位姿信息
     * @param pointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    void add(PointInfo pointInfo);
    /**
     * 更新监测点位姿信息
     * @param pointInfo
     * @author kliu
     * @date 2022/5/24 18:18
     */
    void update(PointInfo pointInfo);
    /**
     * 校验检测点位姿是否存在
     * @param pointInfo
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:18
     */
    boolean checkExist(PointInfo pointInfo);
}
