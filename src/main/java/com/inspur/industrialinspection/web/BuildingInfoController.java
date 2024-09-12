package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.BuildingInfo;
import com.inspur.industrialinspection.service.BuildingInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 楼栋信息
 * @author kliu
 * @date 2022/6/17 19:15
 */
@Controller
@RequestMapping(path="/industrial_robot/buildinginfo")
@Api(tags = {"楼栋"})
@Slf4j
public class BuildingInfoController {

    @Autowired
    private BuildingInfoService buildingInfoService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取楼栋信息", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(){
        return Result.success(buildingInfoService.list());
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加楼栋信息", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody BuildingInfo buildingInfo){
        buildingInfoService.add(buildingInfo);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value="修改楼栋信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody BuildingInfo buildingInfo){
        buildingInfoService.update(buildingInfo);
        return Result.success();
    }

    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value="删除楼栋信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody BuildingInfo buildingInfo){
        buildingInfoService.delete(buildingInfo);
        return Result.success();
    }
}
