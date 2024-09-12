package com.inspur.industrialinspection.web;

import cn.hutool.core.date.DateUtil;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.service.PointInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 检测点相关
 * @author kliu
 * @date 2022/5/27 9:16
 */
@Controller
@RequestMapping(path="/industrial_robot/pointinfo")
@Api(tags = {"检测点"})
@Slf4j
public class PointInfoController {

    @Autowired
    private PointInfoService pointInfoService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取检测点基本信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws IOException {
        return Result.success(pointInfoService.list(roomId));
    }

    @GetMapping("/getRealTimePosture")
    @ResponseBody
    @ApiOperation(value="获取实时位姿")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRealTimePosture(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws InterruptedException {
        return Result.success(pointInfoService.getRealTimePosture(roomId));
    }

    @PostMapping("/receivePosture")
    @ResponseBody
    @ApiOperation(value="接收位姿")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public Result receivePosture(@ApiParam(value = "位姿", required = true) @RequestParam("posture_json") String posture_json){
        pointInfoService.receivePosture(posture_json);
        return Result.success();
    }

    @PostMapping("/associatedPosture")
    @ResponseBody
    @ApiOperation(value="关联位姿")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result associatedPosture(@RequestBody PointInfo pointInfo) throws InterruptedException {
        pointInfoService.associatedPosture(pointInfo);
        return Result.success();
    }

    @PostMapping("/associatedWaitPoint")
    @ResponseBody
    @ApiOperation(value="关联待命点")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result associatedWaitPoint(@RequestBody RoomInfo roomInfo) throws InterruptedException, IOException {
        pointInfoService.associatedWaitPoint(roomInfo.getRoomId());
        return Result.success();
    }

    @PostMapping("/associatedPostureManual")
    @ResponseBody
    @ApiOperation(value="手动关联位姿")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result associatedPostureManual(@RequestBody PointInfo pointInfo) throws InterruptedException {
        pointInfoService.associatedPostureManual(pointInfo);
        return Result.success();
    }
}
