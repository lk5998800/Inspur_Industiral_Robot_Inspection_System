package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.service.RobotMoveService;
import com.inspur.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 机器人移动
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/robotmove")
@Api(tags = {"机器人"})
public class RobotMoveController {

    @Autowired
    private RobotMoveService robotMoveService;

    @PostMapping("/move")
    @ResponseBody
    @ApiOperation(value="机器人移动")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result move(@RequestBody PointInfo pointInfo){
        robotMoveService.move(pointInfo);
        return Result.success();
    }
}
