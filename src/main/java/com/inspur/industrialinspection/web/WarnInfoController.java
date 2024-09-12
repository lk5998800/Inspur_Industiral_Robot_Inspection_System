package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.vo.TopInformationVo;
import com.inspur.industrialinspection.entity.vo.WarnInfoCountVo;
import com.inspur.industrialinspection.service.WarnInfoService;
import com.inspur.industrialinspection.entity.vo.WarnMessageResultVo;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author: LiTan
 * @description: 报警记录
 * @date: 2022-10-26 09:39:28
 */
@RequestMapping(path = "/industrial_robot/warn_info")
@RestController
@Api(tags = "报警记录")
public class WarnInfoController {

    @Autowired
    WarnInfoService warnInfoService;

    @GetMapping("/getWarnInfo")
    @ApiOperation(value = "获取报警信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getWarnInfo(@ApiParam(value = "机房实例id", required = true) @RequestParam("roomId") long roomId) {
        List<WarnMessageResultVo> warnInfos = warnInfoService.getWarnInfo(roomId);
        return Result.success(warnInfos);
    }

    @GetMapping("/getCountInfo")
    @ApiOperation(value = "获取报警信息数量")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getCountWarnInfo(@ApiParam(value = "机房实例id") @RequestParam("roomId") long roomId) {
        List<WarnInfoCountVo> warnInfoCountVos = warnInfoService.getCountWarnInfo(roomId);
        return Result.success(warnInfoCountVos);
    }


    @GetMapping("/getWarnInfoProportion")
    @ApiOperation(value = "获取报警信息占比")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getWarnInfoProportion(@ApiParam(value = "机房实例id") @RequestParam("roomId") long roomId) {
        List list = warnInfoService.getWarnInfoProportion(roomId);
        return Result.success(list);
    }

    @GetMapping("/getTopInformation")
    @ApiOperation(value = "获取监控中心机器人置顶信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getTopInformation(@ApiParam(value = "机房实例id") @RequestParam("roomId") long roomId) {
        TopInformationVo topInformationVo = warnInfoService.getTopInformation(roomId);
        return Result.success(topInformationVo);
    }

}
