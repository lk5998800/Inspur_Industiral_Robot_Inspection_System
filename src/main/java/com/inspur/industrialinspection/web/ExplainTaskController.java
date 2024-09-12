package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.entity.ExplainPointSkill;
import com.inspur.industrialinspection.entity.ExplainTask;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.service.ExplainTaskService;
import com.inspur.page.PageBean;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


/**
 * @author: LiTan
 * @description: 导览讲解
 * @date: 2022-10-31 10:18:03
 */
@RequestMapping(path = "/industrial_robot/explain_task")
@RestController
@Api(tags = "导览讲解")
public class ExplainTaskController {

    @Autowired
    ExplainTaskService explainTaskService;

    @GetMapping("/getExplainTask")
    @ApiOperation(value = "获取导览讲解任务详细")
    @ApiResponses(value = {@ApiResponse(code = 0, message = "成功"), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getExplainTask(@ApiParam(value = "讲解id", required = true) @RequestParam("id") long id) {
        ExplainTask explainTask = explainTaskService.getExplainTask(id);
        return Result.success(explainTask);
    }

    @GetMapping("/pageList")
    @ResponseBody
    @ApiOperation(value = "获取导览讲解任务列表", notes = "无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result pageList(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,
                           @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                           @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum,
                           @ApiParam(value = "当前状态") @RequestParam(required = false, name = "status") String status,
                           @ApiParam(value = "任务时间") @RequestParam(required = false, name = "taskTime") String taskTime,
                           @ApiParam(value = "任务名称") @RequestParam(required = false, name = "keyword") String keyword) throws Exception {
        PageBean pageBean = explainTaskService.pageList(roomId, pageSize, pageNum, status, taskTime, keyword);
        return Result.success(pageBean);
    }

    @PostMapping("/addOrUpdate")
    @ResponseBody
    @ApiOperation(value = "添加或修改导览讲解任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result addOrUpdate(@RequestBody @Validated ExplainTask explainTask) {
        explainTaskService.addOrUpdate(explainTask);
        return Result.success();
    }

    @DeleteMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除导览讲解任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@ApiParam(value = "主键id", required = true) @RequestParam("id") int id) {
        explainTaskService.delete(id);
        return Result.success();
    }

    @GetMapping("/startTask")
    @ResponseBody
    @ApiOperation(value = "开始导览讲解任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result startTask(@ApiParam(value = "主键id", required = true) @RequestParam("id") long id) throws IOException {
        explainTaskService.startTask(id);
        return Result.success();
    }


    @GetMapping("/endTask")
    @ResponseBody
    @ApiOperation(value = "结束任务", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result endTask(@ApiParam(value = "主键id", required = true) @RequestParam("id") long id) {
        explainTaskService.endTask(id);
        return Result.success();
    }


    @GetMapping("/getExplainPointInfos")
    @ResponseBody
    @ApiOperation(value = "获取机房下的导览讲解点位信息", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getAlongWorkPointInfos(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        return Result.success(explainTaskService.getExplainPointInfos(roomId));
    }
    @GetMapping("/getExplainPointInfoStatus")
    @ResponseBody
    @ApiOperation(value = "获取机房下的导览讲解点位关联状态", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getExplainPointInfoStatus(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        return Result.success(explainTaskService.getExplainPointInfoStatus(roomId));
    }
    @PostMapping("/associatedExplainPoint")
    @ResponseBody
    @ApiOperation(value="关联导览点")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result associatedExplainPoint(@RequestBody PointInfo pointInfo) throws InterruptedException {
        explainTaskService.associatedExplainPoint(pointInfo);
        return Result.success();
    }

    @GetMapping("/getExplainPointSkills")
    @ResponseBody
    @ApiOperation(value = "获取机房下所有导览讲解点位技能", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getExplainPointSkills(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId) {
        return Result.success(explainTaskService.getExplainPointSkills(roomId));
    }

    @GetMapping("/getExplainPointSkill")
    @ResponseBody
    @ApiOperation(value = "获取机房下单个导览讲解点位技能", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getExplainPointSkill(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId,@ApiParam(value = "点位名称") @RequestParam(defaultValue = "0", required = false, name = "pointName") String pointName) {
        return Result.success(explainTaskService.getExplainPointSkill(roomId,pointName));
    }
    @PostMapping("/addOrUpdateExplainSkill")
    @ResponseBody
    @ApiOperation(value = "添加或修改导览讲解点位技能", notes = "")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result addOrUpdateExplainSkill(@RequestBody @Validated ExplainPointSkill explainPointSkill) {
        explainTaskService.addOrUpdateExplainSkill(explainPointSkill);
        return Result.success();
    }
}
