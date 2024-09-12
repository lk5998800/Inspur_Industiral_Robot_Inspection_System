package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.UserInfo;
import com.inspur.industrialinspection.service.UserInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 登录用户信息
 *
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path = "/industrial_robot/userinfo")
@Api(tags = {"登录用户信息"})
@Slf4j
public class UserInfoController {

    @Autowired
    private UserInfoService userService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取管理人员信息", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list() {
        return Result.success(userService.list());
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "添加管理人员信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody UserInfo userInfo) throws Exception {
        userService.add(userInfo);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value = "修改管理人员信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody UserInfo userInfo) throws Exception {
        userService.update(userInfo);
        return Result.success();
    }

    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value = "删除管理人员信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody UserInfo userInfo) {
        userService.delete(userInfo);
        return Result.success();
    }

    @PostMapping("uploadProfile")
    @ResponseBody
    @ApiOperation(value = "上传用户头像", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result uploadProfile(@RequestParam MultipartFile multipartFile, @RequestParam long userId) throws Exception {
        userService.saveFrofile(multipartFile, userId);
        return Result.success();
    }

    @PostMapping("changePwd")
    @ResponseBody
    @ApiOperation(value = "修改密码", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result changePwd(@RequestBody JSONObject pwdObject) {
        userService.changePwd(pwdObject.getLong("userId"), pwdObject.getStr("userPwdOrigin"), pwdObject.getStr("userPwdNew"));
        return Result.success();
    }

    @GetMapping("/getByRobotIdPersonList")
    @ResponseBody
    @ApiOperation(value = "根据机器人唯一标识获取人员列表", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getByRobotIdPersonList(@RequestParam("robotId") long robotId) {
       List<UserInfo> userInfos =  userService.getByRobotIdPersonList(robotId);
        return Result.success(userInfos);
    }
}
