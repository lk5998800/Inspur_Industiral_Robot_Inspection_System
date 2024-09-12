package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.TaskReportService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

/**
 * 任务记录
 * @author kliu
 * @date 2022/6/7 15:27
 */
@Controller
@RequestMapping(path="/industrial_robot/taskreport")
@Api(tags = "任务记录")
public class TaskReportController {

    @Autowired
    TaskReportService taskReportService;

    /**
     * 获取任务报告信息
     * @param instanceId
     * @return com.inspur.result.Result
     * @author kliu
     * @date 2022/6/7 15:26
     */
    @GetMapping("/getTaskReport")
    @ResponseBody
    @ApiOperation(value="获取任务报告信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTaskReport(@ApiParam(value = "任务执行实例id", required = true) @RequestParam("instanceId") long instanceId) throws Exception {
        HashMap hashMap = taskReportService.getTaskReport(instanceId);
        return Result.success(hashMap);
    }
}
