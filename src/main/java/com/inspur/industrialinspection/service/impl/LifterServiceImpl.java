package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.LifterService;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: kliu
 * @description: 升降杆相关服务
 * @date: 2022/8/11 10:35
 */
@SuppressWarnings("ALL")
@Service
@Slf4j
public class LifterServiceImpl implements LifterService{

    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private CommonService commonService;

    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public void issuedLifterTask(long robotId, int count) {

        try {
            JSONArray rowPointActionArray = new JSONArray();
            JSONArray detectionArray = new JSONArray(), pointActionArray = new JSONArray();

            JSONObject param;
            Map detectionMap;
            detectionMap = new LinkedHashMap();
            detectionMap.put("action", "init_lifter");
            detectionArray.add(new JSONObject(detectionMap));
            String direction;
            for (int i = 0; i <count; i++){
                if (i%2==0){
                    direction = "up";
                    param = new JSONObject();
                    detectionMap = new LinkedHashMap();
                    param.set("distance", 60);
                    param.set("direction", direction);
                    detectionMap.put("action", "lifter");
                    detectionMap.put("param", param);
                    detectionArray.add(new JSONObject(detectionMap));

                    param = new JSONObject();
                    detectionMap = new LinkedHashMap();
                    param.set("distance", 60);
                    param.set("direction", direction);
                    detectionMap.put("action", "lifter");
                    detectionMap.put("param", param);
                    detectionArray.add(new JSONObject(detectionMap));

                    param = new JSONObject();
                    detectionMap = new LinkedHashMap();
                    param.set("distance", 60);
                    param.set("direction", direction);
                    detectionMap.put("action", "lifter");
                    detectionMap.put("param", param);
                    detectionArray.add(new JSONObject(detectionMap));
                }else{
                    direction = "down";
                    param = new JSONObject();
                    detectionMap = new LinkedHashMap();
                    param.set("distance", 60);
                    param.set("direction", direction);
                    detectionMap.put("action", "lifter");
                    detectionMap.put("param", param);
                    detectionArray.add(new JSONObject(detectionMap));

                    param = new JSONObject();
                    detectionMap = new LinkedHashMap();
                    param.set("distance", 60);
                    param.set("direction", direction);
                    detectionMap.put("action", "lifter");
                    detectionMap.put("param", param);
                    detectionArray.add(new JSONObject(detectionMap));

                    param = new JSONObject();
                    detectionMap = new LinkedHashMap();
                    param.set("distance", 60);
                    param.set("direction", direction);
                    detectionMap.put("action", "lifter");
                    detectionMap.put("param", param);
                    detectionArray.add(new JSONObject(detectionMap));
                }
            }

            Map pointActionMap = new LinkedHashMap();
            pointActionMap.put("point_name", "A01");
            pointActionMap.put("detection_item", detectionArray);
            rowPointActionArray.add(new JSONObject(pointActionMap));

            pointActionArray.add(rowPointActionArray);

            Map map = new LinkedHashMap<String, Object>();
            map.put("task_time", DateUtil.now());
            map.put("task_id", 277);
            map.put("robot_id", robotId);
            map.put("run_mode", "normal");
            map.put("point_action_list", pointActionArray);

            Map returnMap = new LinkedHashMap<String, Object>();
            returnMap.put("data", map);
            String json = new JSONObject(returnMap).toString();
            log.info(json);
            String issuedStr = commonService.gzipCompress(json).replace("\n", "").replace("\r", "");
            mqttPushClient.publish("industrial_robot_issued/"+robotId,issuedStr);
            pointActionArray = null;
            returnMap = null;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }
}
