package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.RelocalizationService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 重定位
 * @author kliu
 * @date 2022/8/12 11:14
 */
@Controller
@RequestMapping(path="/industrial_robot/relocalization")
@Api(tags = {"机器人"})
@Slf4j
public class RelocalizationController {

    @Autowired
    private RelocalizationService relocalizationService;

    @PostMapping("/success")
    @ResponseBody
    @ApiOperation(value="机器人重定位成功")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result success(@ApiParam(value = "机器人id",  required = true) @RequestParam(name = "robotId") long robotId){
        relocalizationService.success(robotId);
        return Result.success();
    }
}
