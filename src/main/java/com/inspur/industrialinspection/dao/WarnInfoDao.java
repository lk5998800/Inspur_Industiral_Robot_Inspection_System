package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.WarnInfo;
import com.inspur.industrialinspection.entity.vo.WarnInfoCountVo;

import java.util.List;

/**
 * @author kliu
 * @description 告警信息
 * @date 2022/4/18 20:25
 */
public interface WarnInfoDao {
    /**
     * 添加告警信息
     *
     * @param warnInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:15
     */
    void add(WarnInfo warnInfo);

    /**
     * 更新告警信息
     *
     * @param warnInfo
     * @return void
     * @author kliu
     * @date 2022/5/24 20:15
     */
    void update(WarnInfo warnInfo);

    /**
     * 校验告警信息是否存在
     *
     * @param warnInfo
     * @return boolean
     * @author kliu
     * @date 2022/5/24 20:15
     */
    boolean checkExist(WarnInfo warnInfo);

    /**
     * 获取实例下的告警信息
     *
     * @param instanceId
     * @return java.util.List<com.inspur.industrialinspection.entity.WarnInfo>
     * @author kliu
     * @date 2022/5/24 20:16
     */
    List<WarnInfo> listByInstanceId(long instanceId);

    /**
     * 获取某个日期之后的所有告警信息
     *
     * @param dateStr
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.WarnInfo>
     * @author kliu
     * @date 2022/5/24 20:16
     */
    List<WarnInfo> listByDate(long roomId, String dateStr);


    /**
     * 根据instanceId获取报警种类数量
     *
     * @param instanceId
     * @return
     */
    List<WarnInfoCountVo> getCountWarnInfo(Long instanceId);

    /**
     * 获取异常巡检柜次
     * @param roomId
     * @param startDate
     * @return java.util.List
     * @author kliu
     * @date 2022/10/29 11:27
     */
    List abnormalCabinetcountByRoomIdAndDate(long roomId, String startDate);
}
