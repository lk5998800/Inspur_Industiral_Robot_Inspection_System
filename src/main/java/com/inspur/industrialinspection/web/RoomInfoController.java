package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.service.RoomInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 机房信息
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/room")
@Api(tags = {"机房"})
@Slf4j
public class RoomInfoController {

    @Autowired
    private RoomInfoService roomService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取机房信息", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                       @ApiParam(value = "楼栋id") @RequestParam(defaultValue = "0", required = false, name = "buildingId") long buildingId) {
        return Result.success(roomService.list(robotId, buildingId));
    }

    @GetMapping("/listWithoutToken")
    @ResponseBody
    @ApiOperation(value="获取机房信息-不需要token", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result listWithoutToken(@ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                                   @ApiParam(value = "园区id") @RequestParam(defaultValue = "0", required = false, name = "parkId") int parkId) {
        return Result.success(roomService.listWithoutToken(robotId, parkId));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加机房信息", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody @Validated RoomInfo roomInfo) {
        roomService.add(roomInfo);
        return Result.success("");
    }
    @PostMapping("/addFile")
    @ResponseBody
    @ApiOperation(value="添加机房信息(带图片)", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(RoomInfo roomInfo, MultipartFile file) {
        roomService.add(roomInfo,file);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value="修改机房信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody @Validated RoomInfo roomInfo) {
        roomService.update(roomInfo);
        return Result.success();
    }
    @PostMapping("updateFile")
    @ResponseBody
    @ApiOperation(value="修改机房信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(RoomInfo roomInfo, MultipartFile file) {
        roomService.update(roomInfo,file);
        return Result.success();
    }

    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value="删除机房信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody RoomInfo roomInfo) {
        roomService.delete(roomInfo);
        return Result.success();
    }

    @GetMapping("/roomRobotUserList")
    @ResponseBody
    @ApiOperation(value="获取机房机器人用户关联信息", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result roomRobotUserList() {
        return Result.success(roomService.roomRobotUserList());
    }
}
