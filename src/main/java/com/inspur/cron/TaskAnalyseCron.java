package com.inspur.cron;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import com.inspur.industrialinspection.thread.AnalyseDetectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 检测项结果分析
 * @author kliu
 * @date 2022/5/12 9:50
 */
@Component
@Slf4j
public class TaskAnalyseCron {

    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;

    /**
     * 任务分析定时任务，每10分钟执行1次
     * @author kliu
     * @date 2022/5/24 17:51
     */
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void taskAnalyseCron() {
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.unAnalyseList();
        JSONObject jsonObject;
        JSONArray jsonArray;
        for (TaskDetectionResult taskDetectionResult : taskDetectionResults) {
            boolean sensorAnalyseFlag;
            boolean infraredAnalyseFlag;
            boolean alarmLightAnalyseFlag;
            String sensor = taskDetectionResult.getSensor();
            String infrared = taskDetectionResult.getInfrared();
            String alarmLight = taskDetectionResult.getAlarmLight();
            if (StringUtils.isEmpty(sensor)) {
                sensorAnalyseFlag = true;
            }else{
                jsonObject = JSONUtil.parseObj(sensor);
                Object temperature = jsonObject.get("temperature");
                //该数据未处理
                if (temperature instanceof Number) {
                    AnalyseDetectionResult.queue.offer(taskDetectionResult);
                    continue;
                } else {
                    sensorAnalyseFlag = true;
                }
            }

            if (StringUtils.isEmpty(alarmLight)) {
                alarmLightAnalyseFlag = true;
            }else{
                jsonObject = JSONUtil.parseObj(alarmLight);
                if(jsonObject.containsKey("status")){
                    alarmLightAnalyseFlag = true;
                }else{
                    AnalyseDetectionResult.queue.offer(taskDetectionResult);
                    continue;
                }
            }

            if (StringUtils.isEmpty(infrared)) {
                infraredAnalyseFlag = true;
            }else{
                infraredAnalyseFlag = true;
                jsonArray = JSONUtil.parseArray(infrared);
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    //包含status说明已比对完成
                    if (!jsonObject.containsKey("status")) {
                        infraredAnalyseFlag = false;
                        AnalyseDetectionResult.queue.offer(taskDetectionResult);
                        break;
                    }
                }
            }

            if(sensorAnalyseFlag && alarmLightAnalyseFlag && infraredAnalyseFlag){
                TaskDetectionResult nowDbTaskDetectionResult = taskDetectionResultDao.getDetlByInstanceIdAndPointNameForUpdate(taskDetectionResult.getInstanceId(), taskDetectionResult.getPointName());
                String nowDbInfrared = nowDbTaskDetectionResult.getInfrared();
                if (!StringUtils.isEmpty(nowDbInfrared)){
                    if(!nowDbInfrared.equals(infrared)){
                        throw new RuntimeException("定时任务分析检测项结果出现并发错误，将丢弃该次数据，等待下一次定时任务重新调用");
                    }
                }
                taskDetectionResultDao.updateAnalyseComplete(taskDetectionResult);
            }
        }
    }
}
