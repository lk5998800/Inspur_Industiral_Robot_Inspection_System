package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.RoomParamService;
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
 * 机房参数
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/roomparam")
@Api(value = "机房参数", tags = {"机房参数"})
@Slf4j
public class RoomParamController {

    @Autowired
    private RoomParamService roomParamService;

    @GetMapping("getcamaraparam")
    @ResponseBody
    @ApiOperation(value="获取机房相机参数", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getCamaraParam(@ApiParam(value = "任务id") @RequestParam("taskId") long taskId) throws Exception {
        return Result.success(roomParamService.getCamaraParam(taskId));
    }
}
