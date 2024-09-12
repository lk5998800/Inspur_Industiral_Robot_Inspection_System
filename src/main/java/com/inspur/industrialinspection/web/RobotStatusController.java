package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.RobotStatusService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 机器人状态上传接口
 *
 * @author kliu
 * @date 2022/5/27 9:00
 */
@Controller
@RequestMapping(path = "/industrial_robot/robotstatus")
@Api(tags = {"机器人"})
@Slf4j
public class RobotStatusController {

    @Autowired
    private RobotStatusService robotStatusService;

    @PostMapping("/receiveRobotStatus")
    @ResponseBody
    @ApiOperation(value = "接收机器人状态信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public Result receiveRobotStatus(@ApiParam(value = "机器人状态信息", required = true) @RequestParam("robot_status_json") String robot_status_json) {
        robotStatusService.receiveRobotStatus(robot_status_json);
        return Result.success();
    }

    @GetMapping("/getRobotStatus")
    @ResponseBody
    @ApiOperation(value = "获取机器人状态信息-手机监控")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRobotStatus(@ApiParam(value = "机器人id") @RequestParam(name = "robotId") long robotId) throws Exception {
        return Result.success(robotStatusService.getRobotStatusWithTask(robotId));
    }

    @GetMapping("/getRobotServiceUrl")
    @ResponseBody
    @ApiOperation(value = "获取机器人服务地址-手机监控")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRobotServiceUrl() {
        return Result.success(robotStatusService.getRobotServiceUrl());
    }

    @GetMapping("/getRobotPowerChangeLine")
    @ResponseBody
    @ApiOperation(value = "获取机器人电量变化曲线")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRobotPowerChangeLine(@ApiParam(value = "机器人id") @RequestParam(name = "robotId") long robotId) throws Exception {
        return Result.success(robotStatusService.getRobotPowerChangeLine(robotId));
    }

    @GetMapping("/getRobotPower")
    @ResponseBody
    @ApiOperation(value = "获取机器人电量")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRobotPower(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                                @ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId) {
        return Result.success(robotStatusService.getRobotPower(roomId, robotId));
    }

    @GetMapping("/getRecentPicture")
    @ResponseBody
    @ApiOperation(value = "获取机器人最近拍摄的一张照片")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRecentPicture(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId) {
        return Result.success(robotStatusService.getRecentPicture(robotId));
    }

    @GetMapping("/pileReturnFailure")
    @ResponseBody
    @ApiOperation(value = "接受机器人回桩异常状态")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result pileReturnFailure(@RequestParam("robotId") long robotId, @RequestParam("message") String message) throws Exception {
        robotStatusService.pileReturnFailure(robotId,message);
        return Result.success();
    }
}
