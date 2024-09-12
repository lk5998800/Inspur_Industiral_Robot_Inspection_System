package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.industrialinspection.service.ItAssetTaskInfoService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author kliu
 * @description 资产盘点任务controller
 * @date 2022/4/28 8:29
 */
@Controller
@RequestMapping(path="/industrial_robot/itassettaskinfo")
@Api(tags = {"资产盘点"})
@Slf4j
public class ItAssetTaskInfoController {

    @Autowired
    private ItAssetTaskInfoService itAssetTaskInfoService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取列表", notes="无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(ItAssetTaskInfo itAssetTaskInfo,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum){
        return Result.success(itAssetTaskInfoService.list(itAssetTaskInfo, pageNum, pageSize));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody @Validated ItAssetTaskInfo itAssetTaskInfo){
        itAssetTaskInfoService.add(itAssetTaskInfo);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value="修改", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody @Validated ItAssetTaskInfo itAssetTaskInfo){
        itAssetTaskInfoService.update(itAssetTaskInfo);
        return Result.success();
    }

    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value="删除", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody ItAssetTaskInfo itAssetTaskInfo){
        itAssetTaskInfoService.delete(itAssetTaskInfo);
        return Result.success();
    }
    @PostMapping("batchDelete")
    @ResponseBody
    @ApiOperation(value="批量删除任务信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result batchDelete(@RequestBody JSONArray batchDeleteArr){
        itAssetTaskInfoService.batchDelete(batchDeleteArr);
        return Result.success();
    }
}
