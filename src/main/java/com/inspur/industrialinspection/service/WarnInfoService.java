package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.WarnInfo;
import com.inspur.industrialinspection.entity.vo.TopInformationVo;
import com.inspur.industrialinspection.entity.vo.WarnInfoCountVo;
import com.inspur.industrialinspection.entity.vo.WarnMessageResultVo;

import java.util.HashMap;
import java.util.List;


/**
 * @author: LiTan
 * @description:    报警信息服务
 * @date:   2022-10-26 09:45:10
 */
public interface WarnInfoService {

    /**
     * 根据room_id获取报警信息
     * @param roomId
     * @return
     */
    List<WarnMessageResultVo> getWarnInfo(long roomId);

    /**
     * 根据room_id获取报警信息分类数量
     * @param roomId
     * @return
     */
    List<WarnInfoCountVo> getCountWarnInfo(long roomId);

    /**
     * 根据room_id获取报警信息种类占比
     * @param roomId
     * @return
     */
    List getWarnInfoProportion(long roomId);

    /**
     * 获取监控中心置顶信息
     * @param roomId
     * @return
     */
    TopInformationVo getTopInformation(long roomId);
}
