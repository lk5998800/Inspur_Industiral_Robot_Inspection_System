package com.inspur.industrialinspection.thread;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.industrialinspection.dao.RoomDetectionSumDayDao;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.dao.TaskInfoDao;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.entity.RoomDetectionSumDay;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import com.inspur.industrialinspection.entity.TaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author kliu
 * @description 按机房、日期汇总机房检测项信息
 * @date 2022/4/15 11:32
 */
@Slf4j
@Service
public class RoomDetectionSumDayService {
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private RoomDetectionSumDayDao roomDetectionSumDayDao;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;

    /**
     * 重置汇总数据，并重新计算汇总数据
     * @return void
     * @author kliu
     * @date 2022/7/29 10:52
     */
    public void resetAndSum(long roomId){
        roomDetectionSumDayDao.deleteAll(roomId);
        //获取该机房下的检测数据
        List<TaskInstance> instanceIds = taskInstanceDao.getAllInstanceByRoomId(roomId);
        List<TaskDetectionResult> list;
        TaskDetectionResult detectionResult;
        for (TaskInstance taskInstance : instanceIds) {
            Long instanceId = taskInstance.getInstanceId();
            list = taskDetectionResultDao.list(instanceId);
            if (list.size() > 0) {
                detectionResult = list.get(0);
                roomDetectionSumDay(detectionResult);
            }
        }
    }

    /**
     * @param taskDetectionResult
     * @author kliu
     * @description 对检测项数据进行按日汇总
     * @date 2022/4/7 19:46
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    public void roomDetectionSumDay(TaskDetectionResult taskDetectionResult) {
        long instanceId = taskDetectionResult.getInstanceId();
        TaskInstance taskInstance = taskInstanceDao.getDetlById(instanceId);
        long taskId = taskInstance.getTaskId();
        long roomId = taskInfoDao.getDetlById(taskId).getRoomId();

        String startTime = taskInstance.getStartTime();
        //以任务开始时间作为检测日期计算汇总数据，跨天的任务，也以开始时间对应天数进行
        String detectionDate = startTime.substring(0, 10);

        int sensorCount = 0;
        int infraredPointCount = 0;
        int infraredAllCount = 0;
        int alarmLightCount = 0;

        List<BigDecimal> noiseList = new ArrayList<BigDecimal>();
        List<BigDecimal> pm2p5List = new ArrayList<BigDecimal>();
        List<BigDecimal> smokeList = new ArrayList<BigDecimal>();
        List<BigDecimal> humidityList = new ArrayList<BigDecimal>();
        List<BigDecimal> temperatureList = new ArrayList<BigDecimal>();
        List<BigDecimal> infraredList = new ArrayList<BigDecimal>();

        BigDecimal noiseSum = new BigDecimal(0);
        BigDecimal pm2p5Sum = new BigDecimal(0);
        BigDecimal smokeSum = new BigDecimal(0);
        BigDecimal humiditySum = new BigDecimal(0);
        BigDecimal temperatureSum = new BigDecimal(0);
        BigDecimal infraredSum = new BigDecimal(0);
        BigDecimal zero = new BigDecimal(0);

        int noiseAbnormalCount = 0;
        int pm2p5AbnormalCount = 0;
        int smokeAbnormalCount = 0;
        int humidityAbnormalCount = 0;
        int temperatureAbnormalCount = 0;
        int infraredAbnormalCount = 0;
        int alarmLightAbnormalCount = 0;

        //获取所有数据
        List<TaskDetectionResult> list = taskDetectionResultDao.listByRoomIdAndDate(roomId, detectionDate);
        JSONObject jsonObject;
        JSONArray jsonArray;
        for (TaskDetectionResult detectionResult : list) {
            //传感器数据处理
            String sensor = detectionResult.getSensor();
            if(!StringUtils.isEmpty(sensor)){
                sensorCount++;

                jsonObject = JSONUtil.parseObj(sensor);

                BigDecimal noise = getSensorValue(jsonObject, Detection.NOISE);
                noiseList.add(noise);
                noiseSum = noiseSum.add(noise);

                BigDecimal pm2p5 = getSensorValue(jsonObject, Detection.PM2P5);
                pm2p5List.add(pm2p5);
                pm2p5Sum = pm2p5Sum.add(pm2p5);

                BigDecimal smoke = getSensorValue(jsonObject, Detection.SMOKE);
                smokeList.add(smoke);
                smokeSum = smokeSum.add(smoke);

                BigDecimal humidity = getSensorValue(jsonObject, Detection.HUMIDITY);
                humidityList.add(humidity);
                humiditySum = humiditySum.add(humidity);

                BigDecimal temperature = getSensorValue(jsonObject, Detection.TEMPERATURE);
                temperatureList.add(temperature);
                temperatureSum = temperatureSum.add(temperature);

                if(detectionAbnormal(jsonObject, Detection.NOISE)){
                    noiseAbnormalCount++;
                }
                if(detectionAbnormal(jsonObject, Detection.PM2P5)){
                    pm2p5AbnormalCount++;
                }
                if(detectionAbnormal(jsonObject, Detection.SMOKE)){
                    smokeAbnormalCount++;
                }
                if(detectionAbnormal(jsonObject, Detection.HUMIDITY)){
                    humidityAbnormalCount++;
                }
                if(detectionAbnormal(jsonObject, Detection.TEMPERATURE)){
                    temperatureAbnormalCount++;
                }
            }

            //红外测温数据处理
            //红外测温依据升降杆的高度不同 会产生多条数据，此处需要对所有数据都进行处理，包含中位数、平均数等
            //告警信息中，我们认为同一个机柜处如果多个高度都报警，此时会认为会有多个检测点位，该项中，单独计算
            String infrared = detectionResult.getInfrared();
            if(!StringUtils.isEmpty(infrared)){
                boolean abnormalCount = false;
                infraredPointCount++;
                jsonArray = JSONUtil.parseArray(infrared);
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if(jsonObject.containsKey("status")){
                        BigDecimal infraredValue = getInfraredValue(jsonObject);
                        infraredList.add(infraredValue);
                        infraredAllCount++;
                        infraredSum = infraredSum.add(infraredValue);
                        if(DetectionResult.ABNORMAL.equals(jsonObject.getStr("status"))){
                            if(!abnormalCount){
                                infraredAbnormalCount++;
                                abnormalCount = true;
                            }
                        }
                    }else{
                        continue;
                    }
                }
            }

            String alarmLight = detectionResult.getAlarmLight();
            if(!StringUtils.isEmpty(alarmLight)){
                jsonObject = JSONUtil.parseObj(alarmLight);
                alarmLightCount++;
                if(jsonObject.containsKey("status")){
                    if(DetectionResult.ABNORMAL.equals(jsonObject.getStr("status"))){
                        alarmLightAbnormalCount++;
                    }
                }else{
                    continue;
                }
            }
        }

        RoomDetectionSumDay roomDetectionSumDay = new RoomDetectionSumDay();
        roomDetectionSumDay.setRoomId(roomId);
        roomDetectionSumDay.setDetectionDate(detectionDate);

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            //noise
            generateRoomDetectionSumDayAndSave(Detection.NOISE, noiseAbnormalCount, noiseSum, sensorCount, sensorCount, noiseList, roomDetectionSumDay);
            //pm2p5
            generateRoomDetectionSumDayAndSave(Detection.PM2P5, pm2p5AbnormalCount, pm2p5Sum, sensorCount, sensorCount, pm2p5List, roomDetectionSumDay);
            //smoke
            generateRoomDetectionSumDayAndSave(Detection.SMOKE, smokeAbnormalCount, smokeSum, sensorCount, sensorCount, smokeList, roomDetectionSumDay);
            //humidity
            generateRoomDetectionSumDayAndSave(Detection.HUMIDITY, humidityAbnormalCount, humiditySum, sensorCount, sensorCount, humidityList, roomDetectionSumDay);
            //temperature
            generateRoomDetectionSumDayAndSave(Detection.TEMPERATURE, temperatureAbnormalCount, temperatureSum, sensorCount, sensorCount, temperatureList, roomDetectionSumDay);
            //infrared
            generateRoomDetectionSumDayAndSave(Detection.INFRARED, infraredAbnormalCount, infraredSum, infraredPointCount, infraredAllCount,infraredList, roomDetectionSumDay);

            //alarmlight
            //报警灯没有那么多数据，直接处理
            roomDetectionSumDay.setDetectionId(Detection.ALARMLIGHT);
            roomDetectionSumDay.setAbnormalCount(alarmLightAbnormalCount);
            roomDetectionSumDay.setCount(alarmLightCount);
            roomDetectionSumDay.setAvg(zero);
            roomDetectionSumDay.setMin(zero);
            roomDetectionSumDay.setMax(zero);
            roomDetectionSumDay.setMedian(zero);
            saveSumData(roomDetectionSumDay);
            dataSourceTransactionManager.commit(transactionStatus);
        }catch (Exception e){
            e.printStackTrace();
            dataSourceTransactionManager.rollback(transactionStatus);
        }



        list = null;
        roomDetectionSumDay = null;
        noiseList = null;
        pm2p5List = null;
        smokeList = null;
        humidityList = null;
        temperatureList = null;
        infraredList = null;
    }

    /**
     * @param detectionId
     * @param abnormalCount
     * @param sum
     * @param pointCount
     * @param allCount
     * @param list
     * @param roomDetectionSumDay
     * @author kliu
     * @description 生成实体并保存
     * @date 2022/4/8 14:51
     */
    private void generateRoomDetectionSumDayAndSave(String detectionId, int abnormalCount, BigDecimal sum, int pointCount, int allCount, List list, RoomDetectionSumDay roomDetectionSumDay){
        BigDecimal zero = new BigDecimal(0);
        roomDetectionSumDay.setDetectionId(detectionId);
        roomDetectionSumDay.setAbnormalCount(abnormalCount);
        roomDetectionSumDay.setCount(pointCount);
        if(list.size() == 0){
            roomDetectionSumDay.setAvg(zero);
            roomDetectionSumDay.setMin(zero);
            roomDetectionSumDay.setMax(zero);
            roomDetectionSumDay.setMedian(zero);
        }else{
            roomDetectionSumDay.setAvg(sum.divide(new BigDecimal(allCount),2,BigDecimal.ROUND_HALF_UP));
            Collections.sort(list);
            roomDetectionSumDay.setMin(getMin(list));
            roomDetectionSumDay.setMax(getMax(list));
            roomDetectionSumDay.setMedian(getMedian(list));
        }
        saveSumData(roomDetectionSumDay);
    }

    /**
     * @param roomDetectionSumDay
     * @author kliu
     * @description 保存汇总数据
     * @date 2022/4/8 11:28
     */
    private void saveSumData(RoomDetectionSumDay roomDetectionSumDay){
        if (roomDetectionSumDayDao.checkExist(roomDetectionSumDay)){
            roomDetectionSumDayDao.update(roomDetectionSumDay);
        }else{
            roomDetectionSumDayDao.add(roomDetectionSumDay);
        }
    }

    /**
     * @param list
     * @return java.math.BigDecimal
     * @author kliu
     * @description 取最大值
     * @date 2022/4/8 11:29
     */
    private BigDecimal getMax(List<BigDecimal> list){
        return list.get(list.size()-1);
    }

    private BigDecimal getMin(List<BigDecimal> list){
        return list.get(0);
    }

    /**
     * @param list
     * @return BigDecimal
     * @author kliu
     * @description 求中位数
     * @date 2022/4/7 19:51
     */
    private static BigDecimal getMedian(List<BigDecimal> list) {
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
     * @param jsonObject
     * @param key
     * @return java.math.BigDecimal
     * @author kliu
     * @description 获取传感器数据，含阈值处理的数据
     * @date 2022/4/7 19:46
     */
    private BigDecimal getSensorValue(JSONObject jsonObject, String key){
        Object o = jsonObject.get(key);
        if(o instanceof Number){
            return new BigDecimal(((Number) o).doubleValue());
        }
        return ((JSONObject)o).getBigDecimal("value");
    }

    /**
     * @param jsonObject
     * @return java.math.BigDecimal
     * @author kliu
     * @description 获取传感器数据，含阈值处理的数据
     * @date 2022/4/7 19:46
     */
    private BigDecimal getInfraredValue(JSONObject jsonObject){
        return jsonObject.getBigDecimal("value");
    }

    /**
     * @param jsonObject
     * @param key
     * @return boolean
     * @author kliu
     * @description 判断检测项对否异常
     * @date 2022/4/7 20:16
     */
    private boolean detectionAbnormal(JSONObject jsonObject, String key){
        Object o = jsonObject.get(key);
        if(o instanceof Number){
            return false;
        }
        return DetectionResult.ABNORMAL.equals(((JSONObject)o).getStr("status"));
    }
}
