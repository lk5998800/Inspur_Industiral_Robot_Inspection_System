package com.inspur.cron;

import com.inspur.industrialinspection.dao.ItAssetTaskAnalyseResultDao;
import com.inspur.industrialinspection.dao.ItAssetTaskInfoDao;
import com.inspur.industrialinspection.dao.ItAssetTaskInstanceDao;
import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 资产盘点任务分析定时任务
 * @author kliu
 * @date 2022/8/2 15:14
 */
@Component
@Slf4j
public class ItAssetTaskAnalyseCron {

    @Autowired
    private ItAssetTaskInstanceDao itAssetTaskInstanceDao;
    @Autowired
    private ItAssetTaskAnalyseResultDao itAssetTaskAnalyseResultDao;
    @Autowired
    private ItAssetTaskInfoDao itAssetTaskInfoDao;

    /**
     * 资产盘点任务分析
     * @return void
     * @author kliu
     * @date 2022/8/2 15:15
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void taskAnalyseCron() {
        ItAssetTaskInfo itAssetTaskInfo;
        List<ItAssetTaskInstance> itAssetTaskInstances = itAssetTaskInstanceDao.unAnalyseList();
        for (ItAssetTaskInstance itAssetTaskInstance : itAssetTaskInstances) {
            long instanceId = itAssetTaskInstance.getInstanceId();
            long taskId = itAssetTaskInstance.getTaskId();
            itAssetTaskInfo = itAssetTaskInfoDao.getDetlById(taskId);
            long roomId = itAssetTaskInfo.getRoomId();
            String inventoryMethod = itAssetTaskInfo.getInventoryMethod();
            //RFID 匹配
            if ("rfid".equals(inventoryMethod)) {
                //rfid 无法判断点位信息，所以不存在资产移位的情况
                //1.资产基本信息表存在数据，上传结果有数据--资产正常
                itAssetTaskAnalyseResultDao.saveItAssetRfidNormal(instanceId, roomId);
                //2.资产基本信息表存在数据，上传结果无数据-资产缺失
                itAssetTaskAnalyseResultDao.saveItAssetRfidLack(instanceId, roomId);
                //3.上传结果有数据，资产基本信息表无数据 -不明资产
                itAssetTaskAnalyseResultDao.saveItAssetRfidUnknown(instanceId, roomId);
            }else if ("qrcode".equals(inventoryMethod)){
                //1.资产基本信息表存在数据，上传结果有数据，点位相同--资产正常
                itAssetTaskAnalyseResultDao.saveItAssetQrcodeNormal(instanceId, roomId);
                //2.资产基本信息表有数据，上传结果有数据，但是点位不一样-资产移位
                //itAssetTaskAnalyseResultDao.saveItAssetQrcodeShift(instanceId, roomId);
                //3.资产基本信息表存在数据，上传结果无数据-资产缺失
                itAssetTaskAnalyseResultDao.saveItAssetQrcodeLack(instanceId, roomId);
                //4.上传结果有数据，资产基本信息表无数据-不明资产
                itAssetTaskAnalyseResultDao.saveItAssetQrcodeUnknown(instanceId, roomId);
            }
            if (itAssetTaskAnalyseResultDao.instanceNoraml(instanceId)){
                itAssetTaskInstance.setTaskResult("normal");
            }else{
                itAssetTaskInstance.setTaskResult("abnormal");
            }
            itAssetTaskInstanceDao.update(itAssetTaskInstance);
        }
    }
}
