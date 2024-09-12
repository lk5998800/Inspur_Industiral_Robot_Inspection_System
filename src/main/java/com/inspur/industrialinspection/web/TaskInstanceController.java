package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.service.TaskInstanceService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 任务记录
 *
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path = "/industrial_robot/taskinstance")
@Api(tags = "任务记录")
@Slf4j
public class TaskInstanceController {

    @Autowired
    private TaskInstanceService taskInstanceService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取任务执行记录")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                       @ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        return Result.success(taskInstanceService.list(roomId, robotId, pageSize, pageNum));
    }

    @PostMapping("/terminate")
    @ResponseBody
    @ApiOperation(value = "终止任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result terminate(@RequestBody TaskInstance taskInstance) throws Exception {
        taskInstanceService.terminate(taskInstance);
        return Result.success("");
    }

    @PostMapping("/receiveTerminateResult")
    @ResponseBody
    @ApiOperation(value = "接收任务终止结果")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public Result receiveTerminateResult(@ApiParam(value = "检测结果", required = true) @RequestParam("terminate_result_json") String terminate_result_json) {
        taskInstanceService.receiveTerminateResult(terminate_result_json);
        return Result.success();
    }

    @PostMapping("/industrialRobotEndTask")
    @ResponseBody
    @ApiOperation(value = "任务结束")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public Result industrialRobotEndTask(@ApiParam(value = "任务结束", required = true) @RequestParam("endTaskTimeJson") String endTaskTimeJson) {
        taskInstanceService.industrialRobotEndTask(endTaskTimeJson);
        return Result.success();
    }

    @GetMapping("/getRunningTaskCount")
    @ResponseBody
    @ApiOperation(value = "获取运行的任务数量-远程控制使用", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRunningTaskCount(@ApiParam(value = "机房id") @RequestParam(name = "roomId") long roomId) {
        return Result.success(taskInstanceService.getRunningTaskCount(roomId));
    }

    /**
     * 终止正在运行的任务，远程控制中使用
     *
     * @param roomId
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/9/19 15:35
     */
    @GetMapping("/terminateRunningTask")
    @ResponseBody
    @ApiOperation(value = "终止正在运行的任务，远程控制中使用", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result terminateRunningTask(@ApiParam(value = "机房id") @RequestParam(name = "roomId") long roomId) {
        taskInstanceService.terminateRunningTask(roomId);
        return Result.success();
    }


    @GetMapping("/getBackChargingPilePath")
    @ResponseBody
    @ApiOperation(value = "获取返回充电桩任务路径")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getBackChargingPilePath(@ApiParam(value = "任务id") @RequestParam(defaultValue = "0", required = true, name = "taskId") long taskId,
                                          @ApiParam(value = "任务类型") @RequestParam(defaultValue = "0", required = true, name = "type") int type,
                                          @ApiParam(value = "点位名称", required = true) @RequestParam("pointName") String pointName) {
        return Result.success(taskInstanceService.getBackChargingPilePath(taskId, type, pointName));
    }

    @GetMapping("/getBackToPointNamePath")
    @ResponseBody
    @ApiOperation(value = "获取到达点位的路径规划")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getBackToPointNamePath(@ApiParam(value = "任务id") @RequestParam(defaultValue = "0", required = true, name = "taskId") long taskId,
                                         @ApiParam(value = "任务类型") @RequestParam(defaultValue = "0", required = true, name = "type") int type,
                                         @ApiParam(value = "点位名称", required = true) @RequestParam("pointName") String pointName) {
        return Result.success(taskInstanceService.getBackToPointNamePath(taskId, type, pointName));
    }

    @PostMapping("/updateTaskStatus")
    @ResponseBody
    @ApiOperation(value = "暂停任务接口，修改任务为暂停状态")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result updateTaskStatus(@RequestParam("taskStatusJson") String taskStatusJson) {
        taskInstanceService.updateTaskStatus(taskStatusJson);
        return Result.success();
    }


}
