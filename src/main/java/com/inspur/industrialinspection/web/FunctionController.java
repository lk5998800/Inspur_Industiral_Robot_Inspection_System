package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.*;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 获取功能
 * @author wangzhaodi
 * @date 2022/11/14 14:42
 */
@Controller
@RequestMapping(path="/industrial_robot/functions")
@Api(tags = {"功能信息"})
@Slf4j
public class FunctionController {
    @Autowired
    private FunctionInfoService functionInfoService;
    @Autowired
    private RequestService requestService;

    @GetMapping("/getRight")
    @ResponseBody
    @ApiOperation(value="获取用户具有的权限")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getRight(HttpServletRequest request) {
        String token = request.getHeader("token");
        long userId = requestService.getUserIdByToken(token);
        List<FunctionInfo> functionInfos = functionInfoService.getFunctionsByUserId(userId);
        return Result.success(functionInfos);
    }

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="功能信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "roleId") @RequestParam(defaultValue = "0", required = false, name = "roleId") long roleId) {
        List<FunctionInfo> functionInfos = functionInfoService.list(roleId);
        return Result.success(functionInfos);
    }
}
