package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.service.DetectionReportService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检测报告
 * @author kliu
 * @date 2022/6/7 16:08
 */
@Controller
@RequestMapping(path="/industrial_robot/detcetionreport")
@Api(tags = "检测报告")
@Slf4j
public class DetectionReportController {

    @Autowired
    private DetectionReportService detectionReportService;

    @GetMapping("/getTodayReportOverview")
    @ResponseBody
    @ApiOperation(value="获取当日报告概览信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayReportOverview(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        HashMap hashMap = detectionReportService.getTodayReportOverview(roomId);
        return Result.success(hashMap);
    }

    @GetMapping("/getTodayReportWarnType")
    @ResponseBody
    @ApiOperation(value="获取当日报告巡检报警类型占比")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayReportWarnType(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getTodayReportWarnType(roomId));
    }
    @GetMapping("/getRecent7DaysReportWarnType")
    @ResponseBody
    @ApiOperation(value="获取近7日报告巡检报警类型占比")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysReportWarnType(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent7DaysReportWarnType(roomId));
    }
    @GetMapping("/getRecent30DaysReportWarnType")
    @ResponseBody
    @ApiOperation(value="获取近30日报告巡检报警类型占比")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysReportWarnType(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent30DaysReportWarnType(roomId));
    }

    @GetMapping("/getTodayReportCabinetStatus")
    @ResponseBody
    @ApiOperation(value="获取当日报告巡检机柜状态统计")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayReportCabinetStatus(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getTodayReportCabinetStatus(roomId));
    }
    @GetMapping("/getRecent7DaysReportCabinetStatus")
    @ResponseBody
    @ApiOperation(value="获取近7日报告巡检机柜状态统计")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysReportCabinetStatus(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent7DaysReportCabinetStatus(roomId));
    }
    @GetMapping("/getRecent30DaysReportCabinetStatus")
    @ResponseBody
    @ApiOperation(value="获取近30日报告巡检机柜状态统计")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysReportCabinetStatus(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent30DaysReportCabinetStatus(roomId));
    }

    @GetMapping("/getTodayReportTaskType")
    @ResponseBody
    @ApiOperation(value="获取当日报告任务类型统计")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayReportTaskType(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getTodayReportTaskType(roomId));
    }
    @GetMapping("/getRecent7DaysReportTaskType")
    @ResponseBody
    @ApiOperation(value="获取近7日报告任务类型统计")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysReportTaskType(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent7DaysReportTaskType(roomId));
    }
    @GetMapping("/getRecent30DaysReportTaskType")
    @ResponseBody
    @ApiOperation(value="获取近30日报告任务类型统计")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysReportTaskType(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent30DaysReportTaskType(roomId));
    }

    @GetMapping("/getTodayReportWorkTime")
    @ResponseBody
    @ApiOperation(value="获取当日报告工作时长")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayReportWorkTime(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getTodayReportWorkTime(roomId));
    }
    @GetMapping("/getRecent7DaysReportWorkTime")
    @ResponseBody
    @ApiOperation(value="获取近7日报告工作时长")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysReportWorkTime(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent7DaysReportWorkTime(roomId));
    }
    @GetMapping("/getRecent30DaysReportWorkTime")
    @ResponseBody
    @ApiOperation(value="获取近30日报告工作时长")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysReportWorkTime(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionReportService.getRecent30DaysReportWorkTime(roomId));
    }

    @Deprecated
    @GetMapping("/getTodayReportDetectionOverview")
    @ResponseBody
    @ApiOperation(value="获取当日检测项概览信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayReportDetectionOverview(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        List list = detectionReportService.getTodayReportDetectionOverview(roomId);
        return Result.success(list);
    }

    @Deprecated
    @GetMapping("/getTodayDetectionDetlInfo")
    @ResponseBody
    @ApiOperation(value="获取当日检测项明细信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayDetectionDetlInfo(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        Map map = detectionReportService.getTodayDetectionDetlInfo(roomId);
        return Result.success(map);
    }

    @GetMapping("/getTodayDetectionDetlInfoWithSum")
    @ResponseBody
    @ApiOperation(value="获取巡检项明细汇总数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayDetectionDetlInfoWithSum(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                   @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                                   @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        Map map = detectionReportService.getTodayDetectionDetlInfoWithSum(roomId, pageSize, pageNum);
        return Result.success(map);
    }
    @GetMapping("/getRecent7DaysDetectionDetlInfoWithSum")
    @ResponseBody
    @ApiOperation(value="获取巡检项明细汇总数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysDetectionDetlInfoWithSum(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                         @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                                         @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        Map map = detectionReportService.getRecent7DaysDetectionDetlInfoWithSum(roomId, pageSize, pageNum);
        return Result.success(map);
    }
    @GetMapping("/getRecent30DaysDetectionDetlInfoWithSum")
    @ResponseBody
    @ApiOperation(value="获取巡检项明细汇总数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysDetectionDetlInfoWithSum(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                          @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                                          @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        Map map = detectionReportService.getRecent30DaysDetectionDetlInfoWithSum(roomId, pageSize, pageNum);
        return Result.success(map);
    }

    @GetMapping("/getTodayDetectionAiPicture")
    @ResponseBody
    @ApiOperation(value="获取巡检项明细ai图片数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayDetectionAiPicture(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                             @ApiParam(value = "点位名称", required = true) @RequestParam("pointName") String pointName,
                                             @ApiParam(value = "告警类型", required = true) @RequestParam("aiType") String aiType) {
        JSONArray array = detectionReportService.getTodayDetectionAiPicture(roomId, pointName, aiType);
        return Result.success(array);
    }
    @GetMapping("/getRecent7DaysDetectionAiPicture")
    @ResponseBody
    @ApiOperation(value="获取巡检项明细ai图片数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysDetectionAiPicture(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                   @ApiParam(value = "点位名称", required = true) @RequestParam("pointName") String pointName,
                                                   @ApiParam(value = "告警类型", required = true) @RequestParam("aiType") String aiType) {
        JSONArray array = detectionReportService.getRecent7DaysDetectionAiPicture(roomId, pointName, aiType);
        return Result.success(array);
    }
    @GetMapping("/getRecent30DaysDetectionAiPicture")
    @ResponseBody
    @ApiOperation(value="获取巡检项明细ai图片数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysDetectionAiPicture(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                    @ApiParam(value = "点位名称", required = true) @RequestParam("pointName") String pointName,
                                                    @ApiParam(value = "告警类型", required = true) @RequestParam("aiType") String aiType) {
        JSONArray array = detectionReportService.getRecent30DaysDetectionAiPicture(roomId, pointName, aiType);
        return Result.success(array);
    }
    @GetMapping("/getRecent7DaysReportOverview")
    @ResponseBody
    @ApiOperation(value="获取近7日报告概览信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysReportOverview(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        HashMap hashMap = detectionReportService.getRecent7DaysReportOverview(roomId);
        return Result.success(hashMap);
    }

    @Deprecated
    @GetMapping("/getRecent7DayReportDetectionOverview")
    @ResponseBody
    @ApiOperation(value="获取近7日检测项概览信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DayReportDetectionOverview(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        List list = detectionReportService.getRecent7DayReportDetectionOverview(roomId);
        return Result.success(list);
    }

    @GetMapping("/getRecent30DaysReportOverview")
    @ResponseBody
    @ApiOperation(value="获取近一月报告概览信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysReportOverview(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        HashMap hashMap = detectionReportService.getRecent30DaysReportOverview(roomId);
        return Result.success(hashMap);
    }

    @Deprecated
    @GetMapping("/getRecent30DayReportDetectionOverview")
    @ResponseBody
    @ApiOperation(value="获取近一月检测项概览信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DayReportDetectionOverview(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        List list = detectionReportService.getRecent30DayReportDetectionOverview(roomId);
        return Result.success(list);
    }

    @Deprecated
    @GetMapping("/getRecentDetectionDetlInfo")
    @ResponseBody
    @ApiOperation(value="获取最近一次检测项明细信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecentDetectionDetlInfo(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        Map map = detectionReportService.getRecentDetectionDetlInfo(roomId);
        return Result.success(map);
    }

    @GetMapping("/getTodayPersonBehaviorWithSum")
    @ResponseBody
    @ApiOperation(value="获取人员行为汇总数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTodayPersonBehaviorWithSum(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                                @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        JSONObject jsonObject = detectionReportService.getTodayPersonBehaviorWithSum(roomId, pageSize, pageNum);
        return Result.success(jsonObject);
    }
    @GetMapping("/getRecent7DaysPersonBehaviorWithSum")
    @ResponseBody
    @ApiOperation(value="获取近7日人员行为汇总数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent7DaysPersonBehaviorWithSum(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                      @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                                      @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        JSONObject jsonObject = detectionReportService.getRecent7DaysPersonBehaviorWithSum(roomId, pageSize, pageNum);
        return Result.success(jsonObject);
    }
    @GetMapping("/getRecent30DaysPersonBehaviorWithSum")
    @ResponseBody
    @ApiOperation(value="获取近30日人员行为汇总数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecent30DaysPersonBehaviorWithSum(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        JSONObject jsonObject = detectionReportService.getRecent30DaysPersonBehaviorWithSum(roomId, pageSize, pageNum);
        return Result.success(jsonObject);
    }
    @GetMapping("/getRecentCabinetUBitList")
    @ResponseBody
    @ApiOperation(value="获取机柜u位统计")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecentCabinetUBitList(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                                                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        JSONObject jsonObject = detectionReportService.getRecentCabinetUBitList(roomId, pageSize, pageNum);
        return Result.success(jsonObject);
    }
}
