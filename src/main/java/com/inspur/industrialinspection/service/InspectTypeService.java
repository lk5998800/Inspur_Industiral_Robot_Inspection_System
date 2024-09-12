package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.InspectType;

import java.io.IOException;
import java.util.List;
/**
 * 巡检类型服务
 * @author kliu
 * @date 2022/5/25 8:52
 */
public interface InspectTypeService {
    /**
     * 获取巡检类型列表
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.InspectType>
     * @author kliu
     * @date 2022/5/25 8:49
     */
    List<InspectType> list(long roomId);
    /**
     * 添加巡检类型
     * @param inspectType
     * @return void
     * @author kliu
     * @date 2022/5/25 8:49
     */
    void add(InspectType inspectType);
    /**
     * 更新巡检类型
     * @param inspectType
     * @return void
     * @author kliu
     * @date 2022/5/25 8:50
     */
    void update(InspectType inspectType);
    /**
     * 删除巡检类型
     * @param inspectType
     * @return void
     * @author kliu
     * @date 2022/5/25 8:50
     */
    void delete(InspectType inspectType);
    /**
     * 校验巡检类型是否存在
     * @param roomId
     * @param inspectTypeId
     * @return boolean
     * @author kliu
     * @date 2022/5/25 8:50
     */
    boolean checkIsExist(long roomId, long inspectTypeId);
    /**
     * 获取巡检类型明细 读取文件
     * @param roomId
     * @param inspectTypeId
     * @return com.inspur.industrialinspection.entity.InspectType
     * @author kliu
     * @date 2022/5/25 8:50
     */
    InspectType getDetlById(long roomId, long inspectTypeId);
    /**
     * 获取巡检类型明细不读取文件
     * @param roomParamObject
     * @param inspectTypeId
     * @return com.inspur.industrialinspection.entity.InspectType
     * @author kliu
     * @date 2022/5/25 8:50
     */
    InspectType getDetlById(JSONObject roomParamObject, long inspectTypeId);
}
