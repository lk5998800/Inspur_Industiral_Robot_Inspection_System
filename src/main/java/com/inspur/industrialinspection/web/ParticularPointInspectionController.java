package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskInstance;
import com.inspur.industrialinspection.service.ParticularPointInspectionService;
import com.inspur.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 楼栋信息
 * @author kliu
 * @date 2022/6/17 19:15
 */
@Controller
@RequestMapping(path="/industrial_robot/particularpointinspection")
@Api(tags = {"特定点巡检"})
@Slf4j
public class ParticularPointInspectionController {

    @Autowired
    private ParticularPointInspectionService particularPointInspectionService;

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加特定点巡检任务", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody @Validated ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance) {
        particularPointInspectionService.add(particularPointInspectionTaskInstance);
        return Result.success("");
    }
}
