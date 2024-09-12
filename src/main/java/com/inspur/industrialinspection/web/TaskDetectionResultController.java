package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.TaskDetectionResultService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @projectName: platform_app_service
 * @package: com.inspur.industrial_robot.web
 * @className: TaskDetectionResult
 * @author: kliu
 * @description: 任务检测结果接收
 * @date: 2022/4/12 17:41
 * @version: 1.0
 */
@Controller
@RequestMapping(path="/industrial_robot/taskdetectionresult")
@Api(value = "检测结果", tags = {"检测结果"})
@Slf4j
public class TaskDetectionResultController {

    @Autowired
    private BeanFactory beanFactory;

    @PostMapping("/receiveDetectionResult")
    @ResponseBody
    @ApiOperation(value="接收检测结果")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result receiveDetectionResult(@ApiParam(value = "检测结果", required = true) @RequestParam("detectionResultJson") String detectionResultJson){
        TaskDetectionResultService taskDetectionResultService = beanFactory.getBean(TaskDetectionResultService.class);
        taskDetectionResultService.add(detectionResultJson);
        return Result.success();
    }
}
