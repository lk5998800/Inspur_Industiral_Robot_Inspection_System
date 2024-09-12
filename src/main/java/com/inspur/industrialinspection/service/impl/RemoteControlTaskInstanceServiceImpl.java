package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.RemoteControlTaskInstanceDao;
import com.inspur.industrialinspection.dao.RemoteControlTaskResultDao;
import com.inspur.industrialinspection.entity.RemoteControlTaskInstance;
import com.inspur.industrialinspection.entity.RemoteControlTaskResult;
import com.inspur.industrialinspection.service.RemoteControlTaskInstanceService;
import com.inspur.mqtt.MqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * @author: kliu
 * @description: 远程控制实例服务
 * @date: 2022/9/7 14:17
 */
@Service
@Slf4j
public class RemoteControlTaskInstanceServiceImpl implements RemoteControlTaskInstanceService {

    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RemoteControlTaskResultDao remoteControlTaskResultDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private RemoteControlTaskInstanceDao remoteControlTaskInstanceDao;

    @Override
    public void receiveTaskResult(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        JSONObject dataObject = (JSONObject) jsonObject.get("data");
        long instanceId = dataObject.getLong("task_id");
        long robotId = dataObject.getLong("robot_id");
        String startTime = dataObject.getStr("start_time");
        String endTime = dataObject.getStr("end_time");
        String uuid = dataObject.getStr("uuid");

        if (!remoteControlTaskInstanceDao.checkExist(instanceId)){
            throw new RuntimeException("任务id不存在，请检查传入的数据");
        }

        JSONObject detectionObject = dataObject.getJSONObject("detection_data");
        JSONObject frontPictureObejct;
        if (detectionObject.containsKey("front_picture")){
            frontPictureObejct = detectionObject.getJSONObject("front_picture");
            String code = frontPictureObejct.getStr("code");
            String successCode = "0";
            if (!successCode.equals(code)) {
                log.error("机器人前置拍照数据异常，异常原因：" + frontPictureObejct.getStr("message"));
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            if (!frontPictureObejct.containsKey(ParaKey.DATA)){
                log.error("机器人前置拍照数据异常，异常原因：不存在data");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            JSONObject frontPictureDataObejct = frontPictureObejct.getJSONObject("data");
            if (!frontPictureDataObejct.containsKey(ParaKey.PATH)) {
                log.error("机器人前置拍照数据异常，异常原因：不存在前置拍照图片");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            JSONArray pathArr = frontPictureDataObejct.getJSONArray("path");
            if (pathArr.size() != 1) {
                log.error("机器人前置拍照数据异常，异常原因：前置拍照数据有【"+pathArr.size()+"】张");
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
                return;
            }
            String imgUrl = pathArr.getStr(0);
            String timestamp = frontPictureDataObejct.getStr("timestamp");
            String imgType = "front_picture";

            TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

            RemoteControlTaskResult remoteControlTaskResult = new RemoteControlTaskResult();
            remoteControlTaskResult.setInstanceId(instanceId);
            remoteControlTaskResult.setImgType(imgType);
            remoteControlTaskResult.setImgUrl(imgUrl);
            remoteControlTaskResult.setImgTime(timestamp);

            try {
                if (!remoteControlTaskResultDao.checkExist(remoteControlTaskResult)) {
                    remoteControlTaskResultDao.add(remoteControlTaskResult);
                    //计算图片个数，更新进instance表中
                    int picCount = remoteControlTaskResultDao.picCount(instanceId);
                    remoteControlTaskInstanceDao.updatePicCount(instanceId, picCount);
                    dataSourceTransactionManager.commit(transactionStatus);
                    transactionStatus = null;
                }
                //发送数据保存成功标志
                mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
            } catch (TransactionException e) {
                if (transactionStatus != null){
                    dataSourceTransactionManager.rollback(transactionStatus);
                }
                throw new RuntimeException("保存前置拍照数据异常"+e.getMessage());
            }
        }else{
            //发送数据保存成功标志
            mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
        }
    }

    @Override
    public void add(RemoteControlTaskInstance remoteControlTaskInstance) {
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            remoteControlTaskInstanceDao.addAndReturnId(remoteControlTaskInstance);
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;
        } catch (Exception e) {
            if (transactionStatus !=null){
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void updateTaskEndByUserId(long userId) {
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            remoteControlTaskInstanceDao.updateTaskEndByUserId(userId);
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;
        } catch (Exception e) {
            if (transactionStatus !=null){
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException(e.getMessage());
        }
    }
}
