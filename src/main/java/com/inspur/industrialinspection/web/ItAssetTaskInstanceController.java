package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import com.inspur.industrialinspection.service.ItAssetTaskInstanceService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 任务记录
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Controller
@RequestMapping(path="/industrial_robot/itassettaskinstance")
@Api(tags = "资产盘点")
@Slf4j
public class ItAssetTaskInstanceController {

    @Autowired
    private ItAssetTaskInstanceService itAssetTaskInstanceService;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value="获取任务执行记录")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result list(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                       @ApiParam(value = "机器人id") @RequestParam(defaultValue = "0", required = false, name = "robotId") long robotId,
                       @ApiParam(value = "任务名称") @RequestParam(required = false, name = "taskName") String taskName,
                       @ApiParam(value = "机柜行") @RequestParam(required = false, name = "cabinetRow") String cabinetRow,
                       @ApiParam(value = "机柜列") @RequestParam(defaultValue = "0", required = false, name = "cabinetColumn") long cabinetColumn,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum) {
        return Result.success(itAssetTaskInstanceService.list(roomId, robotId, taskName, cabinetRow, cabinetColumn, pageSize, pageNum));
    }

    @PostMapping("/terminate")
    @ResponseBody
    @ApiOperation(value="终止任务", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result terminate(@RequestBody ItAssetTaskInstance itAssetTaskInstance){
        itAssetTaskInstanceService.terminate(itAssetTaskInstance);
        return Result.success("");
    }

    @GetMapping("/instanceDetlList")
    @ResponseBody
    @ApiOperation(value="资产盘点任务执行明细信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result instanceDetlList(@ApiParam(value = "机柜行") @RequestParam(required = false, name = "cabinetRow") String cabinetRow,
                                   @ApiParam(value = "机柜列") @RequestParam(defaultValue = "0", required = false, name = "cabinetColumn") long cabinetColumn,
                                   @ApiParam(value = "资产编号") @RequestParam(required = false, name = "assetNo") String assetNo,
                                   @ApiParam(value = "资产名称") @RequestParam(required = false, name = "assetName") String assetName,
                                   @ApiParam(value = "负责人") @RequestParam(defaultValue = "0", required = false, name = "person_in_charge_id") long personInChargeId,
                                   @ApiParam(value = "实例id", required = true) @RequestParam("instanceId") long instanceId,
                                   @ApiParam(value = "数据类型") @RequestParam(required = false, name = "dataType") String dataType,
                                   @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                                   @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum){
        return Result.success(itAssetTaskInstanceService.instanceDetlList(cabinetRow, cabinetColumn, assetNo, assetName, personInChargeId, instanceId, dataType, pageSize, pageNum));
    }

    @PostMapping("batchDelete")
    @ResponseBody
    @ApiOperation(value="批量删除任务执行信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result batchDelete(@RequestBody JSONArray batchDeleteArr){
        itAssetTaskInstanceService.batchDelete(batchDeleteArr);
        return Result.success();
    }
}
