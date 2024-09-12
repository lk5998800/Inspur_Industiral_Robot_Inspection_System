package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.service.RobotInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 机器人信息
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/robotinfo")
@Api(tags = {"机器人"})
@Slf4j
public class RobotInfoController {

    @Autowired
    private RobotInfoService robotService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取机器人列表信息", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        List list = robotService.list(roomId);
        return Result.success(list);
    }

    @GetMapping("/listWithoutPark")
    @ResponseBody
    @ApiOperation(value="获取所有机器人列表信息", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result listWithoutPark(){
        List list = robotService.listWithoutPark();
        return Result.success(list);
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加机器人信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody JSONObject jsonObject) {
        robotService.add(jsonObject);
        return Result.success("");
    }
}
