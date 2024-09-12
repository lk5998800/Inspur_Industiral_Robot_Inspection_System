package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.TaskStatus;
import com.inspur.industrialinspection.dao.ItAssetTaskInfoDao;
import com.inspur.industrialinspection.dao.ItAssetTaskInstanceDao;
import com.inspur.industrialinspection.dao.TaskInfoDao;
import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import com.inspur.industrialinspection.service.AlongWorkService;
import com.inspur.industrialinspection.service.ItAssetTaskInstanceService;
import com.inspur.industrialinspection.service.RoomParamService;
import com.inspur.mqtt.MqttPushClient;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kliu
 * @description: 资产盘点任务执行实例
 * @date: 2022/7/27 17:04
 */
@Service
public class ItAssetTaskInstanceServiceImpl implements ItAssetTaskInstanceService {
    private volatile static ConcurrentHashMap<Long, Boolean> itAssetTaskInstanceMap = new ConcurrentHashMap<Long, Boolean>();
    @Autowired
    private ItAssetTaskInfoDao itAssetTaskInfoDao;
    @Autowired
    private ItAssetTaskInstanceDao itAssetTaskInstanceDao;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Override
    public PageBean list(long roomId, long robotId, String taskName, String cabinetRow, long cabinetColumn, int pageSize, int pageNum) {
        PageBean pageBean = itAssetTaskInstanceDao.list(roomId, robotId, taskName, cabinetRow, cabinetColumn, pageSize, pageNum);
        return pageBean;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminate(ItAssetTaskInstance itAssetTaskInstance) {
        long instanceId = itAssetTaskInstance.getInstanceId();
        if(!itAssetTaskInstanceDao.checkExist(instanceId)){
            throw new RuntimeException("要终止的任务不存在，请检查传入的数据");
        }
        String execStatus = itAssetTaskInstanceDao.getDetlById(instanceId).getExecStatus();
        if(TaskStatus.TERMINATE.equals(execStatus)){
            throw new RuntimeException("当前任务已终止，不允许再次终止");
        }
        if(TaskStatus.END.equals(execStatus)){
            throw new RuntimeException("当前任务已结束，不允许终止");
        }
        if(TaskStatus.CREATE.equals(execStatus)){
            throw new RuntimeException("当前任务未运行，不允许终止");
        }
        itAssetTaskInstance = itAssetTaskInstanceDao.getDetlById(instanceId);
        long taskId = itAssetTaskInstance.getTaskId();
        ItAssetTaskInfo itAssetTaskInfo = itAssetTaskInfoDao.getDetlById(taskId);
        long robotId = itAssetTaskInfo.getRobotId();
        JSONObject issuedJsonObject = new JSONObject();
        issuedJsonObject.set("taskId", instanceId);
        //1   普通巡检    2   随工任务  3 资产盘点任务
        issuedJsonObject.set("type", 3);
        mqttPushClient.publish("industrial_robot_terminate/"+robotId,issuedJsonObject.toString());
        itAssetTaskInstanceMap.put(instanceId, false);

        int i=0;
        int terminateWaitCount = 50;
        while (i<terminateWaitCount){
            if (itAssetTaskInstanceMap.get(instanceId)) {
                itAssetTaskInstanceMap.remove(instanceId);

                itAssetTaskInstance.setEndTime(DateUtil.now());
                itAssetTaskInstance.setExecStatus(TaskStatus.TERMINATE);
                //更新状态为已终止
                itAssetTaskInstanceDao.update(itAssetTaskInstance);
                break;
            }else{
                i++;
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        if(itAssetTaskInstanceMap.containsKey(instanceId)){
            throw new RuntimeException("任务终止失败，请尝试重新终止");
        }
    }

    @Override
    public void receiveTerminateResult(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        Long taskId = jsonObject.getLong("task_id");
        if(itAssetTaskInstanceMap.containsKey(taskId)){
            itAssetTaskInstanceMap.put(taskId, true);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void industrialRobotEndTask(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        Long instanceId = jsonObject.getLong("task_id");
        String endTime = jsonObject.getStr("end_time").substring(0, 16);
        ItAssetTaskInstance dbTaskInstance = itAssetTaskInstanceDao.getDetlByIdForUpdate(instanceId);
        //仅running状态下更新结束时间，防止终止任务后任务状态也变为已结束
        if (TaskStatus.RUNNING.equals(dbTaskInstance.getExecStatus())){
            ItAssetTaskInstance itAssetTaskInstance = new ItAssetTaskInstance();
            itAssetTaskInstance.setInstanceId(instanceId);
            itAssetTaskInstance.setEndTime(endTime);
            itAssetTaskInstance.setExecStatus(TaskStatus.END);
            itAssetTaskInstanceDao.update(itAssetTaskInstance);
        }
    }

    /**
     * 获取资产盘点任务实例明细
     * @param cabinetRow
     * @param cabinetColumn
     * @param assetName
     * @param personInChargeId
     * @param instanceId
     * @param pageSize
     * @param pageNum
     * @return java.util.List
     * @author kliu
     * @date 2022/8/3 16:15
     */
    @Override
    public PageBean instanceDetlList(String cabinetRow, long cabinetColumn, String assetNo, String assetName, long personInChargeId, long instanceId, String dataType, int pageSize, int pageNum) {
        return itAssetTaskInstanceDao.instanceDetlList(cabinetRow, cabinetColumn, assetNo, assetName, personInChargeId, instanceId, dataType, pageSize, pageNum);
    }

    @Override
    public void batchDelete(JSONArray jsonArray) {
        if (jsonArray.size() == 0) {
            throw new RuntimeException("请选择要删除的数据");
        }
        String inPara = "";
        Long personnelId;
        for (int i = 0; i < jsonArray.size(); i++) {
            personnelId = jsonArray.getLong(i);
            if (StringUtils.isEmpty(inPara)){
                inPara+=personnelId;
            }else{
                inPara+=","+personnelId;
            }
        }
        itAssetTaskInstanceDao.batchDelete(inPara);
    }
}
