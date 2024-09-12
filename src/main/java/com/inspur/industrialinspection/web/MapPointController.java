package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.MapPoint;
import com.inspur.industrialinspection.service.MapPointService;
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
@RequestMapping(path="/industrial_robot/mappoint")
@Api(tags = {"地图点位"})
@Slf4j
public class MapPointController {

    @Autowired
    private MapPointService mapPointService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="列表")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws Exception {
        return Result.success(mapPointService.list(roomId));
    }
    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody MapPoint mapPoint) throws IOException {
        mapPointService.add(mapPoint);
        return Result.success("");
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation(value="更新", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody MapPoint mapPoint) throws IOException {
        mapPointService.update(mapPoint);
        return Result.success("");
    }
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value="删除", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody MapPoint mapPoint) throws IOException {
        mapPointService.delete(mapPoint);
        return Result.success("");
    }
}
