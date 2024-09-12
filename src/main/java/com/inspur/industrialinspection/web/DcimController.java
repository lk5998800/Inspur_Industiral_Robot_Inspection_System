package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.service.DcimService;
import com.inspur.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * dcim接口
 * @author kliu
 * @date 2022/9/1 15:41
 */
@Controller
@RequestMapping(path = "/")
@Api(tags = {"DCIM接口"})
@Slf4j
public class DcimController {

    @Autowired
    private DcimService dcimService;

    @PostMapping("/industrial_robot/dciminvoke")
    @ResponseBody
    @ApiOperation(value = "通用接口调用", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result dcimInvoke(@RequestBody JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return Result.success(dcimService.dcimInvoke(jsonObject, httpServletRequest));
    }
}
