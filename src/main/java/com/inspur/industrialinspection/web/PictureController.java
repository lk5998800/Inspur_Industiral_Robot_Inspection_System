package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.PictureService;
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
 * 机柜照片
 * @author kliu
 * @date 2022/6/17 19:15
 */
@Controller
@RequestMapping(path="/industrial_robot/picture")
@Api(tags = {"全局相册查询"})
@Slf4j
public class PictureController {

    @Autowired
    private PictureService pictureService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取图片列表")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                       @ApiParam(value = "任务类型") @RequestParam(required = false, name = "taskType") String taskType,
                       @ApiParam(value = "开始时间", required = true) @RequestParam(name = "startTime") String startTime,
                       @ApiParam(value = "结束时间", required = true) @RequestParam(name = "endTime") String endTime,
                       @ApiParam(value = "任务名称") @RequestParam(required = false, name = "taskName") String taskName) {
        return Result.success(pictureService.list(roomId, taskType, startTime, endTime, taskName));
    }
    @GetMapping("/detl")
    @ResponseBody
    @ApiOperation(value="获取图片明细")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result detl(@ApiParam(value = "任务实例id", required = true) @RequestParam("instanceId") long instanceId,
                       @ApiParam(value = "任务类型") @RequestParam(required = false, name = "taskType") String taskType,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        return Result.success(pictureService.detl(taskType, instanceId, pageSize, pageNum));
    }
}
