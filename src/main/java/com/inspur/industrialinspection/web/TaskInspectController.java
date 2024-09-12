package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.TaskInspect;
import com.inspur.industrialinspection.service.TaskInspectService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 巡检任务
 * @author wangzhaodi
 * @date 2022/11/16 13:58
 */
@Controller
@RequestMapping(path = "/industrial_robot/taskinspect")
@Api(tags = "手机巡检")
@Slf4j
public class TaskInspectController {

    @Autowired
    private TaskInspectService taskInspectService;

    @GetMapping("/getByRoomName")
    @ResponseBody
    @ApiOperation(value = "根据机房名称获取巡检任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getByRoomName(@RequestParam("roomName") String roomName) {
        TaskInspect taskInspect = taskInspectService.getByRoomName(roomName);
        return Result.success(taskInspect);
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "添加巡检任务信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody TaskInspect taskInspect) throws Exception {
        taskInspectService.addTask(taskInspect);
        return Result.success("");
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation(value="结束任务", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody TaskInspect taskInspect) throws Exception {
        taskInspectService.endTask(taskInspect);
        return Result.success("");
    }
}
