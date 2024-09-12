package com.inspur.cron;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.dao.TaskDetectionResultErrorBackDao;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ai分析错误数据转移至历史表
 * @author kliu
 * @date 2022/8/6 9:53
 */
@Component
@Slf4j
public class AiAnalyseFailToTableCron {
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private TaskDetectionResultErrorBackDao taskDetectionResultErrorBackDao;

    /**
     * 每6小时执行一次，将调用AIserviceAgent失败的数据，转移至历史表，防止一直调用服务
     * @author kliu
     * @date 2022/8/6 9:56
     */
    @Scheduled(cron = "0 0 0/6 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void taskExecute() {
        //取12小时前的时间
        String hourBefore6 = DateUtil.offsetHour(DateUtil.date(), -6).toString("yyyy-MM-dd HH:mm:ss");
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.unAnalyseList();
        JSONObject jsonObject;
        JSONArray jsonArray;
        for (TaskDetectionResult taskDetectionResult : taskDetectionResults) {
            String updateTime = taskDetectionResult.getUpdateTime();
            if (updateTime.compareTo(hourBefore6) < 0) {
                long taskLogId = taskDetectionResult.getTaskLogId();

                String sensor = taskDetectionResult.getSensor();
                String infrared = taskDetectionResult.getInfrared();
                String alarmLight = taskDetectionResult.getAlarmLight();

                //传感器不为空，其余为空，过滤即可，此时认为出问题需要人工介入
                if (!StringUtils.isEmpty(sensor) && StringUtils.isEmpty(infrared) && StringUtils.isEmpty(alarmLight)){
                    continue;
                }

                if(!taskDetectionResultErrorBackDao.checkExist(taskLogId)){
                    taskDetectionResultErrorBackDao.add(taskLogId);
                }

                if (!StringUtils.isEmpty(sensor)) {
                    jsonObject = JSONUtil.parseObj(sensor);
                    Object temperature = jsonObject.get("temperature");
                    //该数据未处理
                    if (temperature instanceof Number) {
                        throw new RuntimeException("传感器数据不允许有未分析的数据存在");
                    }
                }

                if (!StringUtils.isEmpty(alarmLight)) {
                    jsonObject = JSONUtil.parseObj(alarmLight);
                    if(!jsonObject.containsKey("status")){
                        taskDetectionResult.setAlarmLight(null);
                        taskDetectionResultDao.updateAlarmLight(taskDetectionResult);
                    }
                }

                if (!StringUtils.isEmpty(infrared)) {
                    jsonArray = JSONUtil.parseArray(infrared);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        //包含status说明已比对完成
                        if (!jsonObject.containsKey("status")) {
                            jsonArray.remove(i);
                            i--;
                        }
                    }

                    taskDetectionResult.setInfrared(jsonArray.toString());
                    taskDetectionResultDao.updateInfrared(taskDetectionResult);
                }
            }
        }
    }
}
