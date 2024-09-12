package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.DetectionParam;
import com.inspur.industrialinspection.service.DetectionInfoService;
import com.inspur.industrialinspection.service.DetectionParamService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 检测项参数
 * @author kliu
 * @date 2022/6/7 16:07
 */
@Controller
@RequestMapping(path="/industrial_robot/detectionparam")
@Api(tags = {"检测项"})
public class DetectionParamController {

    @Autowired
    private DetectionInfoService detectionInfoService;
    @Autowired
    private DetectionParamService detectionParamService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取检测项及参数")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId,
                       @ApiParam(value = "检测项组合代号") @RequestParam(required = false, name = "combinationCode") String combinationCode) throws IOException {
        return Result.success(detectionInfoService.list(roomId, combinationCode));
    }

    @Deprecated
    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加检测项参数", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody DetectionParam detectionParam) throws IOException {
        detectionParamService.add(detectionParam);
        return Result.success("");
    }
}
