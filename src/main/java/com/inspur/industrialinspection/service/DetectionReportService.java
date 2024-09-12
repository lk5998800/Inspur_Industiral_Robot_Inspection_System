package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检测报告服务
 * @author kliu
 * @date 2022/5/25 8:47
 */
public interface DetectionReportService {
    /**
     * 获取当天检测报告任务概览
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/5/25 8:47
     */
    HashMap getTodayReportOverview(long roomId);
    /**
     * 获取当日报告巡检报警类型
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getTodayReportWarnType(long roomId);
    /**
     * 获取近7日报告巡检报警类型
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent7DaysReportWarnType(long roomId);
    /**
     * 获取近30日报告巡检报警类型
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent30DaysReportWarnType(long roomId);
    /**
     * 获取当日报告巡检机柜状态统计
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getTodayReportCabinetStatus(long roomId);
    /**
     * 获取近7日报告巡检机柜状态统计
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent7DaysReportCabinetStatus(long roomId);
    /**
     * 获取近30日报告巡检机柜状态统计
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent30DaysReportCabinetStatus(long roomId);

    /**
     * 获取当日报告任务类型统计
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getTodayReportTaskType(long roomId);
    /**
     * 获取近7日报告任务类型统计
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent7DaysReportTaskType(long roomId);
    /**
     * 获取近30日报告任务类型统计
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent30DaysReportTaskType(long roomId);

    /**
     * 获取当日报告工作时长
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getTodayReportWorkTime(long roomId);
    /**
     * 获取近7日报告工作时长
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent7DaysReportWorkTime(long roomId);
    /**
     * 获取近30日报告工作时长
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 9:55
     */
    JSONArray getRecent30DaysReportWorkTime(long roomId);
    /**
     * 获取近7天检测报告任务概览
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/25 17:31
     */
    HashMap getRecent7DaysReportOverview(long roomId);
    /**
     * 获取近30天检测报告任务概览
     * @param roomId
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/25 17:31
     */
    HashMap getRecent30DaysReportOverview(long roomId);
    /**
     * 获取当天报告检测项概览
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/5/25 8:47
     */
    List getTodayReportDetectionOverview(long roomId);
    /**
     * 获取近7天报告检测项概览
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/25 17:31
     */
    List getRecent7DayReportDetectionOverview(long roomId);
    /**
     * 获取近30天报告检测项概览
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/25 17:31
     */
    List getRecent30DayReportDetectionOverview(long roomId);
    /**
     * 获取当天检测项明细信息
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/5/25 8:48
     */
    Map getTodayDetectionDetlInfo(long roomId);
    /**
     * 获取当日检测项明细汇总数据
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/5 14:30
     */
    JSONObject getTodayDetectionDetlInfoWithSum(long roomId, int pageSize, int pageNum);
    /**
     * 获取近7日检测项明细汇总数据
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/5 14:30
     */
    JSONObject getRecent7DaysDetectionDetlInfoWithSum(long roomId, int pageSize, int pageNum);
    /**
     * 获取近30日检测项明细汇总数据
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/5 14:31
     */
    JSONObject getRecent30DaysDetectionDetlInfoWithSum(long roomId, int pageSize, int pageNum);

    /**
     * 获取最近一次检测项明细信息
     * @param roomId
     * @return java.util.Map
     * @author kliu
     * @date 2022/6/27 14:33
     */
    Map getRecentDetectionDetlInfo(long roomId);

    /**
     * 获取当日报告图片告警数据
     * @param roomId
     * @param pointName
     * @param aiType
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/4 11:12
     */
    JSONArray getTodayDetectionAiPicture(long roomId, String pointName, String aiType);
    /**
     * 获取近7日报告图片告警数据
     * @param roomId
     * @param pointName
     * @param aiType
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/4 11:12
     */
    JSONArray getRecent7DaysDetectionAiPicture(long roomId, String pointName, String aiType);
    /**
     * 获取近30日报告图片告警数据
     * @param roomId
     * @param pointName
     * @param aiType
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/4 11:12
     */
    JSONArray getRecent30DaysDetectionAiPicture(long roomId, String pointName, String aiType);

    /**
     * 当天人员行为汇总
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/5 14:14
     */
    JSONObject getTodayPersonBehaviorWithSum(long roomId, int pageSize, int pageNum);
    /**
     * 近7日人员行为汇总
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/5 14:14
     */
    JSONObject getRecent7DaysPersonBehaviorWithSum(long roomId, int pageSize, int pageNum);

    /**
     * 近30日人员行为汇总
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/5 14:14
     */
    JSONObject getRecent30DaysPersonBehaviorWithSum(long roomId, int pageSize, int pageNum);
    /**
     * 获取机柜U位统计
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/5 14:14
     */
    JSONObject getRecentCabinetUBitList(long roomId, int pageSize, int pageNum);
}
