package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.GatingPara;

import java.util.List;

/**
 * 门控参数dao
 * @author kliu
 * @date 2022/6/23 19:12
 */
public interface GatingParaDao {
    /**
     * 获取门控参数明细
     * @param roomId
     * @param pointName
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/6/23 19:13
     */
    GatingPara getDetlById(long roomId, String pointName);
    /**
     * 添加门控参数
     * @param gatingPara
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/7/11 15:43
     */
    void add(GatingPara gatingPara);

    /**
     * 更新门控参数
     * @param gatingPara
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/7/11 15:43
     */
    void update(GatingPara gatingPara);

    /**
     * 校验数据是否存在
     * @param gatingPara
     * @return boolean
     * @author kliu
     * @date 2022/7/11 15:46
     */
    boolean checkExist(GatingPara gatingPara);

    /**
     * 依据门控id获取详细信息
     * @param doorCode
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/10/12 11:09
     */
    GatingPara getDetlByDoorCode(String doorCode);
    /**
     * 根据单个点位获取所有门控参数
     * @param roomId
     * @param pointName
     * @return List<GatingPara>
     * @author ldh
     * @date 2022/10/25
     */
    List<GatingPara> getDetlsById(long roomId, String pointName);
}
