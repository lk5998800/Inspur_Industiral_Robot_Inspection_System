package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.DetectionCombination;
import com.inspur.industrialinspection.entity.DetectionInfo;

import java.io.IOException;
import java.util.List;

/**
 * 检测项组合service
 * @author kliu
 * @date 2022/4/28 8:30
 */
public interface DetectionCombinationService {
    /**
     * 获取检测项组合列表
     * @param roomId 机房id
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionCombination> 检测项组合列表
     * @author kliu
     * @date 2022/5/30 20:26
     */
    List<DetectionCombination> list(long roomId);
    /**
     * 添加检测项组合
     * @param detectionCombination
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    void add(DetectionCombination detectionCombination);
    /**
     * 更新检测项组合
     * @param detectionCombination
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    void update(DetectionCombination detectionCombination);
    /**
     * 添加检测项组合
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    void add(JSONObject jsonObject);
    /**
     * 更新检测项组合
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    void update(JSONObject jsonObject);
    /**
     * 删除检测项组合
     * @param detectionCombination
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    void delete(DetectionCombination detectionCombination);
    /**
     * 获取检测项信息 读取文件
     * @param roomId
     * @param pointName
     * @param inspectTypeId
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionInfo>
     * @author kliu
     * @date 2022/5/25 8:41
     */
    List<DetectionInfo> list(long roomId, String pointName, long inspectTypeId);
    /**
     * 获取检测项信息，不读取文件
     * @param roomParamObject
     * @param pointName
     * @param inspectTypeId
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionInfo>
     * @author kliu
     * @date 2022/5/25 8:42
     */
    List<DetectionInfo> list(JSONObject roomParamObject, String pointName, long inspectTypeId);
}
