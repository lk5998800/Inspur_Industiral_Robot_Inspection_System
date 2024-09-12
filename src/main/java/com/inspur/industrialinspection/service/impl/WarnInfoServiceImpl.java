package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.entity.vo.ProportionVo;
import com.inspur.industrialinspection.entity.vo.TopInformationVo;
import com.inspur.industrialinspection.entity.vo.WarnInfoCountVo;
import com.inspur.industrialinspection.entity.vo.WarnMessageResultVo;
import com.inspur.industrialinspection.service.RobotStatusService;
import com.inspur.industrialinspection.service.RoomParamService;
import com.inspur.industrialinspection.service.WarnInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.awt.SystemColor.NUM_COLORS;
import static java.awt.SystemColor.info;

/**
 * @author: LiTan
 * @description: 报警信息服务
 * @date: 2022-10-26 09:46:06
 */
@Service
public class WarnInfoServiceImpl implements WarnInfoService {
    @Autowired
    private WarnInfoDao warnInfoDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private RobotStatusService robotStatusService;
    @Autowired
    private RoomDetectionSumDayDao roomDetectionSumDayDao;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;

    @Override
    public List<WarnMessageResultVo> getWarnInfo(long roomId) {
        TaskInstance taskInstance = taskInstanceDao.getByRoomIDNewTask(roomId);
        List<WarnMessageResultVo> collect = null;
        if (taskInstance != null) {
            List<WarnInfo> warnInfos = warnInfoDao.listByInstanceId(taskInstance.getInstanceId());
            if (warnInfos != null) {
                collect = warnInfos.stream().map(item -> {
                    WarnMessageResultVo messageResultVo = new WarnMessageResultVo();
                    messageResultVo.setName(item.getPointName().replace("-上","").replace("-下",""));
                    messageResultVo.setTime(item.getWarnTime());
                    messageResultVo.setType(item.getDetectionId());
                    messageResultVo.setStatus("预警");
                    String name = getName(item.getDetectionId());
                    messageResultVo.setTypeChi(name);
                    return messageResultVo;
                }).collect(Collectors.toList());
            }
        }
        return collect;
    }

    @Override
    public List<WarnInfoCountVo> getCountWarnInfo(long roomId) {
        TaskInstance byRoomIDNewTask = taskInstanceDao.getByRoomIDNewTask(roomId);
        List<WarnInfoCountVo> collect = null;
        if (byRoomIDNewTask != null) {
            List<WarnInfoCountVo> infoCountVos = warnInfoDao.getCountWarnInfo(byRoomIDNewTask.getInstanceId());
            if (infoCountVos != null) {
                collect = infoCountVos.stream().map(item -> {
                    WarnInfoCountVo warnInfoCountVo = new WarnInfoCountVo();
                    String name = getName(item.getName());
                    warnInfoCountVo.setName(name);
                    warnInfoCountVo.setValue(item.getValue());
                    return warnInfoCountVo;
                }).collect(Collectors.toList());
            }
        }
        return collect;
    }

    @Override
    public List getWarnInfoProportion(long roomId) {
        TaskInstance byRoomIDNewTask = taskInstanceDao.getByRoomIDNewTask(roomId);
        List<HashMap<String, List>> collect = null;
        if (byRoomIDNewTask != null) {
            List<WarnInfoCountVo> infoCountVos = warnInfoDao.getCountWarnInfo(byRoomIDNewTask.getInstanceId());
            if (infoCountVos != null) {
                collect = infoCountVos.stream().map(item -> {
                    HashMap<String, List> hashMap = new HashMap<>();
                    LinkedList<Object> linkedList = new LinkedList<>();
                    ProportionVo proportionVo = new ProportionVo();
                    ProportionVo vo = new ProportionVo();
                    proportionVo.setValue(item.getValue());
                    linkedList.add(proportionVo);
                    //让前端显示好看
                    int sumValue = (int) (item.getValue() * 0.5);
                    if (sumValue==0){
                        sumValue = 1;
                    }

                    vo.setValue(sumValue);
                    linkedList.add(vo);
                    hashMap.put(item.getName(), linkedList);
                    return hashMap;
                }).collect(Collectors.toList());
            }
        }
        return collect;
    }

    @Override
    public TopInformationVo getTopInformation(long roomId) {
        TopInformationVo topInformationVo = new TopInformationVo();
        List<TaskInfo> byRoomId = taskInfoDao.getByRoomId(roomId);

        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);

        topInformationVo.setRobotId(robotId);
        //电量
        String robotPower = robotStatusService.getRobotPower(robotId);
        topInformationVo.setPower(robotPower);

        String status = "";
        String robotStatus = robotStatusService.getRobotStatus(robotId).getStr("robotStatus");
        if ("online".equals(robotStatus)) {
            //在线
            JSONObject robotStatusWithTask = robotStatusService.getRobotStatusWithTask(robotId);
            String charging = robotStatusWithTask.getStr("charging");
            if ("充电中".equals(charging)) {
                status = "充电中";
            }
            String taskStatus = robotStatusWithTask.getStr("task_status");
            if ("是".equals(taskStatus)) {
                status = "任务中";
            }
            if (!"充电中".equals(status) && !"任务中".equals(status)) {
                status = "待命中";
            }
        } else {
            //离线
            status = "离线";
        }
        topInformationVo.setTaskStatus(status);
        //总任务
        int number = 0;
        for (TaskInfo taskInfo : byRoomId) {
            String[] split = taskInfo.getExecTime().split(",");
            number += split.length;
        }
        topInformationVo.setCountTask(number);
        String time = DateUtil.parse(DateUtil.now()).toString("yyyy-MM-dd");
        List<TaskInstance> taskInstanceByRoomIdAndDate = taskInstanceDao.getTaskInstanceByRoomIdAndDate(roomId, time + " 00:00:00", time + " 23:59:59");
        topInformationVo.setExecutedTask(taskInstanceByRoomIdAndDate.size());
        List<WarnMessageResultVo> warnInfo = getWarnInfo(roomId);
        topInformationVo.setAlarmsNumber(warnInfo.size());
        //今日最高温度
        List<RoomDetectionSumDay> roomDetectionSumDay = roomDetectionSumDayDao.getDayMaxTemperature(roomId, time, Detection.TEMPERATURE);
        if (roomDetectionSumDay != null && roomDetectionSumDay.size() >0) {
            topInformationVo.setMaxTemperature(roomDetectionSumDay.get(0).getMax());
        }

        //添加灭火器检测结果
        String fireExtinguisherCount = null;
        //当前机房配置灭火器检测，则获取最新一次任务的灭火器检测的数据，未配置灭火器检测，则返回null
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONObject inspectSettingObject = roomParamObject.getJSONObject("inspect_setting");
        JSONArray detectionCombinationArr = inspectSettingObject.getJSONArray("detection_combination");
        JSONObject detectionCombinationObject;
        boolean fireExtinguisherAbnoraml = false;
        for (int i = 0; i < detectionCombinationArr.size(); i++) {
            detectionCombinationObject = detectionCombinationArr.getJSONObject(i);
            JSONArray combinationDetlArr = detectionCombinationObject.getJSONArray("combination_detl");
            if (fireExtinguisherCount != null){
                break;
            }
            for (int j = 0; j < combinationDetlArr.size(); j++) {
                String str = combinationDetlArr.getStr(j);
                if (Detection.FIREEXTINGUISHER.equals(str)){
                    //计算最新一次盘点任务中，灭火器检测的总数量
                    TaskInstance taskInstance = taskInstanceDao.getLatestTaskInstanceByRobotId(robotId);
                    long instanceId = taskInstance.getInstanceId();
                    List<TaskDetectionResult> fireExtinguishers = taskDetectionResultDao.getFireExtinguishers(instanceId);
                    int count = 0;
                    for (TaskDetectionResult fireExtinguisher : fireExtinguishers) {
                        JSONObject jsonObject = JSONUtil.parseObj(fireExtinguisher.getFireExtinguisher());
                        count+=jsonObject.getInt("fire_extinguisher_count");
                        if (!fireExtinguisherAbnoraml){
                            String fireExtinguisherStatus = jsonObject.getStr("status", DetectionResult.NORMAL);
                            if (fireExtinguisherStatus.equals(DetectionResult.NORMAL)){
                            }else{
                                fireExtinguisherAbnoraml = true;
                            }
                        }
                    }
                    fireExtinguisherCount = count+"";
                    break;
                }
            }
        }

        topInformationVo.setFireExtinguisherCount(fireExtinguisherCount);
        topInformationVo.setFireExtinguisherAbnormal(fireExtinguisherAbnoraml);
        return topInformationVo;
    }

    /**
     * 中文名称
     *
     * @param nameId
     * @return
     */
    public String getName(String nameId) {
        String name = "";
        if ("noise".equals(nameId)) {
            name = "噪声";
        } else if ("temperature".equals(nameId)) {
            name = "温度";
        } else if ("humidity".equals(nameId)) {
            name = "湿度";
        } else if ("so2".equals(nameId)) {
            name = "二氧化硫";
        } else if ("smoke".equals(nameId)) {
            name = "烟雾";
        } else if ("pm2p5".equals(nameId)) {
            name = "pm2p5";
        } else if ("infrared".equals(nameId)) {
            name = "红外测温";
        } else if ("alarm_light".equals(nameId)) {
            name = "指示灯";
        }
        return name;
    }
}


