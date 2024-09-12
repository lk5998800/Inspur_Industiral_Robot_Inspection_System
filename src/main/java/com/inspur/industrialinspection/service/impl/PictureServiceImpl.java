package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.PictureService;
import com.inspur.industrialinspection.service.RoomParamService;
import com.inspur.page.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 全局相册查询
 * @author kliu
 * @date 2022/9/24 10:10
 */
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {
    @Autowired
    private AlongWorkDao alongWorkDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    private RemoteControlTaskInstanceDao remoteControlTaskInstanceDao;
    @Autowired
    private ParticularPointInspectionTaskResultDao particularPointInspectionTaskResultDao;
    @Autowired
    private ParticularPointInspectionTaskInstanceDao particularPointInspectionTaskInstanceDao;
    @Autowired
    private RemoteControlTaskResultDao remoteControlTaskResultDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private AlongWorkDtlDao alongWorkDtlDao;
    @Autowired
    private CommonService commonService;

    @Override
    public JSONArray list(long roomId, String taskType, String startTime, String endTime, String taskName) {
        List<AlongWork> alongWorks = new ArrayList();
        List<TaskInstance> taskInstances = new ArrayList();
        List<RemoteControlTaskInstance> remoteControlTaskInstances = new ArrayList();
        List<ParticularPointInspectionTaskInstance> particularPointInspectionTaskInstances = new ArrayList();

        //随工
        if (StringUtils.isEmpty(taskType) || "alongWork".equals(taskType)){
            alongWorks = alongWorkDao.getPictureList(roomId, startTime, endTime, taskName);
        }

        JSONArray inspectTypeArr = new JSONArray();
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        if (roomParamObject.containsKey("inspect_setting")){
            JSONObject inspectSettingObejct = roomParamObject.getJSONObject("inspect_setting");
            if (inspectSettingObejct.containsKey("inspect_type")) {
                inspectTypeArr = inspectSettingObejct.getJSONArray("inspect_type");
            }
        }

        //普通巡检
        if (StringUtils.isEmpty(taskType) || "inspectTask".equals(taskType)){
            String inspectTypeArrIn = "";
            if (!StringUtils.isEmpty(taskName)){
                for (int i = 0; i < inspectTypeArr.size(); i++) {
                    JSONObject jsonObject = inspectTypeArr.getJSONObject(i);
                    String inspectTypeName = jsonObject.getStr("inspect_type_name");
                    if (inspectTypeName.indexOf(taskName)>-1){
                        inspectTypeArrIn+="'"+jsonObject.getLong("inspect_type_id")+"',";
                    }
                }
                if (StringUtils.isEmpty(inspectTypeArrIn)){
                }else{
                    if (inspectTypeArrIn.endsWith(",")){
                        inspectTypeArrIn = inspectTypeArrIn.substring(0, inspectTypeArrIn.length()-1);
                        taskInstances = taskInstanceDao.getPictureList(roomId, startTime, endTime, inspectTypeArrIn);
                    }
                }
            }else{
                taskInstances = taskInstanceDao.getPictureList(roomId, startTime, endTime, inspectTypeArrIn);
            }
        }

        //远程遥控
        if (StringUtils.isEmpty(taskType) || "remoteControl".equals(taskType)){
            if (StringUtils.isEmpty(taskName) || "远程遥控任务".indexOf(taskName) > -1){
                remoteControlTaskInstances = remoteControlTaskInstanceDao.getPictureList(roomId, startTime, endTime);
            }
        }

        //单点检测
        if (StringUtils.isEmpty(taskType) || "particularPointInspection".equals(taskType)){
            if (StringUtils.isEmpty(taskName) || "远程遥控任务单点巡检任务".indexOf(taskName) > -1){
                particularPointInspectionTaskInstances = particularPointInspectionTaskInstanceDao.getPictureList(roomId, startTime, endTime);
            }
        }


        JSONObject jsonObject;
        long instanceId;
        JSONArray jsonArray = new JSONArray();
        //远程遥控任务和单点巡检任务的实例id可能重复，此时在其前添加前缀
        //远程遥控任务中 前缀为1  单点巡检任务前缀为2  3 随工  4 普通巡检
        for (RemoteControlTaskInstance remoteControlTaskInstance : remoteControlTaskInstances) {
            jsonObject = new JSONObject();
            instanceId = remoteControlTaskInstance.getInstanceId();
            instanceId = Long.parseLong("1"+instanceId);
             startTime = remoteControlTaskInstance.getStartTime();
            jsonObject.set("taskTime", DateUtil.parse(startTime).toString("yy/MM/dd HH:mm"));
            jsonObject.set("taskType", "远程遥控任务");
            jsonObject.set("taskName", "单点巡检");
            jsonObject.set("instanceId", instanceId);
            jsonArray.add(jsonObject);
        }

        for (ParticularPointInspectionTaskInstance particularPointInspectionTaskInstance : particularPointInspectionTaskInstances) {
            jsonObject = new JSONObject();
            instanceId = particularPointInspectionTaskInstance.getInstanceId();
            instanceId = Long.parseLong("2"+instanceId);
            startTime = particularPointInspectionTaskInstance.getStartTime();
            jsonObject.set("taskTime", DateUtil.parse(startTime).toString("yy/MM/dd HH:mm"));
            jsonObject.set("taskType", "单点巡检任务");
            jsonObject.set("instanceId", instanceId);
            jsonObject.set("taskName", "单点巡检");
            jsonArray.add(jsonObject);
        }

        for (AlongWork alongWork : alongWorks) {
            jsonObject = new JSONObject();
            instanceId = alongWork.getId();
            instanceId = Long.parseLong("3"+instanceId);
            startTime = alongWork.getTaskTime();
            jsonObject.set("taskTime", DateUtil.parse(startTime).toString("yy/MM/dd HH:mm"));
            jsonObject.set("taskType", "随工任务");
            jsonObject.set("taskName", alongWork.getTaskName());
            jsonObject.set("instanceId", instanceId);
            jsonArray.add(jsonObject);
        }

        String inspectTypeName = "";
        for (TaskInstance taskInstance : taskInstances) {
            long inspectTypeId = taskInstance.getInspectTypeId();
            for (int i = 0; i < inspectTypeArr.size(); i++) {
                jsonObject = inspectTypeArr.getJSONObject(i);
                Long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
                if (inspectTypeId == inspectTypeId1){
                    inspectTypeName =jsonObject.getStr("inspect_type_name");
                    break;
                }
            }
            jsonObject = new JSONObject();
            instanceId = taskInstance.getInstanceId();
            instanceId = Long.parseLong("4"+instanceId);
            startTime = taskInstance.getStartTime();
            jsonObject.set("taskTime", DateUtil.parse(startTime).toString("yy/MM/dd HH:mm"));
            jsonObject.set("taskType", "巡检任务");
            jsonObject.set("taskName", inspectTypeName);
            jsonObject.set("instanceId", instanceId);
            jsonArray.add(jsonObject);
        }


        jsonArray.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String taskTime1 = ((JSONObject) o1).getStr("taskTime");
                String taskTime2 = ((JSONObject) o2).getStr("taskTime");
                return taskTime2.compareTo(taskTime1);
            }
        });


        return jsonArray;
    }

    @Override
    public PageBean detl(String taskType, long instanceId, int pageSize, int pageNum) {
        JSONArray jsonArray = new JSONArray();
        instanceId = Long.parseLong((""+instanceId).substring(1));
        if("单点巡检任务".equals(taskType)){
            ParticularPointInspectionTaskResult particularPointInspectionTaskResult = particularPointInspectionTaskResultDao.getDetlById(instanceId);
            if (particularPointInspectionTaskResult != null){
                String infrared = particularPointInspectionTaskResult.getInfrared();
                if (!StringUtils.isEmpty(infrared)){
                    jsonArray.add(commonService.url2Https(infrared));
                }
                if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAlarmLight())){
                    JSONArray tempArr = new JSONObject(particularPointInspectionTaskResult.getAlarmLight()).getJSONArray("path");
                    for (int i = 0; i < tempArr.size(); i++) {
                        jsonArray.add(commonService.url2Https(tempArr.getStr(i)));
                    }
                }
                if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getFront())){
                    jsonArray.add(commonService.url2Https(particularPointInspectionTaskResult.getFront()));
                }
                if (!StringUtils.isEmpty(particularPointInspectionTaskResult.getAfter())){
                    jsonArray.add(commonService.url2Https(particularPointInspectionTaskResult.getAfter()));
                }
            }
        }else if("远程遥控任务".equals(taskType)){
            List<RemoteControlTaskResult> list = remoteControlTaskResultDao.list(instanceId);
            for (RemoteControlTaskResult remoteControlTaskResult : list) {
                jsonArray.add(commonService.url2Https(remoteControlTaskResult.getImgUrl()));
            }
        }else if("随工任务".equals(taskType)){
            List<AlongWorkDtl> list = alongWorkDtlDao.list(instanceId);
            AlongWork alongWork = alongWorkDao.getDtlById(instanceId);
            String videoUrl = alongWork.getVideoUrl();
            if (!StringUtils.isEmpty(videoUrl)){
                JSONArray jsonArray1 = new JSONArray(videoUrl);
                for (int i = 0; i < jsonArray1.size(); i++) {
                    jsonArray.add(commonService.url2Https(jsonArray1.getStr(i)));
                }
            }
            for (AlongWorkDtl alongWorkDtl : list) {
                if (!StringUtils.isEmpty(alongWorkDtl.getImgUrl())){
                    jsonArray.add(commonService.url2Https(alongWorkDtl.getImgUrl()));
                }
            }
        }else if("巡检任务".equals(taskType)){
            List<TaskDetectionResult> list = taskDetectionResultDao.list(instanceId);
            for (TaskDetectionResult detectionResult : list) {
                String alarmLight = detectionResult.getAlarmLight();
                String infrared = detectionResult.getInfrared();
                String fireExtinguisher = detectionResult.getFireExtinguisher();
                if (!StringUtils.isEmpty(alarmLight)){
                    JSONObject jsonObject = new JSONObject(alarmLight);
                    if (jsonObject.containsKey("alarm_light_merge_url")){
                        String alarmLightMergeUrl = jsonObject.getStr("alarm_light_merge_url");
                        jsonArray.add(commonService.url2Https(alarmLightMergeUrl));
                    }
                }
                if (!StringUtils.isEmpty(infrared)){
                    JSONArray jsonArray1 = new JSONArray(infrared);
                    for (int i = 0; i < jsonArray1.size(); i++) {
                        JSONObject jsonObject = jsonArray1.getJSONObject(i);
                        if (jsonObject.containsKey("infrared_merge_url")){
                            String infraredMergeUrl = jsonObject.getStr("infrared_merge_url");
                            jsonArray.add(commonService.url2Https(infraredMergeUrl));
                            break;
                        }
                    }
                }
                if (!StringUtils.isEmpty(fireExtinguisher)){
                    JSONObject jsonObject = new JSONObject(fireExtinguisher);
                    JSONArray pathArr = jsonObject.getJSONArray("path");
                    for (int i = 0; i < pathArr.size(); i++) {
                        jsonArray.add(commonService.url2Https(pathArr.getStr(i)));
                    }
                }
            }
        }

        int totalSize = jsonArray.size();
        int totalPage = jsonArray.size()/pageSize;

        int listTotalSize = jsonArray.size() % pageSize;
        if (listTotalSize>0){
            totalPage++;
        }else{
            listTotalSize = pageSize;
        }

        int begin = (pageNum-1)*pageSize;
        int end = pageNum*pageSize;

        List<Object> objects = jsonArray.subList(begin, end > totalSize ? totalSize : end);

        //临时功能，在此处计算分页，不再加到查询里边
        PageBean pageBean = new PageBean();
        pageBean.setCurrentpage(pageNum);
        pageBean.setPageSize(pageSize);
        pageBean.setTotalSize(totalSize);
        pageBean.setTotalPage(totalPage);
        pageBean.setListTotalSize(listTotalSize);
        pageBean.setContentList(objects);
        return pageBean;
    }
}
