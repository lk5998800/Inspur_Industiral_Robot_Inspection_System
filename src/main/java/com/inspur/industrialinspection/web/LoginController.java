package com.inspur.industrialinspection.web;

import com.alibaba.fastjson.JSONObject;
import com.inspur.industrialinspection.entity.UserInfo;
import com.inspur.industrialinspection.service.JwtService;
import com.inspur.industrialinspection.service.UserInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 登录
 * @author kliu
 * @date 2022/6/7 16:09
 */
@Controller
@RequestMapping(path="/industrial_robot/login")
@Api(tags = {"登录"})
@Slf4j
public class LoginController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    @ResponseBody
    @ApiOperation(value="登录")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result login(@RequestBody UserInfo userInfo, HttpServletResponse response) throws Exception {
        UserInfo tempUser = userInfoService.checkPwdReturnUserInfo(userInfo);
        tempUser.setFacialFeature("");
        String token = jwtService.createToken(JSONObject.toJSONString(tempUser));
        response.setHeader("USER_LOGIN_TOKEN",token);
        tempUser.setToken(token);
        return Result.success(tempUser);
    }
}
