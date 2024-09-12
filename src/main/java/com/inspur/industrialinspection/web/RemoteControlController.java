package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.RemoteControlService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 楼栋信息
 *
 * @author kliu
 * @date 2022/6/17 19:15
 */
@Controller
@RequestMapping(path = "/industrial_robot/remotecontrol")
@Api(tags = {"远程控制"})
@Slf4j
public class RemoteControlController {

    @Autowired
    private RemoteControlService remoteControlService;

    @Deprecated
    @GetMapping("/move")
    @ResponseBody
    @ApiOperation(value = "移动", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result move(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                       @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                       @ApiParam(value = "v", required = true) @RequestParam("v") double v,
                       @ApiParam(value = "w", required = true) @RequestParam("w") double w) {
        remoteControlService.move(robotId, roomId, v, w);
        return Result.success();
    }

    @GetMapping("/reboot")
    @ResponseBody
    @ApiOperation(value = "机器人重启", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result reboot(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                         @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        remoteControlService.reboot(robotId, roomId);
        return Result.success();
    }

    @GetMapping("/rebootCan")
    @ResponseBody
    @ApiOperation(value = "机器人重启-断电式", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result rebootCan(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId) {
        remoteControlService.rebootCan(robotId);
        return Result.success();
    }

    @GetMapping("/relocalization")
    @ResponseBody
    @ApiOperation(value = "重定位", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result relocalization(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                                 @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        remoteControlService.relocalization(robotId, roomId);
        return Result.success();
    }

    @GetMapping("/lifter")
    @ResponseBody
    @ApiOperation(value = "升降杆控制", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result lifter(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                         @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                         @ApiParam(value = "升降杆位置") @RequestParam(defaultValue = "0", required = false, name = "position") String position) {
        remoteControlService.lifter(robotId, roomId, position);
        return Result.success();
    }

    @GetMapping("/emergencyStop")
    @ResponseBody
    @ApiOperation(value = "急停", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result emergencyStop(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                                @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        remoteControlService.emergencyStop(robotId, roomId);
        return Result.success();
    }

    @GetMapping("/backChargingPile")
    @ResponseBody
    @ApiOperation(value = "返航", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result backChargingPile(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                                   @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) throws InterruptedException {
        remoteControlService.backChargingPile(robotId, roomId);
        return Result.success();
    }

    @GetMapping("/frontPicture")
    @ResponseBody
    @ApiOperation(value = "前置拍照", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result frontPicture(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                               @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) throws InterruptedException {
        remoteControlService.frontPicture(robotId, roomId);
        return Result.success();
    }

    @GetMapping("/afterPicture")
    @ResponseBody
    @ApiOperation(value = "后置拍照", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result afterPicture(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                               @ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) throws InterruptedException {
        remoteControlService.afterPicture(robotId, roomId);
        return Result.success();
    }

    @GetMapping("/picTaskList")
    @ResponseBody
    @ApiOperation(value = "相册任务列表", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result picTaskList(@ApiParam(value = "机房id") @RequestParam(name = "roomId") long roomId) {
        return Result.success(remoteControlService.picTaskList(roomId));
    }

    @GetMapping("/picTaskDetl")
    @ResponseBody
    @ApiOperation(value = "相册任务明细", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result picTaskDetl(@ApiParam(value = "任务类型") @RequestParam(name = "taskType") String taskType,
                              @ApiParam(value = "实例id") @RequestParam(name = "instanceId") long instanceId) {
        return Result.success(remoteControlService.picTaskDetl(taskType, instanceId));
    }


    @GetMapping("/liftResult")
    @ResponseBody
    @ApiOperation(value = "升降杆回馈", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result liftResult(@ApiParam(value = "升降杆是否正常反馈") @RequestParam(name = "value") String value, @RequestParam(name = "robotId") Long robotId) {
        remoteControlService.liftingLeverResults(robotId, value);
        return Result.success();
    }

    @GetMapping("/getWebsocketUrl")
    @ResponseBody
    @ApiOperation(value = "获取websocket url", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getWebsocketUrl() {
        return Result.success(remoteControlService.getWebsocketUrl());
    }
}
