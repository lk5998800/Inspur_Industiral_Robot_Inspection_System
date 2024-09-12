package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.RealTimeVideoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author kliu
 * @description 实时视频相关服务
 * @date 2022/5/6 11:29
 */
@Controller
@RequestMapping(path="/industrial_robot/realtimevideo")
@Api(tags = {"实时视频"})
@Slf4j
public class RealTimeVideoController {

    @Autowired
    RealTimeVideoService realTimeVideoService;

    @GetMapping("/start")
    @ResponseBody
    @ApiOperation(value="开始实时视频")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result start(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                        @ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId) throws Exception {
        return Result.success(realTimeVideoService.start(roomId, robotId));
    }
    @GetMapping("/heart")
    @ResponseBody
    @ApiOperation(value="实时视频心跳", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result heart(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                        @ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId) throws IOException, InterruptedException {
        realTimeVideoService.heart(roomId, robotId);
        return Result.success("");
    }

    @GetMapping("/startold")
    @ResponseBody
    @ApiOperation(value="开始实时视频V1.1")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings({"AlibabaLowerCamelCaseVariableNaming", "AlibabaUndefineMagicConstant"})
    @Deprecated
    public Result startold(@ApiParam(value = "agentEsn", required = true) @RequestParam("agentEsn") String agentEsn) throws Exception {
        long roomId = 0;
        if ("inspection-robot-001".equals(agentEsn)){
            roomId = 1;
        }else if ("inspection-robot-002".equals(agentEsn)){
            roomId = 2;
        }else if ("inspection-robot-003".equals(agentEsn)){
            roomId = 3;
        }else if ("inspection-robot-004".equals(agentEsn)){
            roomId = 4;
        }else if ("inspection-robot-123".equals(agentEsn)){
            roomId = 5;
        }

        return Result.success(realTimeVideoService.start(roomId, 0));
    }
    @GetMapping("/heartold")
    @ResponseBody
    @ApiOperation(value="实时视频心跳V1.1", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings({"AlibabaLowerCamelCaseVariableNaming", "AlibabaUndefineMagicConstant"})
    @Deprecated
    public Result heartold(@ApiParam(value = "agentEsn", required = true) @RequestParam("agentEsn") String agentEsn) throws IOException, InterruptedException {
        long roomId = 0;
        if ("inspection-robot-001".equals(agentEsn)){
            roomId = 1;
        }else if ("inspection-robot-002".equals(agentEsn)){
            roomId = 2;
        }else if ("inspection-robot-003".equals(agentEsn)){
            roomId = 3;
        }else if ("inspection-robot-004".equals(agentEsn)){
            roomId = 4;
        }else if ("inspection-robot-123".equals(agentEsn)){
            roomId = 5;
        }
        realTimeVideoService.heart(roomId, 0);
        return Result.success("");
    }

    @PostMapping("/receiveStreamStartResult")
    @ResponseBody
    @ApiOperation(value="接收推流结果")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public Result receiveStreamStartResult(@ApiParam(value = "接收推流开始结果", required = true) @RequestParam("stream_start_json") String stream_start_json){
        realTimeVideoService.receiveStreamResult(stream_start_json);
        return Result.success();
    }
}
