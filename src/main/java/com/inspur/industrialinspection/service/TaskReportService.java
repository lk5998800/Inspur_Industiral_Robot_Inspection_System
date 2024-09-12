package com.inspur.industrialinspection.service;

import java.util.HashMap;
import java.util.List;
/**
 * 任务报告服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface TaskReportService {
    /**
     * 获取任务报告概览
     * @param instanceId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/14 16:37
     */
    HashMap getTaskReportOverview(long instanceId);
    /**
     * 获取任务报告检测项概览
     * @param instanceId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/14 16:37
     */
    List getTaskReportDetectionOverview(long instanceId);
    /**
     * 获取任务检测项明细
     * @param instanceId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/14 16:37
     */
    List getTaskDetectionDetlInfo(long instanceId);
    /**
     * 获取任务报告
     * @param instanceId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/14 16:38
     */
    HashMap getTaskReport(long instanceId);
}
