package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.DetectionInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
/**
 * 检测项
 * @author kliu
 * @date 2022/6/7 16:07
 */
@Controller
@RequestMapping(path="/industrial_robot/detectioninfo")
@Api(tags = {"检测项"})
public class DetectionInfoController {

    @Autowired
    private DetectionInfoService detectionInfoService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取检测项列表")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id") @RequestParam(name = "roomId") long roomId,
                       @ApiParam(value = "检测项组合代号") @RequestParam(required = false, name = "combinationCode") String combinationCode) throws IOException {
        return Result.success(detectionInfoService.list(roomId, combinationCode));
    }
}
