package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.InspectType;
import com.inspur.industrialinspection.service.InspectTypeService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
/**
 * 巡检类型
 * @author kliu
 * @date 2022/6/7 16:08
 */
@Controller
@RequestMapping(path="/industrial_robot/inspecttype")
@Api(tags = "巡检类型")
public class InspectTypeController {

    @Autowired
    private InspectTypeService inspectTypeService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取巡检类型")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId) throws Exception {
        return Result.success(inspectTypeService.list(roomId));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="创建巡检类型", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody InspectType inspectType) throws Exception {
        inspectTypeService.add(inspectType);
        return Result.success("");
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation(value="更新巡检类型", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody InspectType inspectType) throws Exception {
        inspectTypeService.update(inspectType);
        return Result.success("");
    }
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value="删除巡检类型", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody InspectType inspectType) throws Exception {
        inspectTypeService.delete(inspectType);
        return Result.success("");
    }
}
