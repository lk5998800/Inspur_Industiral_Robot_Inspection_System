package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.PointTreeNodeService;
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
 * 楼栋信息
 * @author kliu
 * @date 2022/6/17 19:15
 */
@Controller
@RequestMapping(path="/industrial_robot/pointtreenode")
@Api(tags = {"常州测试"})
@Slf4j
public class PointTreeNodeController {

    @Autowired
    private PointTreeNodeService pointTreeNodeService;

    @GetMapping("/getPoint2ChargingPilePath")
    @ResponseBody
    @ApiOperation(value="依据点位计算返回充电桩路径", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getPoint2ChargingPilePath(@ApiParam(value = "点位名称", required = true) @RequestParam("pointName") String pointName,
                                                @ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId){
        return Result.success(pointTreeNodeService.getPoint2ChargingPilePath(roomId, pointName));
    }
    @GetMapping("/getChargingPilePath2Point")
    @ResponseBody
    @ApiOperation(value="计算充电桩到点位的路径", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getChargingPilePath2Point(@ApiParam(value = "点位名称", required = true) @RequestParam("pointName") String pointName,
                                                @ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId){
        return Result.success(pointTreeNodeService.getChargingPilePath2Point(roomId, pointName));
    }

    @GetMapping("/getPoint2PointPath")
    @ResponseBody
    @ApiOperation(value="计算点位到点位的最短路径", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getPoint2PointPath(@ApiParam(value = "基础点位", required = true) @RequestParam("pointNameBasic") String pointNameBasic,
                                     @ApiParam(value = "目标点位", required = true) @RequestParam("pointNameTarget") String pointNameTarget,
                                     @ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId){
        return Result.success(pointTreeNodeService.getPoint2PointPath(roomId, pointNameBasic, pointNameTarget));
    }
}
