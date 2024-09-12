package com.inspur.industrialinspection.service;

import java.io.IOException;

/**
 * @author kliu
 * @description 实时视频服务
 * @date 2022/5/6 11:31
 */
public interface RealTimeVideoService {
    /**
     * 实时视频开始
     * @param roomId
     * @param robotId
     * @return java.lang.String
     * @throws IOException
     * @throws InterruptedException
     * @author kliu
     * @date 2022/6/14 16:53
     */
    String start(long roomId, long robotId) throws IOException, InterruptedException;
    /**
     * 实时视频心跳
     * @param roomId
     * @param robotId
     * @return void
     * @throws InterruptedException
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:53
     */
    void heart(long roomId, long robotId) throws IOException, InterruptedException;
    /**
     * 接收推流结果
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/14 16:54
     */
    void receiveStreamResult(String json);
}
