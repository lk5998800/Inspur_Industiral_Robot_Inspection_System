package com.inspur.industrialinspection.web;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.inspur.config.LogConfig;
import com.inspur.cron.TaskExecuteCron;
import com.inspur.industrialinspection.service.*;
import com.inspur.industrialinspection.thread.RoomDetectionPointSumDayService;
import com.inspur.result.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * mqtt测试
 * @author kliu
 * @date 2022/6/7 16:09
 */
@RestController
@RequestMapping(path="/industrial_robot/task")
@Api(tags = {"测试"})
public class TestController {
    @Autowired
    private RoomParamService roomParamService;

    @Autowired
    private RobotParamService robotParamService;
    @Autowired
    private SendSmsService sendSmsService;
    @Autowired
    private PhoneNoticeService phoneNoticeService;

    @Autowired
    private LogConfig logConfig;
    @Autowired
    private LifterService lifterService;
    @Autowired
    private TaskExecuteCron taskExecuteCron;
    @Autowired
    private RoomDetectionPointSumDayService roomDetectionPointSumDayService;

    @GetMapping("/upload")
    @ResponseBody
    @ApiOperation(value="上传数据")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result upload() {
        String trim = FileUtil.readUtf8String("D:\\gyxj\\shangchuan.json").trim();
        Map<String, Object> map = new HashMap<String, Object>(1);
        map.put("detectionResultJson", trim);
        String post = HttpUtil.post("http://127.0.0.1:8080/industrial_robot/taskdetectionresult/receiveDetectionResult", map);
        System.out.println(post);
        return Result.success();
    }

    @GetMapping("/clearCache")
    @ResponseBody
    @ApiOperation(value="clear robot room param cache")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result clearCache(){
        roomParamService.clearRoomParamCache();
        robotParamService.clearRobotParamCache();
        return Result.success();
    }

    @GetMapping("/addNotLogMethod")
    @ResponseBody
    @ApiOperation(value="添加不记录日志方法")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result addNotLogMethod(@ApiParam(value = "方法名称") @RequestParam(defaultValue = "0", required = false, name = "method") String method){
        logConfig.addMethod(method);
        return Result.success();
    }

    @GetMapping("/removeNotLogMethod")
    @ResponseBody
    @ApiOperation(value="删除不记录日志方法")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result removeNotLogMethod(@ApiParam(value = "方法名称") @RequestParam(defaultValue = "0", required = false, name = "method") String method){
        logConfig.removeMethod(method);
        return Result.success();
    }
    @GetMapping("/lifterTest")
    @ResponseBody
    @ApiOperation(value="升降杆测试程序")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result lifterTest(@ApiParam(value = "机器人id", required = true) @RequestParam("robotId") long robotId,
                             @ApiParam(value = "升降总次数", required = true) @RequestParam("count") int count){
        lifterService.issuedLifterTask(robotId, count);
        return Result.success();
    }

    @GetMapping("/testAlarmLight")
    @ResponseBody
    @ApiOperation(value="测试指示灯")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result testAlarmLight(String jsonStr){
        taskExecuteCron.testAlarmLight(JSONUtil.parseObj(jsonStr));
        return Result.success();
    }

    @GetMapping("/sendSmsTest")
    @ResponseBody
    @ApiOperation(value="发送短信测试")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result sendSmsTest(@ApiParam(value = "手机号码", required = true) @RequestParam("phoneNumbers") String phoneNumbers,
                              @ApiParam(value = "模板", required = true) @RequestParam("templateCode") String templateCode,
                              @ApiParam(value = "模板参数", required = true) @RequestParam("templateParam") String templateParam) throws Exception {
        sendSmsService.sendSms(phoneNumbers, templateCode, templateParam);
        return Result.success();
    }

    @GetMapping("/phoneNoticeTest")
    @ResponseBody
    @ApiOperation(value="电话测试")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result phoneNoticeTest(@ApiParam(value = "手机号码", required = true) @RequestParam("calledNumber") String calledNumber,
                              @ApiParam(value = "模板", required = true) @RequestParam("ttsCode") String ttsCode,
                              @ApiParam(value = "模板参数", required = true) @RequestParam("ttsParam") String ttsParam) throws Exception {
        phoneNoticeService.phoneNotice(calledNumber, ttsCode, ttsParam);
        return Result.success();
    }

    @GetMapping("/newReportDataInit")
    @ResponseBody
    @ApiOperation(value="新版报告报表数据初始化")
    @ApiResponses(value = { @ApiResponse(code = 0, message = ""), @ApiResponse(code = 1, message = "具体失败原因")})
    public Result newReportDataInit(@ApiParam(value = "机房id", required = true) @RequestParam("roomId") long roomId){
        roomDetectionPointSumDayService.newReportDataInit(roomId);
        return Result.success();
    }
}
