package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.MapPara;
import com.inspur.industrialinspection.service.MapParaService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 地图参数
 * @author kliu
 * @date 2022/11/21 9:20
 */
@Controller
@RequestMapping(path="/industrial_robot/mappara")
@Api(tags = {"地图参数"})
@Slf4j
public class MapParaController {

    @Autowired
    private MapParaService mapParaService;

    @GetMapping("/getByRoomId")
    @ResponseBody
    @ApiOperation(value="获取", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getByRoomId(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        return Result.success(mapParaService.getByRoomId(roomId));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@RequestBody MapPara mapPara) {
        mapParaService.add(mapPara);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value="更新", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@RequestBody MapPara mapPara) {
        mapParaService.update(mapPara);
        return Result.success();
    }
    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value="删除", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(long roomId) {
        mapParaService.delete(roomId);
        return Result.success();
    }
}
