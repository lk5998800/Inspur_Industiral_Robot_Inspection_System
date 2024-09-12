package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.gating.TreeNode;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.DetectionReportService;
import com.inspur.industrialinspection.service.PointTreeNodeService;
import com.inspur.industrialinspection.service.RoomParamService;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 检测报告服务
 * @author kliu
 * @date 2022/5/25 8:47
 */
@Service
public class DetectionReportServiceImpl implements DetectionReportService {
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private RoomDetectionSumDayDao roomDetectionSumDayDao;
    @Autowired
    private RoomDetectionPointSumDayDao roomDetectionSumPointDayDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private WarnInfoDao warnInfoDao;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ItAssetTaskInstanceDao itAssetTaskInstanceDao;
    @Autowired
    private ItAssetDao itAssetDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private RemoteControlTaskInstanceDao remoteControlTaskInstanceDao;
    @Autowired
    private AlongWorkDao alongWorkDao;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    private AlongWorkPedestrianDetectionAlarmDao alongWorkPedestrianDetectionAlarmDao;
    @Autowired
    private PersonnelManagementDao personnelManagementDao;
    @Autowired
    private PointTreeNodeService pointTreeNodeService;
    @Autowired
    private CabinetUbitDao cabinetUbitDao;

    @Override
    public HashMap getTodayReportOverview(long roomId) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getReportOverview(roomId, currentDateStr);
    }

    @Override
    public JSONArray getTodayReportWarnType(long roomId) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getReportWarnType(roomId, currentDateStr);
    }
    @Override
    public JSONArray getRecent7DaysReportWarnType(long roomId) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getReportWarnType(roomId, day6Before);
    }
    @Override
    public JSONArray getRecent30DaysReportWarnType(long roomId) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getReportWarnType(roomId, day29Before);
    }
    @Override
    public JSONArray getTodayReportCabinetStatus(long roomId) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getReportCabinetStatus(roomId, currentDateStr);
    }
    @Override
    public JSONArray getRecent7DaysReportCabinetStatus(long roomId) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getReportCabinetStatus(roomId, day6Before);
    }
    @Override
    public JSONArray getRecent30DaysReportCabinetStatus(long roomId) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getReportCabinetStatus(roomId, day29Before);
    }

    @Override
    public JSONArray getTodayReportTaskType(long roomId) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getReportTaskType(roomId, currentDateStr);
    }
    @Override
    public JSONArray getRecent7DaysReportTaskType(long roomId) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getReportTaskType(roomId, day6Before);
    }
    @Override
    public JSONArray getRecent30DaysReportTaskType(long roomId) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getReportTaskType(roomId, day29Before);
    }
    @Override
    public JSONArray getTodayReportWorkTime(long roomId) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getReportWorkTime(roomId, currentDateStr);
    }
    @Override
    public JSONArray getRecent7DaysReportWorkTime(long roomId) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getReportWorkTime(roomId, day6Before);
    }
    @Override
    public JSONArray getRecent30DaysReportWorkTime(long roomId) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getReportWorkTime(roomId, day29Before);
    }

    @Override
    public HashMap getRecent7DaysReportOverview(long roomId) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getReportOverview(roomId, day6Before);
    }

    @Override
    public HashMap getRecent30DaysReportOverview(long roomId) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getReportOverview(roomId, day29Before);
    }

    /**
     * 任务类型统计
     * @param roomId
     * @param dateStr
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 10:12
     */
    private JSONArray getReportTaskType(long roomId, String dateStr){
        int taskInstanceCount = taskInstanceDao.countByRoomIdAndDate(roomId, dateStr);
        int alongWorkCount = alongWorkDao.countByRoomIdAndDate(roomId, dateStr);
        int remoteControlCount = remoteControlTaskInstanceDao.countByRoomIdAndDate(roomId, dateStr);
        int itAssetTaskCount = itAssetTaskInstanceDao.countByRoomIdAndDate(roomId, dateStr);

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        if (taskInstanceCount>0){
            jsonObject = new JSONObject();
            jsonObject.set("name", "巡检任务次数");
            jsonObject.set("value", taskInstanceCount);
            jsonArray.add(jsonObject);
        }

        if (alongWorkCount>0){
            jsonObject = new JSONObject();
            jsonObject.set("name", "随工任务次数");
            jsonObject.set("value", alongWorkCount);
            jsonArray.add(jsonObject);
        }

        if (itAssetTaskCount>0) {
            jsonObject = new JSONObject();
            jsonObject.set("name", "盘点任务次数");
            jsonObject.set("value", itAssetTaskCount);
            jsonArray.add(jsonObject);
        }

        if (remoteControlCount>0) {
            jsonObject = new JSONObject();
            jsonObject.set("name", "遥控任务次数");
            jsonObject.set("value", remoteControlCount);
            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }

    /**
     * 工作时长统计
     * @param roomId
     * @param dateStr
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 10:12
     */
    private JSONArray getReportWorkTime(long roomId, String dateStr){
        List<TaskInstance> taskInstances = taskInfoDao.listByRoomIdAndDate(roomId, dateStr);
        List<AlongWork> alongWorks = alongWorkDao.listByRoomIdAndDate(roomId, dateStr);
        List<RemoteControlTaskInstance> remoteControlTaskInstances = remoteControlTaskInstanceDao.listByRoomIdAndDate(roomId, dateStr);
        List<ItAssetTaskInstance> itAssetTaskInstances = itAssetTaskInstanceDao.listByRoomIdAndDate(roomId, dateStr);

        JSONArray jsonArray = new JSONArray();
        Long taskWorkTime = 0L;
        Long alongWorkTime = 0L;
        Long remoteControlTaskTime = 0L;
        Long itAssetTaskWorkTime = 0L;

        String startTime;
        String endTime;
        for (TaskInstance taskInstance : taskInstances) {
            startTime = taskInstance.getStartTime();
            endTime = taskInstance.getEndTime();
            if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
                taskWorkTime += DateUtil.parseDateTime(endTime).getTime() - DateUtil.parseDateTime(startTime).getTime();
            }
        }

        for (AlongWork alongWork : alongWorks) {
            startTime = alongWork.getStartTime();
            endTime = alongWork.getEndTime();
            if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
                alongWorkTime += DateUtil.parseDateTime(endTime).getTime() - DateUtil.parseDateTime(startTime).getTime();
            }
        }

        for (RemoteControlTaskInstance remoteControlTaskInstance : remoteControlTaskInstances) {
            startTime = remoteControlTaskInstance.getStartTime();
            endTime = remoteControlTaskInstance.getEndTime();
            if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
                remoteControlTaskTime += DateUtil.parseDateTime(endTime).getTime() - DateUtil.parseDateTime(startTime).getTime();
            }
        }

        for (ItAssetTaskInstance itAssetTaskInstance : itAssetTaskInstances) {
            startTime = itAssetTaskInstance.getStartTime()+":00";
            endTime = itAssetTaskInstance.getEndTime()+":00";
            if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)){
                itAssetTaskWorkTime += DateUtil.parseDateTime(endTime).getTime() - DateUtil.parseDateTime(startTime).getTime();
            }
        }


        JSONObject jsonObject;

        if (taskWorkTime>0){
            jsonObject = new JSONObject();
            jsonObject.set("name","巡检时长");
            jsonObject.set("value", commonService.getFormatValue((double)(taskWorkTime / 60000) / 60));
            jsonArray.add(jsonObject);
        }

        if (alongWorkTime>0) {
            jsonObject = new JSONObject();
            jsonObject.set("name","随工时长");
            jsonObject.set("value", commonService.getFormatValue((double)(alongWorkTime / 60000) / 60));
            jsonArray.add(jsonObject);
        }

        if (itAssetTaskWorkTime>0) {
            jsonObject = new JSONObject();
            jsonObject.set("name","盘点时长");
            jsonObject.set("value", commonService.getFormatValue((double)(itAssetTaskWorkTime / 60000) / 60));
            jsonArray.add(jsonObject);
        }

        if (remoteControlTaskTime>0) {
            jsonObject = new JSONObject();
            jsonObject.set("name","遥控时长");
            jsonObject.set("value", commonService.getFormatValue((double)(remoteControlTaskTime / 60000) / 60));
            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }

    /**
     * 机柜状态统计
     * @param roomId
     * @param dateStr
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 10:12
     */
    private JSONArray getReportCabinetStatus(long roomId, String dateStr){
        //正常巡检柜次
        int normalCabinetcount = taskDetectionResultDao.normalCabinetcountByRoomIdAndDate(roomId, dateStr);
        List<Map> list = warnInfoDao.abnormalCabinetcountByRoomIdAndDate(roomId, dateStr);

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("name", "正常机柜");
        jsonObject.set("value", normalCabinetcount);
        jsonArray.add(jsonObject);

        for (Map map : list) {
            String detectionId = map.get("detection_id").toString();
            int count = Integer.parseInt(map.get("count")+"");
            jsonObject = new JSONObject();
            if (Detection.NOISE.equals(detectionId)){
                jsonObject.set("name", "噪声过大机柜");
            }else  if (Detection.INFRARED.equals(detectionId)){
                jsonObject.set("name", "温度过高机柜");
            }else  if (Detection.ALARMLIGHT.equals(detectionId)){
                jsonObject.set("name", "指示灯报警机柜");
            }else{
                continue;
            }
            jsonObject.set("value", count);
            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }

    /**
     * 告警类型占比
     * @param roomId
     * @param dateStr
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/29 10:12
     */
    private JSONArray getReportWarnType(long roomId, String dateStr){
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        List<WarnInfo> warnInfos = warnInfoDao.listByDate(roomId, dateStr);
        for (WarnInfo warnInfo : warnInfos) {
            String detectionId = warnInfo.getDetectionId();
            if (Detection.HUMIDITY.equals(detectionId)){
                detectionId = "湿度";
            }else if (Detection.INFRARED.equals(detectionId)){
                detectionId = "设备温度";
            }else if (Detection.TEMPERATURE.equals(detectionId)){
                detectionId = "温度";
            }else if (Detection.ALARMLIGHT.equals(detectionId)){
                detectionId = "报警灯";
            }else if (Detection.NOISE.equals(detectionId)){
                detectionId = "噪声";
            }else if (Detection.PM2P5.equals(detectionId)){
                detectionId = "PM2.5";
            }else if (Detection.SMOKE.equals(detectionId)){
                detectionId = "烟雾";
            }
            boolean existsData = false;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getStr("name");
                if (detectionId.equals(name)){
                    existsData = true;
                    jsonObject.set("value", jsonObject.getInt("value")+1);
                    break;
                }
            }
            if (!existsData){
                jsonObject = new JSONObject();
                jsonObject.set("name", detectionId);
                jsonObject.set("value", 1);
                jsonArray.add(jsonObject);

            }
        }
        return jsonArray;
    }

    /**
     * 根据机房id、日期获取报告概览
     * @param roomId
     * @param dateStr
     * @return java.util.HashMap
     * @author kliu
     * @date 2022/6/25 17:36
     */
    private HashMap getReportOverview(long roomId, String dateStr){
        List<TaskInstance> taskInstances = taskInfoDao.listByRoomIdAndDate(roomId, dateStr);
        String startTime = "";
        String endTime = "";
        String inspectionHourStr = "";
        //巡检次数
        int inspectionCount = taskInstances.size();
        double inspectionMinute = 0;
        long timeDiff = 0;

        TaskInstance taskInstance = null;

        for (int i = 0; i < taskInstances.size(); i++) {
            taskInstance = taskInstances.get(i);
            String dbStartTime = taskInstance.getStartTime();
            if (i == 0){
                startTime = dbStartTime;
            }

            String dbEndTime = taskInstance.getEndTime();
            if (i == taskInstances.size()-1){
                endTime = dbEndTime;
                if (StringUtils.isEmpty(endTime)){
                    endTime = "-";
                }
            }

            if (StringUtils.isEmpty(dbEndTime)){
                if(i<taskInstances.size()-1){
                    dbEndTime = taskInstances.get(i+1).getStartTime();
                }else{
                    dbEndTime = DateUtil.now();
                }
            }
            timeDiff += DateUtil.parseDateTime(dbEndTime).getTime() - DateUtil.parseDateTime(dbStartTime).getTime();
        }

        //分钟
        inspectionMinute = timeDiff / 60000;

        double inspectionHour = inspectionMinute/60;
        if (inspectionHour>0){
            inspectionHourStr+=commonService.getFormatValue(inspectionHour, 1)+"小时";
        }

        //巡检点位
        int inspectCount = taskDetectionResultDao.countByRoomIdAndDate(roomId, dateStr);

        //异常信息
        List<WarnInfo> warnInfos = warnInfoDao.listByDate(roomId, dateStr);
        for (int i = 0; i < warnInfos.size(); i++) {
            long taskLogIdi = warnInfos.get(i).getTaskLogId();
            for (int j = i+1; j < warnInfos.size(); j++) {
                long taskLogIdj = warnInfos.get(j).getTaskLogId();
                if (taskLogIdi == taskLogIdj) {
                    warnInfos.remove(j);
                    j--;
                }
            }
        }

        HashMap hashMap = new HashMap(6);
        hashMap.put("inspectionCount", inspectionCount);
        hashMap.put("startTime", startTime);
        hashMap.put("endTime", endTime);
        hashMap.put("inspectionHour", inspectionHourStr);
        hashMap.put("inspectCount", inspectCount);
        hashMap.put("abnormalCount", warnInfos.size());
        taskInstances = null;
        warnInfos = null;

        //添加资产盘点统计
        int itAssetInspectCount = itAssetTaskInstanceDao.countByRoomIdAndDate(roomId, dateStr);
        int itAssetCount = itAssetDao.countByRoomId(roomId);
        hashMap.put("itAssetInspectCount", itAssetInspectCount);
        hashMap.put("itAssetCount", itAssetCount);
        return hashMap;
    }

    /**
     * 获取当天报告检测项概览
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/5/25 8:47
     */
    @Override
    public List getTodayReportDetectionOverview(long roomId) {
        String dateStr = DateUtil.now().substring(0, 10);
        return roomDetectionSumDayDao.list(roomId, dateStr);
    }

    /**
     * 获取近7天报告检测项概况
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/27 12:17
     */
    @Override
    public List getRecent7DayReportDetectionOverview(long roomId) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        List<RoomDetectionSumDay> roomDetectionSumDays = roomDetectionSumDayDao.listGteDate(roomId, day6Before);
        return reportDetectionOverviewDeal(roomDetectionSumDays);
    }

    /**
     * 获取近30天报告检测项概况
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/27 12:17
     */
    @Override
    public List getRecent30DayReportDetectionOverview(long roomId) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        List<RoomDetectionSumDay> roomDetectionSumDays = roomDetectionSumDayDao.listGteDate(roomId, day29Before);
        return reportDetectionOverviewDeal(roomDetectionSumDays);
    }

    /**
     * 检测项报告概览重新计算-7日、1月
     * @param roomDetectionSumDays
     * @return java.util.List
     * @author kliu
     * @date 2022/6/27 14:57
     */
    private List reportDetectionOverviewDeal(List<RoomDetectionSumDay> roomDetectionSumDays){
        Map<String, JSONObject> hashMap = new HashMap(10);
        JSONObject jsonObject;
        List<RoomDetectionSumDay> detectionSums = new ArrayList<>();
        List<BigDecimal> medianList = new ArrayList<BigDecimal>();
        //中位数通过记录list最后进行处理
        //平均数先计算总和，最后进行除法操作
        //最大值、最小值直接进行比对操作
        long roomId=0;
        for (RoomDetectionSumDay roomDetectionSumDay : roomDetectionSumDays) {
            roomId = roomDetectionSumDay.getRoomId();
            String detectionId = roomDetectionSumDay.getDetectionId();
            BigDecimal median = roomDetectionSumDay.getMedian();
            long abnormalCount = roomDetectionSumDay.getAbnormalCount();
            BigDecimal avg = roomDetectionSumDay.getAvg();
            long count = roomDetectionSumDay.getCount();
            BigDecimal max = roomDetectionSumDay.getMax();
            BigDecimal min = roomDetectionSumDay.getMin();
            if (hashMap.containsKey(detectionId)) {
                jsonObject = hashMap.get(detectionId);
                medianList = (List<BigDecimal>) jsonObject.get("median");
                BigDecimal avg1 = jsonObject.getBigDecimal("avg");
                BigDecimal max1 = jsonObject.getBigDecimal("max");
                BigDecimal min1 = jsonObject.getBigDecimal("min");
                Long abnormalCount1 = jsonObject.getLong("abnormalCount");
                Long count1 = jsonObject.getLong("count");
                avg1 = avg1.add(avg);
                abnormalCount1 = abnormalCount1+abnormalCount;
                count1 = count1+count;

                if (max.compareTo(max1)>0) {
                    max1 = max;
                }
                if (min.compareTo(min1)<0) {
                    min1 = min;
                }
                jsonObject.set("median", medianList);
                jsonObject.set("avg", avg1);
                jsonObject.set("max", max1);
                jsonObject.set("min", min1);
                jsonObject.set("count", count1);
                jsonObject.set("abnormalCount", abnormalCount1);
            }else{
                jsonObject = new JSONObject();
                medianList.add(median);
                jsonObject.set("median", medianList);
                jsonObject.set("avg", avg);
                jsonObject.set("max", max);
                jsonObject.set("min", min);
                jsonObject.set("count", count);
                jsonObject.set("abnormalCount", abnormalCount);
                hashMap.put(detectionId, jsonObject);
            }
        }

        RoomDetectionSumDay roomDetectionSumDay;
        for (Map.Entry<String, JSONObject> entry : hashMap.entrySet()) {
            roomDetectionSumDay = new RoomDetectionSumDay();
            String detectionId = entry.getKey();
            jsonObject = entry.getValue();

            medianList = (List<BigDecimal>) jsonObject.get("median");
            BigDecimal avg = jsonObject.getBigDecimal("avg");
            BigDecimal max = jsonObject.getBigDecimal("max");
            BigDecimal min = jsonObject.getBigDecimal("min");
            Long abnormalCount = jsonObject.getLong("abnormalCount");
            Long count = jsonObject.getLong("count");

            Collections.sort(medianList);
            roomDetectionSumDay.setDetectionId(detectionId);
            roomDetectionSumDay.setAvg(avg.divide(new BigDecimal(roomDetectionSumDays.size()),2,BigDecimal.ROUND_HALF_UP));
            roomDetectionSumDay.setMax(max);
            roomDetectionSumDay.setMedian(getMedian(medianList));
            roomDetectionSumDay.setMin(min);
            roomDetectionSumDay.setAbnormalCount(abnormalCount);
            roomDetectionSumDay.setCount(count);
            roomDetectionSumDay.setDetectionDate("");
            roomDetectionSumDay.setRoomId(roomId);
            detectionSums.add(roomDetectionSumDay);
        }

        return detectionSums;
    }

    /**
     * 获取中位数
     * @param list
     * @return java.math.BigDecimal
     * @author kliu
     * @date 2022/6/27 17:13
     */
    private BigDecimal getMedian(List<BigDecimal> list) {
        BigDecimal j;
        int size = list.size();
        int two = 2;
        if(size % two == 1){
            j = list.get((size-1)/2);
        }else {
            j = (list.get(size/2-1).add(list.get(size/2))).divide(new BigDecimal(2));
        }
        return j;
    }

    /**
     * 获取当天检测项明细信息
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/5/25 8:48
     */
    @Override
    public Map getTodayDetectionDetlInfo(long roomId) {
        Map resultMap = new HashMap(2);
        String currentDateStr = DateUtil.now().substring(0, 10);
        List list = new ArrayList();
        TaskInstance taskInstance = taskDao.getRecentHasDataTaskByRoomId(roomId);
        String startTime = taskInstance.getStartTime();
        if(startTime.compareTo(currentDateStr)<0){
            resultMap.put("list", list);
            resultMap.put("task_time", "");
            return resultMap;
        }

        long instanceId = taskInstance.getInstanceId();
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.list(instanceId);
        JSONObject jsonObject;
        for (TaskDetectionResult taskDetectionResult : taskDetectionResults) {
            String pointName = taskDetectionResult.getPointName();
            String sensor = taskDetectionResult.getSensor();
            String alarmLight = taskDetectionResult.getAlarmLight();
            String infrared = taskDetectionResult.getInfrared();
            jsonObject = new JSONObject();
            jsonObject.set("pointName", pointName.replace("上","").replace("下",""));
            if(!StringUtils.isEmpty(sensor)){
                getSensorData(sensor, jsonObject);
            }

            if(!StringUtils.isEmpty(alarmLight)){
                getAlarmLightData(alarmLight, jsonObject);
            }

            if(!StringUtils.isEmpty(infrared)){
                getInfraredData(infrared, jsonObject);
            }

            list.add(jsonObject);
        }

        taskDetectionResults = null;
        resultMap.put("list", list);
        resultMap.put("task_time", "-"+startTime);
        return resultMap;
    }

    @Override
    public JSONObject getTodayDetectionDetlInfoWithSum(long roomId, int pageSize, int pageNum) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getDetectionDetlInfoWithSum(roomId, currentDateStr, pageSize, pageNum);
    }

    @Override
    public JSONObject getRecent7DaysDetectionDetlInfoWithSum(long roomId, int pageSize, int pageNum) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getDetectionDetlInfoWithSum(roomId, day6Before, pageSize, pageNum);
    }

    @Override
    public JSONObject getRecent30DaysDetectionDetlInfoWithSum(long roomId, int pageSize, int pageNum) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getDetectionDetlInfoWithSum(roomId, day29Before, pageSize, pageNum);
    }

    /**
     * 获取检测项明细数据
     * @param roomId
     * @param dateStr
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/1 20:21
     */
    private JSONObject getDetectionDetlInfoWithSum(long roomId, String dateStr, int pageSize, int pageNum) {
        //巡检机柜总数
        int cabinetCount = taskDetectionResultDao.cabinetCountByRoomIdAndDate(roomId, dateStr);
        //巡检总柜次
        int count = taskDetectionResultDao.countByRoomIdAndDate(roomId, dateStr);
        //正常机柜次
        int normalCabinetcount = taskDetectionResultDao.normalCabinetcountByRoomIdAndDate(roomId, dateStr);
        //异常柜次
        int abnormalCabinetcount = count - normalCabinetcount;
        //累计设备报警
        int cabinetWarnCount = taskDetectionResultDao.cabinetWarnCountByRoomIdAndDate(roomId, dateStr);
        //每个点位的巡检机柜次
        List<Map<String, Object>> pointCountList = taskDetectionResultDao.pointCountByRoomIdAndDate(roomId, dateStr);

        //列表
        JSONArray jsonArray = new JSONArray();

        List<RoomDetectionPointSumDay> roomDetectionPointSumDays = roomDetectionSumPointDayDao.listGteDate(roomId, dateStr);

        //重新计算最大值、最小值、平均数
        RoomDetectionPointSumDay roomDetectionPointSumDay;
        List<RoomDetectionPointSumDay> tempRoomDetectionPointSumDays;

        //点位异常柜次  点位巡检总柜次    检测项累计检测柜次
        long pointAbnormalCount, pointCount=0L, detectionPointCount=0L;
        BigDecimal max, min;
        String detectionId;
        JSONObject detectionObject, detectionDetlObject, tempMaxMinObject;

        while (roomDetectionPointSumDays.size() > 0){
            tempRoomDetectionPointSumDays = new ArrayList();
            //获取第一个点位名称
            String pointName = roomDetectionPointSumDays.get(0).getPointName();
            //此处为啥添加该项，是因为 有的检测项如果只配置传感器，不配置红外，则可能会出现计算错误的问题
            for (Map<String, Object> map : pointCountList) {
                String countListPointName = map.get("point_name").toString();
                if(countListPointName.equals(pointName)){
                    pointCount = (Long)map.get("count");
                    break;
                }
            }
            //将第一个点位的所有数据过滤出来，一个点位一个点位的处理数据
            for (int i = 0; i < roomDetectionPointSumDays.size(); i++) {
                roomDetectionPointSumDay = roomDetectionPointSumDays.get(i);
                if (pointName.equals(roomDetectionPointSumDay.getPointName())){
                    tempRoomDetectionPointSumDays.add(roomDetectionPointSumDay);
                    roomDetectionPointSumDays.remove(i);
                    i--;
                }
            }

            detectionObject = new JSONObject();
            boolean maxAbnormal;
            boolean minAbnormal;
            //对同一个点位名称的数据进行汇总比对等
            for (RoomDetectionPointSumDay tempRoomDetectionPointSumDay : tempRoomDetectionPointSumDays) {
                detectionId = tempRoomDetectionPointSumDay.getDetectionId();
                //不包含当前id，则添加当前id
                if (!detectionObject.containsKey(detectionId)){
                    detectionDetlObject = new JSONObject();
                    detectionDetlObject.set("max", new BigDecimal(0));
                    detectionDetlObject.set("min", new BigDecimal(1000000000));
                    detectionDetlObject.set("pointAbnormalCount", 0);
                    detectionDetlObject.set("detectionPointCount", 0);
                    detectionDetlObject.set("maxAbnormal", false);
                    detectionDetlObject.set("minAbnormal", false);
                    detectionObject.set(detectionId, detectionDetlObject);

                }

                detectionDetlObject = detectionObject.getJSONObject(detectionId);
                max = detectionDetlObject.getBigDecimal("max");
                min = detectionDetlObject.getBigDecimal("max");

                pointAbnormalCount = detectionDetlObject.getInt("pointAbnormalCount");
                pointAbnormalCount += tempRoomDetectionPointSumDay.getAbnormalCount();
                detectionPointCount = detectionDetlObject.getInt("detectionPointCount");
                detectionPointCount += tempRoomDetectionPointSumDay.getCount();

                maxAbnormal = detectionDetlObject.getBool("maxAbnormal");
                minAbnormal = detectionDetlObject.getBool("minAbnormal");

                if (max.compareTo(tempRoomDetectionPointSumDay.getMax()) < 0){
                    max = tempRoomDetectionPointSumDay.getMax();
                    //max不异常，则需要每次判断，如果异常了，不再继续判断
                    if (!maxAbnormal){
                        if ("1".equals(tempRoomDetectionPointSumDay.getMaxAbnormal())){
                            maxAbnormal = true;
                        }
                    }
                }
                if (min.compareTo(tempRoomDetectionPointSumDay.getMin()) > 0) {
                    min = tempRoomDetectionPointSumDay.getMin();
                    //min不异常，则需要每次判断，如果异常了，不再继续判断
                    if (!minAbnormal){
                        if ("1".equals(tempRoomDetectionPointSumDay.getMinAbnormal())){
                            minAbnormal = true;
                        }
                    }
                }
                detectionDetlObject.set("max", max);
                detectionDetlObject.set("min", min);
                detectionDetlObject.set("pointAbnormalCount", pointAbnormalCount);
                detectionDetlObject.set("detectionPointCount", detectionPointCount);
                detectionDetlObject.set("maxAbnormal", maxAbnormal);
                detectionDetlObject.set("minAbnormal", minAbnormal);
            }

            //去除烟雾返回
            if (detectionObject.containsKey(Detection.SMOKE)){
                detectionObject.remove(Detection.SMOKE);
            }
            //报警灯-ai
            //添加ai是为了前端可以整合到一个单元格内
            if (detectionObject.containsKey(Detection.ALARMLIGHT)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.ALARMLIGHT);
                pointAbnormalCount = detectionDetlObject.getInt("pointAbnormalCount");
                detectionObject.remove(Detection.ALARMLIGHT);
                detectionObject.set("ai_alarm_light", pointAbnormalCount);
            }
            //红外-ai
            if (detectionObject.containsKey(Detection.INFRARED)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.INFRARED);
                pointAbnormalCount = detectionDetlObject.getInt("pointAbnormalCount");
                detectionObject.set("ai_infrared", pointAbnormalCount);
            }
            //设备温度
            if (detectionObject.containsKey(Detection.INFRARED)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.INFRARED);
                max = detectionDetlObject.getBigDecimal("max");
                min = detectionDetlObject.getBigDecimal("min");
                maxAbnormal = detectionDetlObject.getBool("maxAbnormal");
                minAbnormal = detectionDetlObject.getBool("minAbnormal");

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", max);
                tempMaxMinObject.set("abnormal", maxAbnormal);
                detectionDetlObject.remove("max");
                detectionDetlObject.remove("maxAbnormal");
                detectionDetlObject.set("max", tempMaxMinObject);

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", min);
                tempMaxMinObject.set("abnormal", minAbnormal);
                detectionDetlObject.remove("min");
                detectionDetlObject.remove("minAbnormal");
                detectionDetlObject.set("min", tempMaxMinObject);
            }else{
                detectionDetlObject = new JSONObject();
                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", "-");
                tempMaxMinObject.set("abnormal", false);
                detectionDetlObject.set("max", tempMaxMinObject);
                detectionDetlObject.set("min", tempMaxMinObject);
                detectionObject.set(Detection.INFRARED, detectionDetlObject);
            }

            //温湿度等
            if (detectionObject.containsKey(Detection.TEMPERATURE)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.TEMPERATURE);
                max = detectionDetlObject.getBigDecimal("max");
                min = detectionDetlObject.getBigDecimal("min");
                maxAbnormal = detectionDetlObject.getBool("maxAbnormal");
                minAbnormal = detectionDetlObject.getBool("minAbnormal");

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", max);
                tempMaxMinObject.set("abnormal", maxAbnormal);
                detectionDetlObject.remove("max");
                detectionDetlObject.remove("maxAbnormal");
                detectionDetlObject.set("max", tempMaxMinObject);

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", min);
                tempMaxMinObject.set("abnormal", minAbnormal);
                detectionDetlObject.remove("min");
                detectionDetlObject.remove("minAbnormal");
                detectionDetlObject.set("min", tempMaxMinObject);
            }else{
                detectionDetlObject = new JSONObject();
                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", "-");
                tempMaxMinObject.set("abnormal", false);
                detectionDetlObject.set("max", tempMaxMinObject);
                detectionDetlObject.set("min", tempMaxMinObject);
                detectionObject.set(Detection.TEMPERATURE, detectionDetlObject);
            }

            if (detectionObject.containsKey(Detection.HUMIDITY)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.HUMIDITY);
                max = detectionDetlObject.getBigDecimal("max");
                min = detectionDetlObject.getBigDecimal("min");
                maxAbnormal = detectionDetlObject.getBool("maxAbnormal");
                minAbnormal = detectionDetlObject.getBool("minAbnormal");

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", max);
                tempMaxMinObject.set("abnormal", maxAbnormal);
                detectionDetlObject.remove("max");
                detectionDetlObject.remove("maxAbnormal");
                detectionDetlObject.set("max", tempMaxMinObject);

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", min);
                tempMaxMinObject.set("abnormal", minAbnormal);
                detectionDetlObject.remove("min");
                detectionDetlObject.remove("minAbnormal");
                detectionDetlObject.set("min", tempMaxMinObject);
            }else{
                detectionDetlObject = new JSONObject();
                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", "-");
                tempMaxMinObject.set("abnormal", false);
                detectionDetlObject.set("max", tempMaxMinObject);
                detectionDetlObject.set("min", tempMaxMinObject);
                detectionObject.set(Detection.HUMIDITY, detectionDetlObject);
            }

            if (detectionObject.containsKey(Detection.NOISE)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.NOISE);
                max = detectionDetlObject.getBigDecimal("max");
                min = detectionDetlObject.getBigDecimal("min");
                maxAbnormal = detectionDetlObject.getBool("maxAbnormal");
                minAbnormal = detectionDetlObject.getBool("minAbnormal");

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", max);
                tempMaxMinObject.set("abnormal", maxAbnormal);
                detectionDetlObject.remove("max");
                detectionDetlObject.remove("maxAbnormal");
                detectionDetlObject.set("max", tempMaxMinObject);

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", min);
                tempMaxMinObject.set("abnormal", minAbnormal);
                detectionDetlObject.remove("min");
                detectionDetlObject.remove("minAbnormal");
                detectionDetlObject.set("min", tempMaxMinObject);
            }else{
                detectionDetlObject = new JSONObject();
                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", "-");
                tempMaxMinObject.set("abnormal", false);
                detectionDetlObject.set("max", tempMaxMinObject);
                detectionDetlObject.set("min", tempMaxMinObject);
                detectionObject.set(Detection.NOISE, detectionDetlObject);
            }

            if (detectionObject.containsKey(Detection.PM2P5)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.PM2P5);
                max = detectionDetlObject.getBigDecimal("max");
                min = detectionDetlObject.getBigDecimal("min");
                maxAbnormal = detectionDetlObject.getBool("maxAbnormal");
                minAbnormal = detectionDetlObject.getBool("minAbnormal");

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", max);
                tempMaxMinObject.set("abnormal", maxAbnormal);
                detectionDetlObject.remove("max");
                detectionDetlObject.remove("maxAbnormal");
                detectionDetlObject.set("max", tempMaxMinObject);

                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", min);
                tempMaxMinObject.set("abnormal", minAbnormal);
                detectionDetlObject.remove("min");
                detectionDetlObject.remove("minAbnormal");
                detectionDetlObject.set("min", tempMaxMinObject);
            }else{
                detectionDetlObject = new JSONObject();
                tempMaxMinObject = new JSONObject();
                tempMaxMinObject.set("value", "-");
                tempMaxMinObject.set("abnormal", false);
                detectionDetlObject.set("max", tempMaxMinObject);
                detectionDetlObject.set("min", tempMaxMinObject);
                detectionObject.set(Detection.PM2P5, detectionDetlObject);
            }

            //灭火器-ai  不包含灭火器则不添加数据
            if (detectionObject.containsKey(Detection.FIREEXTINGUISHER)){
                detectionDetlObject = detectionObject.getJSONObject(Detection.FIREEXTINGUISHER);
                pointAbnormalCount = detectionDetlObject.getInt("pointAbnormalCount");
                detectionObject.set("ai_"+Detection.FIREEXTINGUISHER, pointAbnormalCount);
                detectionObject.remove(Detection.FIREEXTINGUISHER);
            }

            detectionObject.set("pointName", pointName.replace("上","").replace("下",""));
            detectionObject.set("pointCount", pointCount);
            jsonArray.add(detectionObject);
        }

        //此处添加分页
        int allSize = jsonArray.size();
        int startSize = pageSize * (pageNum-1);
        int endSize = pageSize * pageNum;

        JSONArray returnJsonArray = new JSONArray();

        for (int i = startSize; i < endSize && i < allSize; i++) {
            returnJsonArray.add(jsonArray.getJSONObject(i));
        }

        int totalPage = allSize/pageSize;
        if (allSize%pageSize>0){
            totalPage++;
        }
        PageBean pageBean = new PageBean();
        pageBean.setCurrentpage(pageNum);
        pageBean.setTotalPage(totalPage);
        pageBean.setTotalSize(allSize);
        pageBean.setContentList(returnJsonArray);
        pageBean.setPageSize(pageSize);
        pageBean.setListTotalSize(returnJsonArray.size());


        JSONObject jsonObject = new JSONObject();
        jsonObject.set("cabinetCount", cabinetCount);
        jsonObject.set("count", count);
        jsonObject.set("normalCabinetcount", normalCabinetcount);
        jsonObject.set("abnormalCabinetcount", abnormalCabinetcount);
        jsonObject.set("cabinetWarnCount", cabinetWarnCount);
        jsonObject.set("cabinetPageList", pageBean);


        pointCountList = null;
        roomDetectionPointSumDays = null;
        roomDetectionPointSumDay = null;
        tempRoomDetectionPointSumDays = null;
        detectionObject = null;
        detectionDetlObject = null;
        tempMaxMinObject = null;
        pageBean = null;

        return jsonObject;
    }
    /**
     * 获取最近一次检测项明细信息
     * @param roomId
     * @return java.util.Map
     * @author kliu
     * @date 2022/6/27 14:33
     */
    @Override
    public Map getRecentDetectionDetlInfo(long roomId) {
        Map resultMap = new HashMap(2);
        List list = new ArrayList();
        TaskInstance taskInstance = taskDao.getRecentHasDataTaskByRoomId(roomId);
        String startTime = taskInstance.getStartTime();

        long instanceId = taskInstance.getInstanceId();
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.list(instanceId);
        JSONObject jsonObject;
        for (TaskDetectionResult taskDetectionResult : taskDetectionResults) {
            String pointName = taskDetectionResult.getPointName();
            String sensor = taskDetectionResult.getSensor();
            String alarmLight = taskDetectionResult.getAlarmLight();
            String infrared = taskDetectionResult.getInfrared();
            jsonObject = new JSONObject();
            jsonObject.set("pointName", pointName.replace("上","").replace("下",""));
            if(!StringUtils.isEmpty(sensor)){
                getSensorData(sensor, jsonObject);
            }

            if(!StringUtils.isEmpty(alarmLight)){
                getAlarmLightData(alarmLight, jsonObject);
            }

            if(!StringUtils.isEmpty(infrared)){
                getInfraredData(infrared, jsonObject);
            }

            list.add(jsonObject);
        }

        taskDetectionResults = null;
        resultMap.put("list", list);
        resultMap.put("task_time", "-"+startTime);
        return resultMap;
    }

    /**
     * 获取当日报告图片告警数据
     *
     * @param roomId
     * @param pointName
     * @param aiType
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/4 11:12
     */
    @Override
    public JSONArray getTodayDetectionAiPicture(long roomId, String pointName, String aiType) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getDetectionAiPicture(roomId, pointName, aiType, currentDateStr);
    }

    /**
     * 获取近7日报告图片告警数据
     *
     * @param roomId
     * @param pointName
     * @param aiType
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/4 11:12
     */
    @Override
    public JSONArray getRecent7DaysDetectionAiPicture(long roomId, String pointName, String aiType) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getDetectionAiPicture(roomId, pointName, aiType, day6Before);
    }

    /**
     * 获取近30日报告图片告警数据
     *
     * @param roomId
     * @param pointName
     * @param aiType
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/4 11:12
     */
    @Override
    public JSONArray getRecent30DaysDetectionAiPicture(long roomId, String pointName, String aiType) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getDetectionAiPicture(roomId, pointName, aiType, day29Before);
    }

    /**
     * 当天人员行为汇总
     *
     * @param roomId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/4 16:22
     */
    @Override
    public JSONObject getTodayPersonBehaviorWithSum(long roomId, int pageSize, int pageNum) {
        String currentDateStr = DateUtil.now().substring(0, 10);
        return getPersonBehaviorWithSum(roomId, currentDateStr, pageSize, pageNum);
    }

    /**
     * 近7日人员行为汇总
     *
     * @param roomId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/4 16:22
     */
    @Override
    public JSONObject getRecent7DaysPersonBehaviorWithSum(long roomId, int pageSize, int pageNum) {
        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        return getPersonBehaviorWithSum(roomId, day6Before, pageSize, pageNum);
    }

    /**
     * 近30日人员行为汇总
     *
     * @param roomId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/4 16:22
     */
    @Override
    public JSONObject getRecent30DaysPersonBehaviorWithSum(long roomId, int pageSize, int pageNum) {
        String day29Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-29), "yyyy-MM-dd");
        return getPersonBehaviorWithSum(roomId, day29Before, pageSize, pageNum);
    }

    @Override
    public JSONObject getRecentCabinetUBitList(long roomId, int pageSize, int pageNum) {
        //微模块数=门控模块
        int wmkCount = 0;
        List<TreeNode> pointTreeNodes = pointTreeNodeService.getPointTreeNode(roomId);
        if (pointTreeNodes != null){
            for (TreeNode pointTreeNode : pointTreeNodes) {
                String node = pointTreeNode.getNode();
                if (node.startsWith("门") && StrUtil.count(node, "-") == 2){
                    wmkCount++;
                }
            }
        }

        //42U 48U 已用U 空闲U
        //已用机柜-暂不统计
        //空闲机柜-暂不统计
        int ubit42Count = 0;
        int ubit48Count = 0;
        int ubitUseCount = 0;
        int ubitFreeCount = 0;
        List<CabinetUbit> cabinetUbits = cabinetUbitDao.list(roomId);
        for (CabinetUbit cabinetUbit : cabinetUbits) {
            int ubit = cabinetUbit.getUbit();
            if (ubit == 42){
                ubit42Count++;
            }else if (ubit == 48){
                ubit48Count++;
            }
            int useUbit = cabinetUbit.getUseUbit();
            ubitUseCount+=useUbit;
            int freeUbit = cabinetUbit.getFreeUbit();
            ubitFreeCount+=freeUbit;
        }

        //此处添加分页
        int allSize = cabinetUbits.size();
        int startSize = pageSize * (pageNum-1);
        int endSize = pageSize * pageNum;

        JSONArray returnJsonArray = new JSONArray();
        CabinetUbit cabinetUbit;
        for (int i = startSize; i < endSize && i < allSize; i++) {
            cabinetUbit = cabinetUbits.get(i);
            returnJsonArray.add(cabinetUbit);
        }
        int totalPage = allSize/pageSize;
        if (allSize%pageSize>0){
            totalPage++;
        }
        PageBean pageBean = new PageBean();
        pageBean.setCurrentpage(pageNum);
        pageBean.setTotalPage(totalPage);
        pageBean.setTotalSize(allSize);
        pageBean.setContentList(returnJsonArray);
        pageBean.setPageSize(pageSize);
        pageBean.setListTotalSize(returnJsonArray.size());



        JSONObject jsonObject = new JSONObject();
        jsonObject.set("wmkCount", wmkCount);
        jsonObject.set("ubit42Count", ubit42Count);
        jsonObject.set("ubit48Count", ubit48Count);
        jsonObject.set("ubitUseCount", ubitUseCount);
        jsonObject.set("ubitFreeCount", ubitFreeCount);
        jsonObject.set("ubitPageList", pageBean);

        cabinetUbits = null;
        pointTreeNodes = null;
        returnJsonArray = null;
        pageBean = null;
        cabinetUbit = null;
        return jsonObject;
    }

    /**
     * 获取人员行为汇总信息
     * @param roomId
     * @param dateStr
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/11/4 16:26
     */
    private JSONObject getPersonBehaviorWithSum(long roomId, String dateStr, int pageSize, int pageNum) {

        //行人检测异常数据
        List<AlongWorkPedestrianDetectionAlarm> xrjcycList = alongWorkPedestrianDetectionAlarmDao.listByRoomIdAndDate(roomId, dateStr);

        List<AlongWork> alongWorks = alongWorkDao.listByRoomIdAndDate(roomId, dateStr);
        List<PersonnelManagement> personnelManagements = personnelManagementDao.pageList(new PersonnelManagement(), 1000000, 1).getContentList();

        //未进行人脸识别或者人脸识别超时的数据
        List<AlongWork> alongWorkWithoutStartTimes = alongWorkDao.listByRoomIdAndDateWithOutStartTime(roomId, dateStr);

        //人脸识别次数=随工任务下发次数
        int faceAllCount = alongWorkDao.workIssueCountByRoomIdAndDate(roomId, dateStr);
        //异常次数=随工任务下发，没有开始时间的数据
        int faceAbnormalCount = alongWorkWithoutStartTimes.size();
        //正常次数=总-异常
        int faceNormalCount = faceAllCount - faceAbnormalCount;
        //累计报警次数  未进行人脸识别或者人脸识别失败+行人检测告警的数据
        int abnormalCountSum = faceAbnormalCount + xrjcycList.size();
        //访客超时报警 --随工里边使用了访客人员且超过有效期的人员
        List<AlongWork> alongWorkWithVisitorOverTimes = alongWorkDao.listByRoomIdAndDateWithVisitorOverTime(roomId, dateStr);
        int visitorOverTimeCount = alongWorkWithVisitorOverTimes.size();
        //超域超限报警  --不加，暂时没有数据来源

        //敏感行为报警  --使用随工里边未检测到行人的功能
        int mgxwtCount = xrjcycList.size();

        //列表
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        for (AlongWork alongWorkWithoutStartTime : alongWorkWithoutStartTimes) {
            jsonObject = new JSONObject();
            String taskTime = alongWorkWithoutStartTime.getTaskTime();
            long taskUserId = alongWorkWithoutStartTime.getTaskUserId();
            String personnelUrl= "";
            String personnelDepartment = "", personnelType = "", personnelName = "";
            for (PersonnelManagement personnelManagement : personnelManagements) {
                long personnelId = personnelManagement.getPersonnelId();
                if(personnelId == taskUserId){
                    personnelUrl = personnelManagement.getPersonnelUrl();
                    personnelDepartment = personnelManagement.getPersonnelDepartment();
                    personnelType = personnelManagement.getPersonnelType();
                    personnelName = personnelManagement.getPersonnelName();
                    //人员类型 vip:VIP normal:普通 visitor:访客 blacklist:黑名单
                    if ("vip".equals(personnelType)){
                        personnelType = "VIP ";
                    }else if ("normal".equals(personnelType)){
                        personnelType = "普通 ";
                    }else if ("visitor".equals(personnelType)){
                        personnelType = "访客 ";
                    }else if ("blacklist".equals(personnelType)){
                        personnelType = "黑名单 ";
                    }
                    break;
                }
            }
            jsonObject.set("time", taskTime);
            jsonObject.set("personInfo", personnelDepartment + personnelType + personnelName);
            jsonObject.set("alarmInfo", "随工人脸识别失败");
            jsonObject.set("personUrl", personnelUrl);
            jsonArray.add(jsonObject);
        }

        for (AlongWork alongWorkWithVisitorOverTime : alongWorkWithVisitorOverTimes) {
            jsonObject = new JSONObject();
            String taskTime = alongWorkWithVisitorOverTime.getTaskTime();
            long taskUserId = alongWorkWithVisitorOverTime.getTaskUserId();
            String personnelUrl= "";
            String personnelDepartment = "", personnelType = "", personnelName = "";
            for (PersonnelManagement personnelManagement : personnelManagements) {
                long personnelId = personnelManagement.getPersonnelId();
                if(personnelId == taskUserId){
                    personnelUrl = personnelManagement.getPersonnelUrl();
                    personnelDepartment = personnelManagement.getPersonnelDepartment();
                    personnelType = personnelManagement.getPersonnelType();
                    personnelName = personnelManagement.getPersonnelName();
                    //人员类型 vip:VIP normal:普通 visitor:访客 blacklist:黑名单
                    if ("vip".equals(personnelType)){
                        personnelType = "VIP ";
                    }else if ("normal".equals(personnelType)){
                        personnelType = "普通 ";
                    }else if ("visitor".equals(personnelType)){
                        personnelType = "访客 ";
                    }else if ("blacklist".equals(personnelType)){
                        personnelType = "黑名单 ";
                    }
                    break;
                }
            }
            jsonObject.set("time", taskTime);
            jsonObject.set("personInfo", personnelDepartment + personnelType + personnelName);
            jsonObject.set("alarmInfo", "访客超时报警");
            jsonObject.set("personUrl", personnelUrl);
            jsonArray.add(jsonObject);
        }

        for (AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm : xrjcycList) {
            long pid = alongWorkPedestrianDetectionAlarm.getPid();
            for (AlongWork alongWork : alongWorks) {
                long id = alongWork.getId();
                if(pid == id){
                    jsonObject = new JSONObject();
                    String taskTime = alongWork.getTaskTime();
                    long taskUserId = alongWork.getTaskUserId();
                    String personnelUrl= "";
                    String personnelDepartment = "", personnelType = "", personnelName = "";
                    for (PersonnelManagement personnelManagement : personnelManagements) {
                        long personnelId = personnelManagement.getPersonnelId();
                        if(personnelId == taskUserId){
                            personnelUrl = personnelManagement.getPersonnelUrl();
                            personnelDepartment = personnelManagement.getPersonnelDepartment();
                            personnelType = personnelManagement.getPersonnelType();
                            personnelName = personnelManagement.getPersonnelName();
                            //人员类型 vip:VIP normal:普通 visitor:访客 blacklist:黑名单
                            if ("vip".equals(personnelType)){
                                personnelType = "VIP ";
                            }else if ("normal".equals(personnelType)){
                                personnelType = "普通 ";
                            }else if ("visitor".equals(personnelType)){
                                personnelType = "访客 ";
                            }else if ("blacklist".equals(personnelType)){
                                personnelType = "黑名单 ";
                            }
                            break;
                        }
                    }
                    jsonObject.set("time", taskTime);
                    jsonObject.set("personInfo", personnelDepartment + personnelType + personnelName);
                    jsonObject.set("alarmInfo", "随工"+alongWorkPedestrianDetectionAlarm.getAlarmInformation());
                    jsonObject.set("personUrl", personnelUrl);
                    jsonArray.add(jsonObject);
                    break;
                }
            }
        }

        jsonArray.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String taskTime1 = ((JSONObject) o1).getStr("time");
                String taskTime2 = ((JSONObject) o2).getStr("time");
                return taskTime2.compareTo(taskTime1);
            }
        });

        //此处添加分页
        int allSize = jsonArray.size();
        int startSize = pageSize * (pageNum-1);
        int endSize = pageSize * pageNum;

        JSONArray returnJsonArray = new JSONArray();

        for (int i = startSize; i < endSize && i < allSize; i++) {
            returnJsonArray.add(jsonArray.getJSONObject(i));
        }

        int totalPage = allSize/pageSize;
        if (allSize%pageSize>0){
            totalPage++;
        }
        PageBean pageBean = new PageBean();
        pageBean.setCurrentpage(pageNum);
        pageBean.setTotalPage(totalPage);
        pageBean.setTotalSize(allSize);
        pageBean.setContentList(returnJsonArray);
        pageBean.setPageSize(pageSize);
        pageBean.setListTotalSize(returnJsonArray.size());

        jsonObject = new JSONObject();
        jsonObject.set("faceAllCount", faceAllCount);
        jsonObject.set("faceNormalCount", faceNormalCount);
        jsonObject.set("faceAbnormalCount", faceAbnormalCount);
        jsonObject.set("abnormalCountSum", abnormalCountSum);
        jsonObject.set("visitorOverTimeCount", visitorOverTimeCount);
        jsonObject.set("mgxwCount", mgxwtCount);
        jsonObject.set("personBehaviorPageList", pageBean);

        xrjcycList = null;
        alongWorks = null;
        personnelManagements = null;
        alongWorkWithoutStartTimes = null;
        jsonArray = null;
        returnJsonArray = null;
        pageBean = null;

        return jsonObject;
    }

    /**
     * 获取检测项ai告警图片
     * @param roomId
     * @param pointName
     * @param aiType
     * @param dateStr
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/11/4 11:15
     */
    private JSONArray getDetectionAiPicture(long roomId, String pointName, String aiType, String dateStr){
        String tempPointName = pointName.replace("-","-上");
        //列表
        JSONArray jsonArray = new JSONArray();
        String detectionId = "";
        if("ai_alarm_light".equals(aiType)){
            detectionId = "alarm_light";
        }else if("ai_infrared".equals(aiType)){
            detectionId = "infrared";
        }else if("ai_fire_extinguisher".equals(aiType)){
            JSONArray tempArray;
            JSONObject tempObejct;
            String tempStr;
            List<TaskDetectionResult> list = taskDetectionResultDao.listFireExtinguisher(roomId, tempPointName, dateStr);
            for (TaskDetectionResult detectionResult : list) {
                String fireExtinguisher = detectionResult.getFireExtinguisher();
                tempObejct = JSONUtil.parseObj(fireExtinguisher);
                tempArray = tempObejct.getJSONArray("path");
                for (int i = 0; i < tempArray.size(); i++) {
                    tempStr = tempArray.getStr(i);
                    if (StringUtils.isEmpty(tempStr)){
                        continue;
                    }
                    jsonArray.add(tempStr);
                }
            }
            tempPointName = pointName.replace("-","-下");
            list = taskDetectionResultDao.listFireExtinguisher(roomId, tempPointName, dateStr);
            for (TaskDetectionResult detectionResult : list) {
                String fireExtinguisher = detectionResult.getFireExtinguisher();
                tempObejct = JSONUtil.parseObj(fireExtinguisher);
                tempArray = tempObejct.getJSONArray("path");
                for (int i = 0; i < tempArray.size(); i++) {
                    tempStr = tempArray.getStr(i);
                    if (StringUtils.isEmpty(tempStr)){
                        continue;
                    }
                    jsonArray.add(tempStr);
                }
            }
            return jsonArray;
        }

        JSONArray tempArray;
        JSONObject tempObejct;
        List<TaskDetectionResult> list = taskDetectionResultDao.list(roomId, tempPointName, detectionId, dateStr);
        for (TaskDetectionResult detectionResult : list) {
            if("ai_alarm_light".equals(aiType)){
                tempObejct = JSONUtil.parseObj(detectionResult.getAlarmLight());
                if (tempObejct.containsKey("alarm_light_merge_url")) {
                    jsonArray.add(tempObejct.getStr("alarm_light_merge_url"));
                }
            }else if("ai_infrared".equals(aiType)){
                tempArray = JSONUtil.parseArray(detectionResult.getInfrared());
                if (tempArray.size()>0){
                    tempObejct = tempArray.getJSONObject(0);
                    if (tempObejct.containsKey("infrared_merge_url")) {
                        jsonArray.add(tempObejct.getStr("infrared_merge_url"));
                    }
                }
            }
        }

        tempPointName = pointName.replace("-","-下");
        list = taskDetectionResultDao.list(roomId, tempPointName, detectionId, dateStr);
        for (TaskDetectionResult detectionResult : list) {
            if("ai_alarm_light".equals(aiType)){
                tempObejct = JSONUtil.parseObj(detectionResult.getAlarmLight());
                if (tempObejct.containsKey("alarm_light_merge_url")) {
                    jsonArray.add(tempObejct.getStr("alarm_light_merge_url"));
                }
            }else if("ai_infrared".equals(aiType)){
                tempArray = JSONUtil.parseArray(detectionResult.getInfrared());
                if (tempArray.size()>0){
                    tempObejct = tempArray.getJSONObject(0);
                    if (tempObejct.containsKey("infrared_merge_url")) {
                        jsonArray.add(tempObejct.getStr("infrared_merge_url"));
                    }
                }
            }
        }

        return jsonArray;
    }

    /**
     * 获取红外数据
     * @param infrared
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/6/21 8:47
     */
    private void  getInfraredData(String infrared, JSONObject jsonObject){
        double value = 0;
        JSONArray tempArray = JSONUtil.parseArray(infrared);
        boolean abnormalFlag = false;
        String infraredMergeUrl = null;
        JSONObject tempObject;
        for (int i = 0; i < tempArray.size(); i++) {
            tempObject = tempArray.getJSONObject(i);
            if(tempObject.containsKey("status")){
                if(i==0){
                    if(tempObject.containsKey("infrared_merge_url")){
                        infraredMergeUrl = tempObject.getStr("infrared_merge_url");
                        infraredMergeUrl = commonService.url2Https(infraredMergeUrl);
                    }
                }
                if(DetectionResult.ABNORMAL.equals(tempObject.getStr("status"))){
                    abnormalFlag = true;
                }
                if(tempObject.getDouble("max")>value){
                    value = tempObject.getDouble("max");
                }
            }
        }

        tempObject = new JSONObject();
        tempObject.set("infraredMergeUrl", infraredMergeUrl);
        if(value == 0){
            tempObject.set("value", "-");
        }else{
            tempObject.set("value", value);
        }
        if(abnormalFlag){
            tempObject.set("status", DetectionResult.ABNORMAL);
            jsonObject.set(Detection.INFRARED, tempObject);
        }else{
            tempObject.set("status", DetectionResult.NORMAL);
            jsonObject.set(Detection.INFRARED, tempObject);
        }
    }

    /**
     * 获取报警灯数据
     * @param alarmLightStr
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/6/21 8:46
     */
    @SuppressWarnings("AlibabaUndefineMagicConstant")
    private void getAlarmLightData(String alarmLightStr, JSONObject jsonObject){
        String alarmLightMergeUrl = null;
        JSONObject detectedObject = JSONUtil.parseObj(alarmLightStr);
        JSONObject tempObject = new JSONObject();
        if(!detectedObject.containsKey("status")){
            tempObject.set("status", DetectionResult.NORMAL);
            jsonObject.set(Detection.ALARMLIGHT, tempObject);
        }else{
            if(detectedObject.containsKey("alarm_light_merge_url")){
                alarmLightMergeUrl = detectedObject.getStr("alarm_light_merge_url");
                alarmLightMergeUrl = commonService.url2Https(alarmLightMergeUrl);
                tempObject.set("alarmLightMergeUrl", alarmLightMergeUrl);
            }
            tempObject.set("status", detectedObject.getStr("status"));
            jsonObject.set(Detection.ALARMLIGHT, tempObject);
        }
    }

    /**
     * 获取传感器数据
     * @param sensor
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/6/21 8:43
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    private void getSensorData(String sensor, JSONObject jsonObject){
        JSONObject detectedObject = JSONUtil.parseObj(sensor);
        Object o;
        if (detectedObject.containsKey(Detection.TEMPERATURE)) {
            o = detectedObject.get(Detection.TEMPERATURE);
        }else{
            o = 0;
        }
        JSONObject tempObject = new JSONObject();
        if(o instanceof Number){
            tempObject.set("value", o);
            tempObject.set("status", DetectionResult.NORMAL);
            jsonObject.set(Detection.TEMPERATURE, tempObject);
        }else{
            tempObject.set("value", ((JSONObject)o).getDouble("value"));
            tempObject.set("status", ((JSONObject)o).getStr("status"));
            tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
            jsonObject.set(Detection.TEMPERATURE, tempObject);
        }

        if (detectedObject.containsKey(Detection.HUMIDITY)) {
            o = detectedObject.get(Detection.HUMIDITY);
        }else{
            o = 0;
        }
        tempObject = new JSONObject();
        if(o instanceof Number){
            tempObject.set("value", o);
            tempObject.set("status", DetectionResult.NORMAL);
            jsonObject.set(Detection.HUMIDITY, tempObject);
        }else{
            tempObject.set("value", ((JSONObject)o).getDouble("value"));
            tempObject.set("status", ((JSONObject)o).getStr("status"));
            tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
            jsonObject.set(Detection.HUMIDITY, tempObject);
        }

        if (detectedObject.containsKey(Detection.NOISE)) {
            o = detectedObject.get(Detection.NOISE);
        }else{
            o = 0;
        }
        tempObject = new JSONObject();
        if(o instanceof Number){
            tempObject.set("value", o);
            tempObject.set("status", DetectionResult.NORMAL);
            jsonObject.set(Detection.NOISE, tempObject);
        }else{
            tempObject.set("value", ((JSONObject)o).getDouble("value"));
            tempObject.set("status", ((JSONObject)o).getStr("status"));
            tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
            jsonObject.set(Detection.NOISE, tempObject);
        }

        if (detectedObject.containsKey(Detection.PM2P5)) {
            o = detectedObject.get(Detection.PM2P5);
        }else{
            o = 0;
        }
        tempObject = new JSONObject();
        if(o instanceof Number){
            tempObject.set("value", o);
            tempObject.set("status", DetectionResult.NORMAL);
            jsonObject.set(Detection.PM2P5, tempObject);
        }else{
            tempObject.set("value", ((JSONObject)o).getDouble("value"));
            tempObject.set("status", ((JSONObject)o).getStr("status"));
            tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
            jsonObject.set(Detection.PM2P5, tempObject);
        }

        if (detectedObject.containsKey(Detection.SMOKE)) {
            o = detectedObject.get(Detection.SMOKE);
        }else{
            o = 0;
        }
        tempObject = new JSONObject();
        if(o instanceof Number){
            tempObject.set("value", o);
            tempObject.set("status", DetectionResult.NORMAL);
            jsonObject.set(Detection.SMOKE, tempObject);
        }else{
            tempObject.set("value", ((JSONObject)o).getDouble("value"));
            tempObject.set("status", ((JSONObject)o).getStr("status"));
            tempObject.set("thresholdLevel", ((JSONObject)o).getStr("thresholdLevel"));
            jsonObject.set(Detection.SMOKE, tempObject);
        }
    }
}
