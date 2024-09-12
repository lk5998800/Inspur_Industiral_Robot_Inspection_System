package com.inspur.cron;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.service.RobotParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 更改机器人参数定时任务-仅修改实时视频参数，用于232环境，手机监控使用
 * @author kliu
 * @date 2022/7/23 13:49
 */
@Component
@Slf4j
public class ChangeRobotParamCron {
    @Autowired
    private RobotParamService robotParamService;

    @Value("${everyday.changeRobotParam.robotId}")
    private String robotIdStr;

    /**
     * 定时执行逻辑 每天早8点 下午6点
     * @return void
     * @author kliu
     * @date 2022/7/23 13:51
     */
    @Scheduled(cron = "0 0 8,18 * * ?")
    public void execute() throws IOException {
        String[] split = robotIdStr.split(",");
        long robotId;
        JSONObject robotParamObejct;
        for (String s : split) {
            robotId = Long.valueOf(s);
            if (robotId>0){
                //周一到周五 早8点到18点往232推流，其余时间往云端推流
                robotParamObejct = robotParamService.getRobotParam(robotId);
                int weekDay = DateUtil.dayOfWeek(DateUtil.date()) - 1 ;
                if (weekDay>=1 && weekDay<=5){
                    int hour = DateUtil.hour(DateUtil.date(), true);
                    if (hour == 8){
                        robotParamObejct.set("srs_webrtc_server_url", "webrtc://10.180.151.232/realtimestream");
                        robotParamObejct.set("srs_rtmp_server_url", "rtmp://10.180.151.232/realtimestream");
                    }else if (hour == 18){
                        robotParamObejct.set("srs_webrtc_server_url", "webrtc://59.110.25.180/realtimestream");
                        robotParamObejct.set("srs_rtmp_server_url", "rtmp://59.110.25.180/realtimestream");
                    }
                }
                robotParamService.add(robotId, robotParamObejct.toString());
            }
        }
    }
}
