package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.TaskInfo;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.service.TaskInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理controller
 * @author kliu
 * @date 2022/6/1 20:25
 */
@Controller
@RequestMapping(path="/industrial_robot/taskinfo")
@Api(tags = "任务管理")
public class TaskInfoController {

    @Autowired
    private TaskInfoService taskInfoService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取任务列表")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                       @ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) throws Exception {
        return Result.success(taskInfoService.list(roomId, robotId, pageSize, pageNum));
    }

    @GetMapping("/listWithoutPark")
    @ResponseBody
    @ApiOperation(value="获取所有任务列表-手机监控使用")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result listWithoutPark(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                       @ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) throws Exception {
        return Result.success(taskInfoService.listWithoutPark(roomId, robotId, pageSize, pageNum));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="创建任务", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody @Validated TaskInfo taskInfo) throws Exception {
        taskInfoService.add(taskInfo);
        return Result.success("");
    }

    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value="删除任务", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody TaskInfo taskInfo) throws Exception {
        taskInfoService.delete(taskInfo);
        return Result.success("");
    }

    @PostMapping("/startNow")
    @ResponseBody
    @ApiOperation(value="任务立即执行", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result startNow(@RequestBody TaskInfo taskInfo) throws Exception {
        taskInfoService.startNow(taskInfo);
        return Result.success("");
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation(value="更新任务", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody @Validated TaskInfo taskInfo) throws Exception {
        taskInfoService.update(taskInfo);
        return Result.success("");
    }

    @GetMapping("/getRobotRunningTaskDesc")
    @ResponseBody
    @ApiOperation(value="获取机器人正在运行的任务描述", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRobotRunningTaskDesc(@RequestParam("robotId") long robotId){
        return Result.success(taskInfoService.getRobotRunningTaskDesc(robotId));
    }

}
