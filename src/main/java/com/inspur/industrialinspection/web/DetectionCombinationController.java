package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.DetectionCombination;
import com.inspur.industrialinspection.service.DetectionCombinationService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author kliu
 * @description 检测项组合controller
 * @date 2022/4/28 8:29
 */
@Controller
@RequestMapping(path="/industrial_robot/detectioncombination")
@Api(tags = {"检测项"})
@Slf4j
public class DetectionCombinationController {

    @Autowired
    private DetectionCombinationService detectionCombinationService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取检测项组合")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) {
        return Result.success(detectionCombinationService.list(roomId));
    }
//    @PostMapping("/add")
//    @ResponseBody
//    @ApiOperation(value="添加检测项组合", notes="")
//    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
//    public Result add(@RequestBody DetectionCombination detectionCombination) {
//        detectionCombinationService.add(detectionCombination);
//        return Result.success("");
//    }
//
//    @PostMapping("/update")
//    @ResponseBody
//    @ApiOperation(value="更新检测项组合", notes="")
//    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
//    public Result update(@RequestBody DetectionCombination detectionCombination) {
//        detectionCombinationService.update(detectionCombination);
//        return Result.success("");
//    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加检测项组合", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody JSONObject jsonObject) {
        detectionCombinationService.add(jsonObject);
        return Result.success("");
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation(value="更新检测项组合", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody JSONObject jsonObject) {
        detectionCombinationService.update(jsonObject);
        return Result.success("");
    }
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value="删除检测项组合", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody DetectionCombination detectionCombination) {
        detectionCombinationService.delete(detectionCombination);
        return Result.success("");
    }
}
