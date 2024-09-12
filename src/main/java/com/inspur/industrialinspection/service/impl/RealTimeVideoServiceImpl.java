package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.industrialinspection.dao.ParkInfoDao;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.service.RealTimeVideoService;
import com.inspur.industrialinspection.service.RobotParamService;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 实时视频相关服务实现
 * @author: kliu
 * @date: 2022/5/6 11:30
 */
@Service
@Slf4j
public class RealTimeVideoServiceImpl implements RealTimeVideoService {
    private volatile static ConcurrentHashMap<String, JSONObject> mqttStream = new ConcurrentHashMap<String, JSONObject>();
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private RobotParamService robotParamService;

    @Override
    public String start(long roomId, long robotId) throws IOException {
        if (roomId==0  && robotId == 0) {
            throw new RuntimeException("机器人id和机房id不能同时为空");
        }
        if (roomId == 0){
            roomId = robotRoomDao.getRoomIdByRobotId(robotId);
        }
        return issued(roomId, "start");
    }

    @Override
    public void heart(long roomId, long robotId) throws IOException {
        if (roomId==0  && robotId == 0) {
            throw new RuntimeException("机器人id和机房id不能同时为空");
        }
        if (roomId == 0){
            roomId = robotRoomDao.getRoomIdByRobotId(robotId);
        }
        issued(roomId, "heart");
    }

    private String issued(long roomId, String type) throws IOException {
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        JSONObject robotParamObject = robotParamService.getRobotParam(robotId);
        int streamCameraNo = robotParamObject.getInt("stream_camera_no");
        String srsRtmpServerUrl = robotParamObject.getStr("srs_rtmp_server_url")+"/"+robotId;
        String srsWebrtcServerUrl = robotParamObject.getStr("srs_webrtc_server_url")+"/"+robotId;

        //缓存中如果存在机器人推流路径，则认为已经发起过推流开始或者心跳接口（推流处开始和心跳处理是一样的）
        if(mqttStream.containsKey(srsRtmpServerUrl)){
            return srsWebrtcServerUrl;
        }

        String topic = "industrial_robot_stream/"+type+"/"+robotId;
        JSONObject messageObject = new JSONObject();
        String uuid = IdUtil.simpleUUID();
        messageObject.set("uuid", uuid);
        messageObject.set("camera_no", streamCameraNo);
        messageObject.set("srs_rtmp_server_url", srsRtmpServerUrl);
        messageObject.set("seconds", 15);
        mqttPushClient.publish(topic, messageObject.toString());
        mqttStream.put(srsRtmpServerUrl, messageObject);
        return srsWebrtcServerUrl;
    }

    @PostConstruct
    public void initHeartThreadPool(){
        executor.submit(() -> {
            try {
                startOrHeart();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void startOrHeart() throws InterruptedException {
        JSONObject jsonObject;
        while (true){
            for (String key : mqttStream.keySet()){
                jsonObject = mqttStream.get(key);
                String uuid = IdUtil.simpleUUID();
                jsonObject.set("uuid", uuid);
                int seconds = jsonObject.getInt("seconds");
                if(seconds == 0){
                    mqttStream.remove(key);
                    continue;
                }
                seconds--;
                jsonObject.set("seconds", seconds);
            }
            Thread.sleep(1000);
        }
    }

    @Override
    public void receiveStreamResult(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        String uuid = jsonObject.getStr("uuid");
    }
}
