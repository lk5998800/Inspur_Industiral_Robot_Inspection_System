package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.RoleInfo;
import com.inspur.industrialinspection.service.RoleInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 角色信息
 *
 * @author wangzhaodi
 * @date 2022/11/15 9:22
 */
@Controller
@RequestMapping(path = "/industrial_robot/roleinfo")
@Api(tags = {"角色信息"})
@Slf4j
public class RoleInfoController {

    @Autowired
    private RoleInfoService roleService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取角色信息", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "userId") @RequestParam(defaultValue = "0", required = false, name = "userId") long userId) {
        return Result.success(roleService.list(userId));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "添加角色信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody JSONObject jsonObject) {
        roleService.add(jsonObject);
        return Result.success("");
    }

    @PostMapping("/adduserRoles")
    @ResponseBody
    @ApiOperation(value = "关联用户角色", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result adduserRoles(@RequestBody JSONObject jsonObject) {
        roleService.addUserRoles(jsonObject);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value = "修改角色信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody JSONObject jsonObject) {
        roleService.update(jsonObject);
        return Result.success();
    }

    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value = "删除角色信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody RoleInfo roleInfo) {
        roleService.delete(roleInfo);
        return Result.success();
    }

}
