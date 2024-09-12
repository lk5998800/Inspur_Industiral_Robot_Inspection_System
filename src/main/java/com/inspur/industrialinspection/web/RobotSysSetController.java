package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.RobotSysSetService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
/**
 * 机器人系统设置
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/robotsysset")
@Api(tags = {"机器人"})
@Slf4j
public class RobotSysSetController {

    @Autowired
    private RobotSysSetService robotSysSetService;

    @GetMapping("/issued")
    @ResponseBody
    @ApiOperation(value="下发机器人参数", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result issued(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId) throws IOException, InterruptedException {
        robotSysSetService.issued(robotId);
        return Result.success("");
    }

    @PostMapping("/receiveRobotSysSetResult")
    @ResponseBody
    @ApiOperation(value="接收机器人系统设置下发结果")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public Result receiveRobotSysSetResult(@ApiParam(value = "机器人系统设置下发结果", required = true) @RequestParam("robot_sys_set_json") String robot_sys_set_json){
        robotSysSetService.receiveRobotSysSetResult(robot_sys_set_json);
        return Result.success();
    }
}
