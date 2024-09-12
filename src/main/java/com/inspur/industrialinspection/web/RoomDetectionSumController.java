package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.thread.RoomDetectionSumDayService;
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
 * 机房检测项汇总
 * @author kliu
 * @date 2022/7/29 13:39
 */
@Controller
@RequestMapping(path = "/industrial_robot/roomdetectionsum")
@Api(tags = {"机房检测项汇总"})
@Slf4j
public class RoomDetectionSumController {

    @Autowired
    private RoomDetectionSumDayService roomDetectionSumDayService;

    @GetMapping("/resetAndSum")
    @ResponseBody
    @ApiOperation(value = "重置机房统计数据并重新计算汇总数据", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result resetAndSum(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId){
        roomDetectionSumDayService.resetAndSum(roomId);
        return Result.success();
    }
}
