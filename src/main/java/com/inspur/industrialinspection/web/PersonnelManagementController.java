package com.inspur.industrialinspection.web;

import cn.hutool.json.JSONArray;
import com.inspur.industrialinspection.entity.PersonnelManagement;
import com.inspur.industrialinspection.service.PersonnelManagementService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 人员管理
 * @author kliu
 * @date 2022/7/21 16:46
 */
@Controller
@RequestMapping(path="/industrial_robot/personnelmanagement")
@Api(tags = {"人员管理"})
@Slf4j
public class PersonnelManagementController {

    @Autowired
    private PersonnelManagementService personnelManagementService;

    @GetMapping("/getOverView")
    @ResponseBody
    @ApiOperation(value="获取人员信息概览", notes="无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getOverView(){
        return Result.success(personnelManagementService.getOverView());
    }

    @GetMapping("/pageList")
    @ResponseBody
    @ApiOperation(value="获取人员信息", notes="无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result pageList(PersonnelManagement personnelManagement,
                       @ApiParam(value = "每页大小", required = true) @RequestParam("pageSize") int pageSize,
                       @ApiParam(value = "当前页数", required = true) @RequestParam("pageNum") int pageNum){
        return Result.success(personnelManagementService.pageList(personnelManagement, pageSize, pageNum));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value="添加人员信息", notes="")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result add(@Validated PersonnelManagement personnelManagement, MultipartFile file) throws Exception {
        personnelManagementService.add(personnelManagement, file);
        return Result.success("");
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value="修改人员信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result update(@Validated PersonnelManagement personnelManagement, MultipartFile file) throws Exception {
        personnelManagementService.update(personnelManagement, file);
        return Result.success();
    }

    @PostMapping("delete")
    @ResponseBody
    @ApiOperation(value="删除人员信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result delete(@RequestBody PersonnelManagement personnelManagement){
        personnelManagementService.delete(personnelManagement);
        return Result.success();
    }

    @PostMapping("batchDelete")
    @ResponseBody
    @ApiOperation(value="批量删除人员信息", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result batchDelete(@RequestBody JSONArray batchDeleteArr){
        personnelManagementService.batchDelete(batchDeleteArr);
        return Result.success();
    }

    @PostMapping("uploadProfile")
    @ResponseBody
    @ApiOperation(value="上传用户头像", notes="")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result uploadProfile(@RequestParam MultipartFile multipartFile, @RequestParam long userId) throws Exception {
        personnelManagementService.saveFrofile(multipartFile, userId);
        return Result.success();
    }

    @GetMapping("/getDistinctPersonnelDepartment")
    @ResponseBody
    @ApiOperation(value="获取部门信息", notes="无入参信息")
    @ApiResponses(value = {@ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result getDistinctPersonnelDepartment(){
        return Result.success(personnelManagementService.getDistinctPersonnelDepartment());
    }
}
