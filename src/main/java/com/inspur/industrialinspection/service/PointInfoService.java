package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.PointInfo;

import java.io.IOException;
import java.util.List;

/**
 * 点位信息服务
 * @author kliu
 * @date 2022/5/25 8:55
 */
public interface PointInfoService {
    /**
     * 获取点位信息
     * @param roomId
     * @return java.util.List
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:54
     */
    List list(long roomId) throws IOException;
    /**
     * 关联位姿，校验机房id，获取实时位姿并保存
     * @param pointInfo
     * @return void
     * @throws InterruptedException
     * @author kliu
     * @date 2022/6/14 16:54
     */
    void associatedPosture(PointInfo pointInfo) throws InterruptedException;
    /**
     * 关联位姿，手动关联
     * @param pointInfo
     * @return void
     * @throws InterruptedException
     * @author kliu
     * @date 2022/6/14 16:54
     */
    void associatedPostureManual(PointInfo pointInfo) throws InterruptedException;
    /**
     * 关联待命点
     * @param roomId
     * @return void
     * @throws InterruptedException
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:54
     */
    void associatedWaitPoint(long roomId) throws InterruptedException, IOException;
    /**
     * 获取检测点位姿，通过mqtt下发指令，机器人manage端收到请求后，请求底盘获取位姿，将获取到的位姿数据通过http返回结果，用map临时存储
     * @param roomId
     * @return com.inspur.industrialinspection.entity.PointInfo
     * @throws InterruptedException
     * @author kliu
     * @date 2022/6/14 16:55
     */
    PointInfo getRealTimePosture(long roomId) throws InterruptedException;
    /**
     * 接收位姿
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/14 16:55
     */
    void receivePosture(String json);

    void receiveFireExtinguisherPara(String json);
}
