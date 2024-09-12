package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.GatingPara;
import com.inspur.industrialinspection.service.GatingService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门控controller
 * @author kliu
 * @date 2022/6/13 11:27
 */
@Controller
@RequestMapping(path = "/industrial_robot/gating")
@Api(tags = {"门控"})
@Slf4j
public class GatingController {

    @Autowired
    private GatingService gatingService;

    @PostMapping("/invokeGating")
    @ResponseBody
    @ApiOperation(value="门控开门")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result invokeGating(@RequestBody JSONObject jsonObject) throws Exception {
        gatingService.invokeGating(jsonObject);
        return Result.success();
    }

    @GetMapping("/getGatingPara")
    @ResponseBody
    @ApiOperation(value="获取门控参数")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getGatingPara(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                                @ApiParam(value = "点位名称") @RequestParam(defaultValue = "0", required = false, name = "pointName") String pointName) throws InterruptedException {
        return Result.success(gatingService.getDetlById(roomId, pointName));
    }

    @PostMapping("/addOrUpdate")
    @ResponseBody
    @ApiOperation(value="添加门控参数")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result addOrUpdate(@RequestBody GatingPara gatingPara) throws InterruptedException {
        gatingService.addOrUpdate(gatingPara);
        return Result.success();
    }

    @GetMapping("/getGatingParas")
    @ResponseBody
    @ApiOperation(value="根据单个点位获取所有门控参数")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getGatingParas(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                                @ApiParam(value = "点位名称") @RequestParam(defaultValue = "0", required = false, name = "pointName") String pointName) throws InterruptedException {
        return Result.success(gatingService.getDetlsById(roomId, pointName));
    }

    @PostMapping("/addOrUpdateList")
    @ResponseBody
    @ApiOperation(value="修改多个门控参数")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result addOrUpdateList(@RequestBody List<GatingPara> gatingPara) throws InterruptedException {
        gatingService.addOrUpdateList(gatingPara);
        return Result.success();
    }

    @GetMapping("/invokeOpenDoor")
    @ResponseBody
    @ApiOperation(value="门控开门")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result invokeOpenDoor(@ApiParam(value = "门控id") @RequestParam(name = "doorCode") int doorCode) {
        gatingService.invokeOpenDoor(doorCode);
        return Result.success();
    }
    @GetMapping("/invokeCloseDoor")
    @ResponseBody
    @ApiOperation(value="门控关门")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result invokeCloseDoor(@ApiParam(value = "门控id") @RequestParam(name = "doorCode") int doorCode) {
        gatingService.invokeCloseDoor(doorCode);
        return Result.success();
    }

}
