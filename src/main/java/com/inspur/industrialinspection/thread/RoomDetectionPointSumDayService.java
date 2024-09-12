package com.inspur.industrialinspection.thread;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.industrialinspection.dao.RoomDetectionPointSumDayDao;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.dao.TaskInfoDao;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.entity.RoomDetectionPointSumDay;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import com.inspur.industrialinspection.entity.TaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author kliu
 * @description 按机房、日期汇总机房检测项信息
 * @date 2022/4/15 11:32
 */
@Slf4j
@Service
public class RoomDetectionPointSumDayService {
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private RoomDetectionPointSumDayDao roomDetectionPointSumDayDao;
    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;

    /**
     * 报告初始化
     * @param roomId
     * @return void
     * @author kliu
     * @date 2022/11/9 9:28
     */
    public void newReportDataInit(long roomId){
        List<TaskDetectionResult> taskDetectionResults = taskDetectionResultDao.listByRoomIdAndDate(roomId, "2022-10-01");
        for (TaskDetectionResult taskDetectionResult : taskDetectionResults) {
            roomDetectionPointSumDay(taskDetectionResult);
        }
    }

    /**
     * @param taskDetectionResult
     * @author kliu
     * @description 对检测项数据进行按日汇总
     * @date 2022/4/7 19:46
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    public void roomDetectionPointSumDay(TaskDetectionResult taskDetectionResult) {
        long instanceId = taskDetectionResult.getInstanceId();
        TaskInstance taskInstance = taskInstanceDao.getDetlById(instanceId);
        long taskId = taskInstance.getTaskId();
        long roomId = taskInfoDao.getDetlById(taskId).getRoomId();

        String startTime = taskInstance.getStartTime();
        //以任务开始时间作为检测日期计算汇总数据，跨天的任务，也以开始时间对应天数进行
        String detectionDate = startTime.substring(0, 10);

        int sensorCount = 0;
        int infraredCount = 0;
        int alarmLightCount = 0;
        int fireExtinguisherCount = 0;

        BigDecimal zero = new BigDecimal(0);

        BigDecimal noiseMax = zero;
        BigDecimal pm2p5Max = zero;
        BigDecimal smokeMax = zero;
        BigDecimal humidityMax = zero;
        BigDecimal temperatureMax = zero;
        BigDecimal infraredMax = zero;
        BigDecimal noiseMin = zero;
        BigDecimal pm2p5Min = zero;
        BigDecimal smokeMin = zero;
        BigDecimal humidityMin = zero;
        BigDecimal temperatureMin = zero;
        BigDecimal infraredMin = zero;

        Boolean noiseMaxAbnormal = false;
        Boolean pm2p5MaxAbnormal = false;
        Boolean smokeMaxAbnormal = false;
        Boolean humidityMaxAbnormal = false;
        Boolean temperatureMaxAbnormal = false;
        Boolean infraredMaxAbnormal = false;
        Boolean noiseMinAbnormal = false;
        Boolean pm2p5MinAbnormal = false;
        Boolean smokeMinAbnormal = false;
        Boolean humidityMinAbnormal = false;
        Boolean temperatureMinAbnormal = false;
        Boolean infraredMinAbnormal = false;

        boolean pointHasFireDetection = false;

        int noiseAbnormalCount = 0;
        int pm2p5AbnormalCount = 0;
        int smokeAbnormalCount = 0;
        int humidityAbnormalCount = 0;
        int temperatureAbnormalCount = 0;
        int infraredAbnormalCount = 0;
        int alarmLightAbnormalCount = 0;
        int fireExtinguisherAbnormalCount = 0;

        //获取当前点位对应的所有的数据
        List<TaskDetectionResult> list = taskDetectionResultDao.listByRoomIdAndDate(roomId, detectionDate, taskDetectionResult.getPointName());
        JSONObject detectionObject, detectionDetlObject;
        JSONArray jsonArray;

        RoomDetectionPointSumDay roomDetectionPointSumDay = new RoomDetectionPointSumDay();
        roomDetectionPointSumDay.setRoomId(roomId);
        roomDetectionPointSumDay.setDetectionDate(detectionDate);

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        BigDecimal noise, pm2p5, smoke, humidity, temperature;

        String pointName= taskDetectionResult.getPointName();
        //计算该点位的汇总数据
        for (TaskDetectionResult detectionResult : list) {
            //传感器数据处理
            String sensor = detectionResult.getSensor();
            if(!StringUtils.isEmpty(sensor)){
                sensorCount++;

                detectionObject = JSONUtil.parseObj(sensor);
                Object obj = detectionObject.getObj(Detection.NOISE);
                if (obj instanceof Number) {
                    //数字类型，异常与否还不知道，此时不改对应的状态
                    noise = getSensorValue(detectionObject, Detection.NOISE);
                    if (noiseMax.compareTo(noise)<0){
                        noiseMax = noise;
                    }
                    if (noiseMin.compareTo(noise)>0){
                        noiseMin = noise;
                    }
                }else{
                    detectionDetlObject = JSONUtil.parseObj(obj);
                    noise = detectionDetlObject.getBigDecimal("value");
                    //此处存有阈值信息，将对应的阈值取出，重新计算对应的值是正常还是异常
                    if (noiseMax.compareTo(noise)<0){
                        noiseMax = noise;
                        //如果当前最大值正常，则需要重新计算新的最大值是否正常
                        //异常则不计算，只更新值
                        if (!noiseMaxAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, noiseMax)){
                                noiseMaxAbnormal = true;
                            }
                        }
                    }
                    if (noiseMin.compareTo(noise)>0){
                        noiseMin = noise;
                        //判断最小值是正常还是异常
                        if (!noiseMinAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, noiseMin)){
                                noiseMinAbnormal = true;
                            }
                        }
                    }
                }

                obj = detectionObject.getObj(Detection.PM2P5);
                if (obj instanceof Number) {
                    //数字类型，异常与否还不知道，此时不改对应的状态
                    pm2p5 = getSensorValue(detectionObject, Detection.PM2P5);
                    if (pm2p5Max.compareTo(pm2p5)<0){
                        pm2p5Max = pm2p5;
                    }
                    if (pm2p5Min.compareTo(pm2p5)>0){
                        pm2p5Min = pm2p5;
                    }
                }else{
                    detectionDetlObject = JSONUtil.parseObj(obj);
                    pm2p5 = detectionDetlObject.getBigDecimal("value");
                    //此处存有阈值信息，将对应的阈值取出，重新计算对应的值是正常还是异常
                    if (pm2p5Max.compareTo(pm2p5)<0){
                        pm2p5Max = pm2p5;
                        //如果当前最大值正常，则需要重新计算新的最大值是否正常
                        //异常则不计算，只更新值
                        if (!pm2p5MaxAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, pm2p5Max)){
                                pm2p5MaxAbnormal = true;
                            }
                        }
                    }
                    if (pm2p5Min.compareTo(pm2p5)>0){
                        pm2p5Min = pm2p5;
                        //判断最小值是正常还是异常
                        if (!pm2p5MinAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, pm2p5Min)){
                                pm2p5MinAbnormal = true;
                            }
                        }
                    }
                }

                obj = detectionObject.getObj(Detection.SMOKE);
                if (obj instanceof Number) {
                    //数字类型，异常与否还不知道，此时不改对应的状态
                    smoke = getSensorValue(detectionObject, Detection.SMOKE);
                    if (smokeMax.compareTo(smoke)<0){
                        smokeMax = smoke;
                    }
                    if (smokeMin.compareTo(smoke)>0){
                        smokeMin = smoke;
                    }
                }else{
                    detectionDetlObject = JSONUtil.parseObj(obj);
                    smoke = detectionDetlObject.getBigDecimal("value");
                    //此处存有阈值信息，将对应的阈值取出，重新计算对应的值是正常还是异常
                    if (smokeMax.compareTo(smoke)<0){
                        smokeMax = smoke;
                        //如果当前最大值正常，则需要重新计算新的最大值是否正常
                        //异常则不计算，只更新值
                        if (!smokeMaxAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, smokeMax)){
                                smokeMaxAbnormal = true;
                            }
                        }
                    }
                    if (smokeMin.compareTo(smoke)>0){
                        smokeMin = smoke;
                        //判断最小值是正常还是异常
                        if (!smokeMinAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, smokeMin)){
                                smokeMinAbnormal = true;
                            }
                        }
                    }
                }

                obj = detectionObject.getObj(Detection.HUMIDITY);
                if (obj instanceof Number) {
                    //数字类型，异常与否还不知道，此时不改对应的状态
                    humidity = getSensorValue(detectionObject, Detection.HUMIDITY);
                    if (humidityMax.compareTo(humidity)<0){
                        humidityMax = humidity;
                    }
                    if (humidityMin.compareTo(humidity)>0){
                        humidityMin = humidity;
                    }
                }else{
                    detectionDetlObject = JSONUtil.parseObj(obj);
                    humidity = detectionDetlObject.getBigDecimal("value");
                    //此处存有阈值信息，将对应的阈值取出，重新计算对应的值是正常还是异常
                    if (humidityMax.compareTo(humidity)<0){
                        humidityMax = humidity;
                        //如果当前最大值正常，则需要重新计算新的最大值是否正常
                        //异常则不计算，只更新值
                        if (!humidityMaxAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, humidityMax)){
                                humidityMaxAbnormal = true;
                            }
                        }
                    }
                    if (humidityMin.compareTo(humidity)>0){
                        humidityMin = humidity;
                        //判断最小值是正常还是异常
                        if (!humidityMinAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, humidityMin)){
                                humidityMinAbnormal = true;
                            }
                        }
                    }
                }

                obj = detectionObject.getObj(Detection.TEMPERATURE);
                if (obj instanceof Number) {
                    //数字类型，异常与否还不知道，此时不改对应的状态
                    temperature = getSensorValue(detectionObject, Detection.TEMPERATURE);
                    if (temperatureMax.compareTo(temperature)<0){
                        temperatureMax = temperature;
                    }
                    if (temperatureMin.compareTo(temperature)>0){
                        temperatureMin = temperature;
                    }
                }else{
                    detectionDetlObject = JSONUtil.parseObj(obj);
                    temperature = detectionDetlObject.getBigDecimal("value");
                    //此处存有阈值信息，将对应的阈值取出，重新计算对应的值是正常还是异常
                    if (temperatureMax.compareTo(temperature)<0){
                        temperatureMax = temperature;
                        //如果当前最大值正常，则需要重新计算新的最大值是否正常
                        //异常则不计算，只更新值
                        if (!temperatureMaxAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, temperatureMax)){
                                temperatureMaxAbnormal = true;
                            }
                        }
                    }
                    if (temperatureMin.compareTo(temperature)>0){
                        temperatureMin = temperature;
                        //判断最小值是正常还是异常
                        if (!temperatureMinAbnormal){
                            //判断最大值是正常还是异常
                            String threshold = detectionDetlObject.getStr("threshold");
                            if (detectionAbnormal(threshold, temperatureMin)){
                                temperatureMinAbnormal = true;
                            }
                        }
                    }
                }

                if(detectionAbnormal(detectionObject, Detection.NOISE)){
                    noiseAbnormalCount++;
                }
                if(detectionAbnormal(detectionObject, Detection.PM2P5)){
                    pm2p5AbnormalCount++;
                }
                if(detectionAbnormal(detectionObject, Detection.SMOKE)){
                    smokeAbnormalCount++;
                }
                if(detectionAbnormal(detectionObject, Detection.HUMIDITY)){
                    humidityAbnormalCount++;
                }
                if(detectionAbnormal(detectionObject, Detection.TEMPERATURE)){
                    temperatureAbnormalCount++;
                }
            }

            //红外测温数据处理
            //告警信息中，我们认为同一个机柜处如果多个高度都报警，此时异常检测点位记为一个点位
            String infrared = detectionResult.getInfrared();
            if(!StringUtils.isEmpty(infrared)){
                boolean abnormalCountSave = false;
                infraredCount++;
                jsonArray = JSONUtil.parseArray(infrared);
                for (int i = 0; i < jsonArray.size(); i++) {
                    detectionDetlObject = jsonArray.getJSONObject(i);
                    if(detectionDetlObject.containsKey("status")){
                        BigDecimal infraredValue = getInfraredValue(detectionDetlObject);

                        //此处存有阈值信息，将对应的阈值取出，重新计算对应的值是正常还是异常
                        if (infraredMax.compareTo(infraredValue)<0){
                            infraredMax = infraredValue;
                            //如果当前最大值正常，则需要重新计算新的最大值是否正常
                            //异常则不计算，只更新值
                            if (!infraredMaxAbnormal){
                                //判断最大值是正常还是异常
                                String threshold = detectionDetlObject.getStr("threshold");
                                if (detectionAbnormal(threshold, infraredMax)){
                                    infraredMaxAbnormal = true;
                                }
                            }
                        }
                        if (infraredMin.compareTo(infraredValue)>0){
                            infraredMin = infraredValue;
                            //判断最小值是正常还是异常
                            if (!infraredMinAbnormal){
                                //判断最大值是正常还是异常
                                String threshold = detectionDetlObject.getStr("threshold");
                                if (detectionAbnormal(threshold, infraredMin)){
                                    infraredMinAbnormal = true;
                                }
                            }
                        }

                        if(DetectionResult.ABNORMAL.equals(detectionDetlObject.getStr("status"))){
                            if(!abnormalCountSave){
                                infraredAbnormalCount++;
                                abnormalCountSave = true;
                            }
                        }
                    }else{
                        continue;
                    }
                }
            }

            String alarmLight = detectionResult.getAlarmLight();
            if(!StringUtils.isEmpty(alarmLight)){
                detectionDetlObject = JSONUtil.parseObj(alarmLight);
                alarmLightCount++;
                if(detectionDetlObject.containsKey("status")){
                    if(DetectionResult.ABNORMAL.equals(detectionDetlObject.getStr("status"))){
                        alarmLightAbnormalCount++;
                    }
                }else{
                    continue;
                }
            }

            String fireExtinguisher = detectionResult.getFireExtinguisher();
            if(!StringUtils.isEmpty(fireExtinguisher)){
                detectionDetlObject = JSONUtil.parseObj(fireExtinguisher);
                fireExtinguisherCount++;
                if(detectionDetlObject.containsKey("status")){
                    if(DetectionResult.ABNORMAL.equals(detectionDetlObject.getStr("status"))){
                        fireExtinguisherAbnormalCount++;
                    }
                }else{
                    continue;
                }
            }
        }

        roomDetectionPointSumDay.setPointName(pointName);

        try {
            //噪声
            roomDetectionPointSumDay.setDetectionId(Detection.NOISE);
            roomDetectionPointSumDay.setMax(noiseMax);
            roomDetectionPointSumDay.setMin(noiseMin);
            roomDetectionPointSumDay.setAbnormalCount(noiseAbnormalCount);
            roomDetectionPointSumDay.setCount(sensorCount);
            roomDetectionPointSumDay.setMinAbnormal(noiseMinAbnormal?"1":"0");
            roomDetectionPointSumDay.setMaxAbnormal(noiseMaxAbnormal?"1":"0");
            saveSumData(roomDetectionPointSumDay);

            //pm2p5
            roomDetectionPointSumDay.setDetectionId(Detection.PM2P5);
            roomDetectionPointSumDay.setMax(pm2p5Max);
            roomDetectionPointSumDay.setMin(pm2p5Min);
            roomDetectionPointSumDay.setAbnormalCount(pm2p5AbnormalCount);
            roomDetectionPointSumDay.setCount(sensorCount);
            roomDetectionPointSumDay.setMinAbnormal(pm2p5MinAbnormal?"1":"0");
            roomDetectionPointSumDay.setMaxAbnormal(pm2p5MaxAbnormal?"1":"0");
            saveSumData(roomDetectionPointSumDay);

            //smoke
            roomDetectionPointSumDay.setDetectionId(Detection.SMOKE);
            roomDetectionPointSumDay.setMax(smokeMax);
            roomDetectionPointSumDay.setMin(smokeMin);
            roomDetectionPointSumDay.setAbnormalCount(smokeAbnormalCount);
            roomDetectionPointSumDay.setCount(sensorCount);
            roomDetectionPointSumDay.setMinAbnormal(smokeMinAbnormal?"1":"0");
            roomDetectionPointSumDay.setMaxAbnormal(smokeMaxAbnormal?"1":"0");
            saveSumData(roomDetectionPointSumDay);

            //humidity
            roomDetectionPointSumDay.setDetectionId(Detection.HUMIDITY);
            roomDetectionPointSumDay.setMax(humidityMax);
            roomDetectionPointSumDay.setMin(humidityMin);
            roomDetectionPointSumDay.setAbnormalCount(humidityAbnormalCount);
            roomDetectionPointSumDay.setCount(sensorCount);
            roomDetectionPointSumDay.setMinAbnormal(humidityMinAbnormal?"1":"0");
            roomDetectionPointSumDay.setMaxAbnormal(humidityMaxAbnormal?"1":"0");
            saveSumData(roomDetectionPointSumDay);

            //temperature
            roomDetectionPointSumDay.setDetectionId(Detection.TEMPERATURE);
            roomDetectionPointSumDay.setMax(temperatureMax);
            roomDetectionPointSumDay.setMin(temperatureMin);
            roomDetectionPointSumDay.setAbnormalCount(temperatureAbnormalCount);
            roomDetectionPointSumDay.setCount(sensorCount);
            roomDetectionPointSumDay.setMinAbnormal(temperatureMinAbnormal?"1":"0");
            roomDetectionPointSumDay.setMaxAbnormal(temperatureMaxAbnormal?"1":"0");
            saveSumData(roomDetectionPointSumDay);

            //infrared
            roomDetectionPointSumDay.setDetectionId(Detection.INFRARED);
            roomDetectionPointSumDay.setMax(infraredMax);
            roomDetectionPointSumDay.setMin(infraredMin);
            roomDetectionPointSumDay.setAbnormalCount(infraredAbnormalCount);
            roomDetectionPointSumDay.setCount(infraredCount);
            roomDetectionPointSumDay.setMinAbnormal(infraredMinAbnormal?"1":"0");
            roomDetectionPointSumDay.setMaxAbnormal(infraredMaxAbnormal?"1":"0");
            saveSumData(roomDetectionPointSumDay);

            //alarmlight
            //报警灯没有那么多数据，直接处理
            roomDetectionPointSumDay.setDetectionId(Detection.ALARMLIGHT);
            roomDetectionPointSumDay.setAbnormalCount(alarmLightAbnormalCount);
            roomDetectionPointSumDay.setCount(alarmLightCount);
            roomDetectionPointSumDay.setMin(zero);
            roomDetectionPointSumDay.setMax(zero);
            roomDetectionPointSumDay.setMinAbnormal("0");
            roomDetectionPointSumDay.setMaxAbnormal("0");
            saveSumData(roomDetectionPointSumDay);

            //灭火器-灭火器检测仅添加检测次数即可
            if (pointHasFireDetection){
                roomDetectionPointSumDay.setDetectionId(Detection.FIREEXTINGUISHER);
                roomDetectionPointSumDay.setAbnormalCount(fireExtinguisherAbnormalCount);
                roomDetectionPointSumDay.setCount(fireExtinguisherCount);
                roomDetectionPointSumDay.setMin(zero);
                roomDetectionPointSumDay.setMax(zero);
                roomDetectionPointSumDay.setMinAbnormal("0");
                roomDetectionPointSumDay.setMaxAbnormal("0");
                saveSumData(roomDetectionPointSumDay);
            }

            dataSourceTransactionManager.commit(transactionStatus);
        } catch (Exception exception) {
            exception.printStackTrace();
            dataSourceTransactionManager.rollback(transactionStatus);
        }
    }

    /**
     * 判断检测项是否异常
     * @param threshold
     * @param value
     * @return boolean
     * @author kliu
     * @date 2022/11/8 16:30
     */
    private boolean detectionAbnormal(String threshold, BigDecimal value){
        if (StringUtils.isEmpty(threshold)){
            log.error("机房检测项点位生成报错：传入的阈值为空");
            return false;
        }
        //高低双阈值
        if(StrUtil.count(threshold, "-")>0){
            String[] split = threshold.split("-");
            BigDecimal lower = new BigDecimal(split[0]);
            BigDecimal upper = new BigDecimal(split[1]);
            if (value.compareTo(lower)<0 || value.compareTo(upper) > 0){
                return true;
            }else{
                return false;
            }
        }else{
            BigDecimal upper = new BigDecimal(threshold);
            if (value.compareTo(upper) > 0){
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * @param roomDetectionPointSumDay
     * @author kliu
     * @description 保存汇总数据
     * @date 2022/4/8 11:28
     */
    private void saveSumData(RoomDetectionPointSumDay roomDetectionPointSumDay){
        if (roomDetectionPointSumDayDao.checkExist(roomDetectionPointSumDay)){
            roomDetectionPointSumDayDao.update(roomDetectionPointSumDay);
        }else{
            roomDetectionPointSumDayDao.add(roomDetectionPointSumDay);
        }
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
