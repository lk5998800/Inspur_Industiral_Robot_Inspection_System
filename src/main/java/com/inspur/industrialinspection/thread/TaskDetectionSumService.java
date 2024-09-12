package com.inspur.industrialinspection.thread;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.Detection;
import com.inspur.code.DetectionResult;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.dao.TaskDetectionSumDao;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import com.inspur.industrialinspection.entity.TaskDetectionSum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 任务检测项汇总服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Slf4j
@Service
public class TaskDetectionSumService {

    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;
    @Autowired
    private TaskDetectionSumDao taskDetectionSumDao;
    @Autowired
    private RoomDetectionSumDayService roomDetectionSumDayService;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private RoomDetectionPointSumDayService roomDetectionPointSumDayService;

    /**
     * @param taskDetectionResult
     * @author kliu
     * @description 对检测项数据进行按任务汇总
     * @date 2022/4/7 19:46
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    public void taskDetectionSum(TaskDetectionResult taskDetectionResult) throws IOException {
        long instanceId = taskDetectionResult.getInstanceId();

        int sensorCount = 0;
        int infraredPointCount = 0;
        int infraredAllCount = 0;
        int alarmLightCount = 0;
        int fireExtinguisherCount = 0;

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
        int fireExtinguisherAbnormalCount = 0;

        //获取传感器中的所有数据
        List<TaskDetectionResult> list = taskDetectionResultDao.list(instanceId);
        JSONObject jsonObject;
        JSONArray jsonArray;
        for (TaskDetectionResult detectionResult : list) {
            //传感器数据处理
            String sensor = detectionResult.getSensor();
            if(!StringUtils.isEmpty(sensor)){
                sensorCount++;

                jsonObject = JSONUtil.parseObj(sensor);

                BigDecimal noise = getSensorValue(jsonObject, "noise");
                noiseList.add(noise);
                noiseSum = noiseSum.add(noise);

                BigDecimal pm2p5 = getSensorValue(jsonObject, "pm2p5");
                pm2p5List.add(pm2p5);
                pm2p5Sum = pm2p5Sum.add(pm2p5);

                BigDecimal smoke = getSensorValue(jsonObject, "smoke");
                smokeList.add(smoke);
                smokeSum = smokeSum.add(smoke);

                BigDecimal humidity = getSensorValue(jsonObject, "humidity");
                humidityList.add(humidity);
                humiditySum = humiditySum.add(humidity);

                BigDecimal temperature = getSensorValue(jsonObject, "temperature");
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
            //告警信息中，我们认为同一个机柜处如果多个高度都报警，此时仅认为会有1个检测点位，该项中，单独计算
            String infrared = detectionResult.getInfrared();
            if(!StringUtils.isEmpty(infrared)){
                boolean abnormalCount = false;
                infraredPointCount++;
                jsonArray = JSONUtil.parseArray(infrared);
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if(jsonObject.containsKey("status")){
                        BigDecimal infraredValue = getInfraredValue(jsonObject);
                        infraredSum = infraredSum.add(infraredValue);
                        infraredAllCount++;
                        infraredList.add(infraredValue);
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

            String fireExtinguisher = detectionResult.getFireExtinguisher();
            if(!StringUtils.isEmpty(fireExtinguisher)){
                jsonObject = JSONUtil.parseObj(fireExtinguisher);
                fireExtinguisherCount++;
                if(jsonObject.containsKey("status")){
                    if(DetectionResult.ABNORMAL.equals(jsonObject.getStr("status"))){
                        fireExtinguisherAbnormalCount++;
                    }
                }else{
                    continue;
                }
            }
        }

        TaskDetectionSum taskDetectionSum = new TaskDetectionSum();
        taskDetectionSum.setInstanceId(instanceId);

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            //noise
            generateTaskDetectionSumAndSave(Detection.NOISE, noiseAbnormalCount, noiseSum, sensorCount, sensorCount, noiseList, taskDetectionSum);
            //pm2p5
            generateTaskDetectionSumAndSave(Detection.PM2P5, pm2p5AbnormalCount, pm2p5Sum, sensorCount, sensorCount, pm2p5List, taskDetectionSum);
            //smoke
            generateTaskDetectionSumAndSave(Detection.SMOKE, smokeAbnormalCount, smokeSum, sensorCount, sensorCount, smokeList, taskDetectionSum);
            //humidity
            generateTaskDetectionSumAndSave(Detection.HUMIDITY, humidityAbnormalCount, humiditySum, sensorCount, sensorCount, humidityList, taskDetectionSum);
            //temperature
            generateTaskDetectionSumAndSave(Detection.TEMPERATURE, temperatureAbnormalCount, temperatureSum, sensorCount, sensorCount, temperatureList, taskDetectionSum);
            //infrared
            generateTaskDetectionSumAndSave(Detection.INFRARED, infraredAbnormalCount, infraredSum, infraredPointCount, infraredAllCount, infraredList, taskDetectionSum);

            //alarmlight
            //报警灯没有那么多数据，直接处理
            taskDetectionSum.setDetectionId(Detection.ALARMLIGHT);
            taskDetectionSum.setAbnormalCount(alarmLightAbnormalCount);
            taskDetectionSum.setCount(alarmLightCount);
            taskDetectionSum.setAvg(zero);
            taskDetectionSum.setMin(zero);
            taskDetectionSum.setMax(zero);
            taskDetectionSum.setMedian(zero);
            saveSumData(taskDetectionSum);

            //灭火器
            //报警灯没有那么多数据，直接处理
            taskDetectionSum.setDetectionId(Detection.FIREEXTINGUISHER);
            taskDetectionSum.setAbnormalCount(fireExtinguisherAbnormalCount);
            taskDetectionSum.setCount(fireExtinguisherCount);
            taskDetectionSum.setAvg(zero);
            taskDetectionSum.setMin(zero);
            taskDetectionSum.setMax(zero);
            taskDetectionSum.setMedian(zero);
            saveSumData(taskDetectionSum);
            dataSourceTransactionManager.commit(transactionStatus);
        }catch(Exception e){
            e.printStackTrace();
            dataSourceTransactionManager.rollback(transactionStatus);
        }

        list = null;
        taskDetectionSum = null;
        noiseList = null;
        pm2p5List = null;
        smokeList = null;
        humidityList = null;
        temperatureList = null;
        infraredList = null;

        //当日数据汇总，此处去除线程开启，直接调用
        roomDetectionSumDayService.roomDetectionSumDay(taskDetectionResult);
        roomDetectionPointSumDayService.roomDetectionPointSumDay(taskDetectionResult);
    }

    /**
     * @param detectionId
     * @param abnormalCount
     * @param sum
     * @param pointCount
     * @param allCount
     * @param list
     * @param taskDetectionSum
     * @author kliu
     * @description 生成实体并保存
     * @date 2022/4/8 14:51
     */
    private void generateTaskDetectionSumAndSave(String detectionId, int abnormalCount, BigDecimal sum, int pointCount, int allCount, List list, TaskDetectionSum taskDetectionSum){
        BigDecimal zero = new BigDecimal(0);
        taskDetectionSum.setDetectionId(detectionId);
        taskDetectionSum.setAbnormalCount(abnormalCount);
        taskDetectionSum.setCount(pointCount);
        if(list.size() == 0){
            taskDetectionSum.setAvg(zero);
            taskDetectionSum.setMin(zero);
            taskDetectionSum.setMax(zero);
            taskDetectionSum.setMedian(zero);
        }else{
            Collections.sort(list);
            taskDetectionSum.setAvg(sum.divide(new BigDecimal(allCount),2,BigDecimal.ROUND_HALF_UP));
            taskDetectionSum.setMin(getMin(list));
            taskDetectionSum.setMax(getMax(list));
            taskDetectionSum.setMedian(getMedian(list));
        }
        saveSumData(taskDetectionSum);
    }
    /**
     * @param taskDetectionSum
     * @author kliu
     * @description 保存汇总数据
     * @date 2022/4/8 11:28
     */
    private void saveSumData(TaskDetectionSum taskDetectionSum){
        if (taskDetectionSumDao.checkExist(taskDetectionSum)){
            taskDetectionSumDao.update(taskDetectionSum);
        }else{
            taskDetectionSumDao.add(taskDetectionSum);
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
