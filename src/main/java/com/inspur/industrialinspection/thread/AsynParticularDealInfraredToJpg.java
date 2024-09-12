package com.inspur.industrialinspection.thread;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.dao.ParticularPointInspectionTaskResultDao;
import com.inspur.industrialinspection.entity.ParticularPointInspectionTaskResult;
import com.inspur.industrialinspection.service.AiAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * @author: kliu
 * @description: 异步处理特定点巡检中的红外转jpg
 * @date: 2022/9/23 10:13
 */
@Service
public class AsynParticularDealInfraredToJpg {
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Value("${aiagent.service.url}")
    private String aiagentUrl;
    @Value("${aiagent.service.infrared.url}")
    private String aiagentInfraredUrl;
    @Autowired
    private AiAgentService aiAgentService;
    @Autowired
    private ParticularPointInspectionTaskResultDao particularPointInspectionTaskResultDao;

    @Async
    public void asynParticularDealInfraredToJpg(long instanceId, String infrared){
        JSONArray pathArray = new JSONArray();
        pathArray.add(infrared);
        String url = aiagentUrl+aiagentInfraredUrl;
        JSONObject serviceObject = new JSONObject();
        serviceObject.set("image_url", pathArray);
        serviceObject.set("infrared_draw_thresh", 100000000);
        JSONObject serviceResult = aiAgentService.invokeHttp(url, serviceObject.toString());
        String infraredMergeUrl = serviceResult.getStr("infrared_merge_url");

        ParticularPointInspectionTaskResult particularPointInspectionTaskResult = new ParticularPointInspectionTaskResult();
        particularPointInspectionTaskResult.setInfrared(infraredMergeUrl);
        particularPointInspectionTaskResult.setInstanceId(instanceId);
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            particularPointInspectionTaskResultDao.update(particularPointInspectionTaskResult);
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;
        } catch (Exception e) {
            if (transactionStatus != null){
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException(e.getMessage());
        }
    }
}
