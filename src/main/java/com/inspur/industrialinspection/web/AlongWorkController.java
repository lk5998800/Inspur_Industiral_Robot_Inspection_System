package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.AlongWork;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.service.AlongWorkService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 随工管理controller
 * @author kliu
 * @date 2022/6/13 11:27
 */
@Controller
@RequestMapping(path = "/industrial_robot/along_work")
@Api(tags = {"随工管理"})
@Slf4j
public class AlongWorkController {

    @Autowired
    private AlongWorkService alongWorkService;

    /**
     * 获取任务列表
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @param status
     * @param taskTime
     * @param keyword
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/6/13 11:26
     */
    @GetMapping("/pageList")
    @ResponseBody
    @ApiOperation(value = "获取随工任务列表", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result pageList(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                           @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                           @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum,
                           @ApiParam(value = "当前状态") @RequestParam(required = false, name = "status") String status,
                           @ApiParam(value = "任务时间") @RequestParam(required = false, name = "taskTime") String taskTime,
                           @ApiParam(value = "任务名称/人员名称") @RequestParam(required = false, name = "keyword") String keyword) throws Exception {
        return Result.success(alongWorkService.pageList(roomId, pageSize, pageNum, status, taskTime, keyword));
    }

    /**
     * 新增或修改任务
     * @param alongWork
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/6/13 11:26
     */
    @PostMapping("/addOrUpdate")
    @ResponseBody
    @ApiOperation(value = "添加或修改随工任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result addOrUpdate(@RequestBody @Validated AlongWork alongWork) {
        alongWorkService.addOrUpdate(alongWork);
        return Result.success();
    }

    /**
     * 删除任务
     * @param id
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/6/13 11:26
     */
    @GetMapping("delete")
    @ResponseBody
    @ApiOperation(value = "删除随工任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@ApiParam(value = "主键id", required = true) @RequestParam("id") int id) {
        alongWorkService.delete(id);
        return Result.success();
    }

    /**
     * 开始任务
     * @param id
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/6/13 11:26
     */
    @GetMapping("startTask")
    @ResponseBody
    @ApiOperation(value = "开始任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result startTask(@ApiParam(value = "主键id", required = true) @RequestParam("id") long id) throws IOException {
        alongWorkService.startTask(id);
        return Result.success();
    }

    /**
     * 结束任务
     * @param id
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/6/13 11:27
     */
    @GetMapping("endTask")
    @ResponseBody
    @ApiOperation(value = "结束任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result endTask(@ApiParam(value = "主键id", required = true) @RequestParam("id") long id) {
        alongWorkService.endTask(id);
        return Result.success();
    }

    /**
     * 接收随工执行记录-时间、点位
     * @param alongWorkDetlJson
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/6/14 16:31
     */
    @PostMapping("/receiveAlongWorkDetl")
    @ResponseBody
    @ApiOperation(value="接收随工执行记录")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result receiveAlongWorkDetl(@ApiParam(value = "随工执行过程", required = true) @RequestParam("alongWorkDetlJson") String alongWorkDetlJson){
        alongWorkService.receiveAlongWorkDetl(alongWorkDetlJson);
        return Result.success();
    }

    @PostMapping("/pedestrianDetectionAlarmInformation")
    @ResponseBody
    @ApiOperation(value="行人检测告警信息接收")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result pedestrianDetectionAlarmInformation(@ApiParam(value = "行人检测告警信息", required = true) @RequestParam("alarmInformation") String alarmInformation){
        alongWorkService.pedestrianDetectionAlarmInformationSave(alarmInformation);
        return Result.success();
    }

    @GetMapping("getRunningAlongWork")
    @ResponseBody
    @ApiOperation(value = "获取正在执行的随工任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRunningAlongWork(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        return Result.success(alongWorkService.getRunningAlongWork(roomId));
    }

    @GetMapping("getAlongWorkDetl")
    @ResponseBody
    @ApiOperation(value = "获取随工任务明细", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getAlongWorkDetl(@ApiParam(value = "随工id") @RequestParam(defaultValue = "0", required = true, name = "id") long id) {
        return Result.success(alongWorkService.getAlongWorkDetl(id));
    }

    @GetMapping("getAlongWorkDetlForRobot")
    @ResponseBody
    @ApiOperation(value = "获取随工任务明细", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getAlongWorkDetlForRobot(@ApiParam(value = "任务id") @RequestParam(defaultValue = "0", required = true, name = "taskId") long taskId) {
        return Result.success(alongWorkService.getAlongWorkDetlForRobot(taskId));
    }

    @GetMapping("getAlongWorkPointInfos")
    @ResponseBody
    @ApiOperation(value = "获取机房下的随工点位", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getAlongWorkPointInfos(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        return Result.success(alongWorkService.getAlongWorkPointInfos(roomId));
    }

    @PostMapping("/initAlongWorkPoint")
    @ResponseBody
    @ApiOperation(value = "初始化随工点", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result initAlongWorkPoint(@RequestBody RoomInfo roomInfo) throws InterruptedException {
        alongWorkService.initAlongWorkPoint(roomInfo);
        return Result.success();
    }

    @PostMapping("/receiveAlongWorkPoints")
    @ResponseBody
    @ApiOperation(value="接收转换后的随工点")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result receiveAlongWorkPoints(@RequestBody JSONObject jsonObject){
        alongWorkService.receiveAlongWorkPoints(jsonObject);
        return Result.success();
    }

    @PostMapping("/associatedWaitPoint")
    @ResponseBody
    @ApiOperation(value="关联随工待命点")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result associatedWaitPoint(@RequestBody RoomInfo roomInfo) throws InterruptedException {
        alongWorkService.associatedWaitPoint(roomInfo.getRoomId());
        return Result.success();
    }

    @PostMapping("/associatedAlongWorkPoint")
    @ResponseBody
    @ApiOperation(value="关联随工点")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result associatedAlongWorkPoint(@RequestBody PointInfo pointInfo) throws InterruptedException {
        alongWorkService.associatedAlongWorkPoint(pointInfo);
        return Result.success();
    }

    @GetMapping("getAlongWorkPointInfosIncludeAll")
    @ResponseBody
    @ApiOperation(value = "获取机房下的随工点位-包含未关联的随工点", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getAlongWorkPointInfosIncludeAll(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        return Result.success(alongWorkService.getAlongWorkPointInfosIncludeAll(roomId));
    }
}
