package com.inspur.industrialinspection.web;

import com.inspur.industrialinspection.service.CabinetPicService;
import com.inspur.result.Result;
import io.minio.errors.*;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 机柜照片
 * @author kliu
 * @date 2022/6/17 19:15
 */
@Controller
@RequestMapping(path="/industrial_robot/cabinetpic")
@Api(tags = {"机柜照片"})
@Slf4j
public class CabinetPicController {

    @Autowired
    private CabinetPicService cabinetPicService;

    @GetMapping("/createPicFile")
    @ResponseBody
    @ApiOperation(value="生成机柜照片", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result createPicFile(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId, HttpServletResponse response) throws Exception {
        cabinetPicService.createPicFile(roomId, response);
        return Result.success();
    }

    @GetMapping("/createPicFileWithRedlight")
    @ResponseBody
    @ApiOperation(value="生成机柜检测为红色指示灯的照片", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result createPicFileWithRedlight(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId, HttpServletResponse response) throws Exception {
        cabinetPicService.createPicFileWithRedlight(roomId, response);
        return Result.success();
    }

    @GetMapping("/createInfraredPicFile")
    @ResponseBody
    @ApiOperation(value="生成红外相机图片", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result createInfraredPicFile(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId, HttpServletResponse response) throws Exception {
        cabinetPicService.createInfraredPicFile(roomId, response);
        return Result.success();
    }

    @GetMapping("/createInfraredPicFileTranslate")
    @ResponseBody
    @ApiOperation(value="生成红外相机图片转换后的", notes="无入参信息")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result createInfraredPicFileTranslate(@ApiParam(value = "机房id") @RequestParam(defaultValue = "0", required = false, name = "roomId") long roomId, HttpServletResponse response) throws Exception {
        cabinetPicService.createInfraredPicFileTranslate(roomId, response);
        return Result.success();
    }
}
