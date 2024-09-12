package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.ItAsset;
import com.inspur.industrialinspection.service.ItAssetService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author kliu
 * @description 资产信息controller
 * @date 2022/4/28 8:29
 */
@Controller
@RequestMapping(path="/industrial_robot/itasset")
@Api(tags = {"资产信息"})
@Slf4j
public class ItAssetController {

    @Autowired
    private ItAssetService itAssetService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取资产信息", notes="无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(ItAsset itAsset,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum){
        return Result.success(itAssetService.list(itAsset, pageSize, pageNum));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加资产信息", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody @Validated ItAsset itAsset){
        itAssetService.add(itAsset);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value="修改资产信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody @Validated ItAsset itAsset){
        itAssetService.update(itAsset);
        return Result.success();
    }

    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value="删除资产信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody ItAsset itAsset){
        itAssetService.delete(itAsset);
        return Result.success();
    }

    @GetMapping("/getItAssetCode")
    @ResponseBody
    @ApiOperation(value="获取资产所需code信息", notes="无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getItAssetCode(){
        return Result.success(itAssetService.getItAssetCode());
    }
}
