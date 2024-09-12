package com.inspur.industrialinspection.service;

/**
 * 任务执行检测项结果服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface TaskDetectionResultService {
    /**
     * 添加检测项结果
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/7 16:29
     */
    void add(String json);
}
