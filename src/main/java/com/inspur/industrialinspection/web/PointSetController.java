package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.service.PointSetService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 检测点设置
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/pointset")
@Api(tags = {"检测点"})
@Slf4j
public class PointSetController {

    @Autowired
    private PointSetService pointSetService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取检测点设置信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws IOException {
        return Result.success(pointSetService.list(roomId));
    }

    @PostMapping("/adds")
    @ResponseBody
    @ApiOperation(value="添加检测点设置", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result adds(@RequestBody JSONObject pointSetObject) throws IOException {
        pointSetService.adds(pointSetObject);
        return Result.success("");
    }
}
