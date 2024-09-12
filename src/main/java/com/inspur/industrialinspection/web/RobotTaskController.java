package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.RobotTaskService;
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
/**
 * 机器人任务信息
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/robottask")
@Api(tags = "机房总览")
@Slf4j
public class RobotTaskController {

    @Autowired
    private RobotTaskService robotTaskService;

    @GetMapping("/getRecentTaskBasic")
    @ResponseBody
    @ApiOperation(value="获取机器人最新一次任务基本信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecentTaskBasic(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws Exception {
        HashMap hashMap = robotTaskService.getRecentTaskBasic(roomId);
        return Result.success(hashMap);
    }

    @GetMapping("/getAbnormalCountRecentDays7")
    @ResponseBody
    @ApiOperation(value="获取近7天任务运行异常信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getAbnormalCountRecentDays7(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws Exception {
        HashMap hashMap = robotTaskService.getAbnormalCountRecentDays7(roomId);
        return Result.success(hashMap);
    }

    @GetMapping("/getRecentTaskCabinetsInfo")
    @ResponseBody
    @ApiOperation(value="获取最近一次任务机柜信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecentTaskCabinetsInfo(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws Exception {
        HashMap hashMap = robotTaskService.getRecentTaskCabinetsInfo(roomId);
        return Result.success(hashMap);
    }

    @GetMapping("/getRecentTaskWarnInfo")
    @ResponseBody
    @ApiOperation(value="获取最近一次任务运行预警信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecentTaskWarnInfo(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws Exception {
        HashMap hashMap = robotTaskService.getRecentTaskWarnInfo(roomId);
        return Result.success(hashMap);
    }

    @GetMapping("/getRobotRunStatusInfo")
    @ResponseBody
    @ApiOperation(value="获取机器人运行状态信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRobotRunStatusInfo(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws Exception {
        HashMap hashMap = robotTaskService.getRobotRunStatusInfo(roomId);
        return Result.success(hashMap);
    }
}
