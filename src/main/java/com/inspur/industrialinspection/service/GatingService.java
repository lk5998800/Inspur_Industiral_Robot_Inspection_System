package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.GatingPara;

import java.util.List;

/**
 * 门控 Service
 * @author kliu
 * @date 2022/5/9 17:26
 */
public interface GatingService {
    /**
     * 门控service方法
     * @param jsonObject
     * @return void
     * @throws InterruptedException
     * @author kliu
     * @date 2022/7/9 9:32
     */
    void invokeGating(JSONObject jsonObject) throws Exception;
    /**
     * 接收mqtt返回数据
     * @param str
     * @return void
     * @throws InterruptedException
     * @author kliu
     * @date 2022/7/13 14:24
     */
    void receiveMqttBack(String str) throws InterruptedException;

    /**
     * 获取随工参数详情
     * @param roomId
     * @param pointName
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/7/13 9:58
     */
    GatingPara getDetlById(long roomId, String pointName);

    /**
     * 添加门控参数
     * @param gatingPara
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/7/11 15:43
     */
    void addOrUpdate(GatingPara gatingPara);

    /**
     * 根据单个点位获取所有门控参数
     * @param roomId
     * @param pointName
     * @return List<com.inspur.industrialinspection.entity.GatingPara>
     * @author ldh
     * @date 2022/10/25
     */
    List<GatingPara> getDetlsById(long roomId, String pointName);
    /**
     * 修改多个门控参数
     * @param gatingPara
     * @return null
     * @author ldh
     * @date 2022/10/25
     */
    void addOrUpdateList(List<GatingPara> gatingPara);

    /**
     * 开门-公司门控
     * @param doorCode
     * @return void
     * @author kliu
     * @date 2022/11/14 8:58
     */
    void invokeOpenDoor(int doorCode);
    /**
     * 关门-公司门控
     * @param doorCode
     * @return void
     * @author kliu
     * @date 2022/11/14 8:58
     */
    void invokeCloseDoor(int doorCode);
}
