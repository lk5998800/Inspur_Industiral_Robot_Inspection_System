package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.TaskStatus;
import com.inspur.industrialinspection.dao.ItAssetTaskInfoDao;
import com.inspur.industrialinspection.dao.ItAssetTaskInstanceDao;
import com.inspur.industrialinspection.dao.ItAssetTaskResultDao;
import com.inspur.industrialinspection.entity.ItAssetTaskInfo;
import com.inspur.industrialinspection.entity.ItAssetTaskInstance;
import com.inspur.industrialinspection.entity.ItAssetTaskResult;
import com.inspur.industrialinspection.service.ItAssetTaskInfoService;
import com.inspur.mqtt.MqttPushClient;
import com.inspur.page.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: kliu
 * @description: 资产盘点任务信息
 * @date: 2022/7/25 17:17
 */
@Service
@Slf4j
public class ItAssetTaskInfoServiceImpl implements ItAssetTaskInfoService {

    @Autowired
    private ItAssetTaskInfoDao itAssetTaskInfoDao;
    @Autowired
    private ItAssetTaskInstanceDao itAssetTaskInstanceDao;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private ItAssetTaskResultDao itAssetTaskResultDao;

    private static String EXEC_TYPE_REGULAR = "regular";
    private static String CYCLE_TYPE_WEEK = "week";
    private static String CYCLE_TYPE_TWOWEEK = "twoweek";
    private static String CYCLE_TYPE_MONTH = "month";

    /**
     * 获取列表
     *
     * @param itAssetTaskInfo
     * @return java.util.List
     * @author kliu
     * @date 2022/7/25 13:48
     */
    @SuppressWarnings("AlibabaStringConcat")
    @Override
    public PageBean list(ItAssetTaskInfo itAssetTaskInfo, int pageNum, int pageSize) {
        PageBean pageBean = itAssetTaskInfoDao.list(itAssetTaskInfo, pageNum, pageSize);
        List<ItAssetTaskInfo> list = pageBean.getContentList();
        String cycleType = "";
        long cycleValue = 0;
        String execTime = "";
        for (ItAssetTaskInfo assetTaskInfo : list) {
            //周期执行 执行时间需要根据周期类型组装
//            if ("cycle".equals(assetTaskInfo.getExecType())){
//                cycleType = assetTaskInfo.getCycleType();
//                cycleValue = assetTaskInfo.getCycleValue();
//                execTime = assetTaskInfo.getExecTime();
//                if ("workday".equals(cycleType)){
//                    execTime = "每个工作日 "+execTime;
//                }else if("everyday".equals(cycleType)){
//                    execTime = "每日 "+execTime;
//                }else if("week".equals(cycleType)){
//                    execTime = "每周（周"+ Convert.numberToChinese(cycleValue, false)+"） "+execTime;
//                }else if("twoweek".equals(cycleType)){
//                    execTime = "每两周（周"+ Convert.numberToChinese(cycleValue, false)+"） "+execTime;
//                }else if("month".equals(cycleType)){
//                    execTime = "每月"+cycleValue+"日 "+execTime;
//                }else{
//                    throw new RuntimeException("不支持的任务循环类型");
//                }
//                assetTaskInfo.setExecTime(execTime);
//            }
        }
        return pageBean;
    }

    /**
     * 添加
     *
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(ItAssetTaskInfo itAssetTaskInfo) {
        //定期执行的任务，执行时间需要到时分秒
        String execType = itAssetTaskInfo.getExecType();
        String execTime = itAssetTaskInfo.getExecTime();
        //定期任务
        if (EXEC_TYPE_REGULAR.equals(execType)){
            try {
                DateUtil.parse(execTime, "yyyy-MM-dd HH:mm").toString("yyyy-MM-dd HH:mm");
            } catch (Exception e) {
                throw new RuntimeException("定期执行的任务执行时间必须符合yyyy-MM-dd HH:mm:ss格式");
            }
            //定期执行的任务，执行时间就是下一次执行时间
            itAssetTaskInfo.setNextExecTime(execTime);
        }else{//周期任务
            try {
                DateUtil.parse(execTime, "HH:mm").toString("HH:mm");
            } catch (Exception e) {
                throw new RuntimeException("周期执行的任务执行时间必须符合HH:mm格式");
            }

            if (StringUtils.isEmpty(itAssetTaskInfo.getCycleType())) {
                throw new RuntimeException("周期执行的任务周期不能为空");
            }

            String cycleType = itAssetTaskInfo.getCycleType();
            if (CYCLE_TYPE_WEEK.equals(cycleType) || CYCLE_TYPE_TWOWEEK.equals(cycleType) || CYCLE_TYPE_MONTH.equals(cycleType)){
                if (itAssetTaskInfo.getCycleValue()==0) {
                    throw new RuntimeException("周期为工作日、每两周、每月的周期对应的值必须大于0");
                }
            }
        }
        itAssetTaskInfoDao.add(itAssetTaskInfo);
    }

    /**
     * 更新
     *
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(ItAssetTaskInfo itAssetTaskInfo) {
        //定期执行的任务，执行时间需要到时分秒
        String execType = itAssetTaskInfo.getExecType();
        String execTime = itAssetTaskInfo.getExecTime();
        //定期任务
        if (EXEC_TYPE_REGULAR.equals(execType)){
            try {
                DateUtil.parse(execTime, "yyyy-MM-dd HH:mm:ss").toString("yyyy-MM-dd HH:mm:ss");
            } catch (Exception e) {
                throw new RuntimeException("定期执行的任务执行时间必须符合yyyy-MM-dd HH:mm:ss格式");
            }
            itAssetTaskInfo.setNextExecTime(execTime);
        }else{//周期任务
            try {
                DateUtil.parse(execTime, "HH:mm").toString("HH:mm");
            } catch (Exception e) {
                throw new RuntimeException("周期执行的任务执行时间必须符合HH:mm格式");
            }

            if (StringUtils.isEmpty(itAssetTaskInfo.getCycleType())) {
                throw new RuntimeException("周期执行的任务周期不能为空");
            }

            String cycleType = itAssetTaskInfo.getCycleType();
            if (CYCLE_TYPE_WEEK.equals(cycleType) || CYCLE_TYPE_TWOWEEK.equals(cycleType) || CYCLE_TYPE_MONTH.equals(cycleType)){
                if (itAssetTaskInfo.getCycleValue()==0) {
                    throw new RuntimeException("周期为工作日、每两周、每月的周期对应的值必须大于0");
                }
            }
        }
        itAssetTaskInfoDao.update(itAssetTaskInfo);
    }

    /**
     * 删除
     *
     * @param itAssetTaskInfo
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(ItAssetTaskInfo itAssetTaskInfo) {
        itAssetTaskInfoDao.delete(itAssetTaskInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveItAssetResult(String json) {
        JSONObject jsonObject = JSONUtil.parseObj(json);
        JSONObject dataObject = (JSONObject) jsonObject.get("data");
        long instanceId = dataObject.getLong("task_id");
        long robotId = dataObject.getLong("robot_id");
        String startTime = dataObject.getStr("start_time");
        String endTime = dataObject.getStr("end_time");
        String uuid = dataObject.getStr("uuid");

        ItAssetTaskInstance itAssetTaskInstance = itAssetTaskInstanceDao.getDetlById(instanceId);
        String execStatus = itAssetTaskInstance.getExecStatus();

        boolean updateTaskInstance = false;
        if (StringUtils.isEmpty(endTime)) {
            //正在运行
            if (TaskStatus.CREATE.equals(execStatus)) {
                itAssetTaskInstance.setExecStatus(TaskStatus.RUNNING);
                updateTaskInstance = true;
            }
        } else {
            if (TaskStatus.RUNNING.equals(execStatus)) {
                itAssetTaskInstance.setExecStatus(TaskStatus.END);
                itAssetTaskInstance.setEndTime(endTime);
                updateTaskInstance = true;
            }
        }
        if (updateTaskInstance) {
            itAssetTaskInstanceDao.update(itAssetTaskInstance);
        }

        JSONObject itAssetDataObject = dataObject.getJSONObject("detection_data");
        String pointName = itAssetDataObject.getStr("point_name");
        JSONObject qrCodeObject = itAssetDataObject.getJSONObject("qr_code");
        String code = qrCodeObject.getStr("code");
        JSONObject dataObject1;
        JSONArray dataArr;
        //noinspection AlibabaUndefineMagicConstant
        if("0".equals(code)) {
            dataObject1 = qrCodeObject.getJSONObject("data");
            dataArr = dataObject1.getJSONArray("qrcode_detection");
        }else{
            log.error("机器人返回资产盘点异常："+qrCodeObject.getStr("message"));
            mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
            return;
        }

        ItAssetTaskResult itAssetTaskResult = new ItAssetTaskResult();
        itAssetTaskResult.setInstanceId(instanceId);
        itAssetTaskResult.setPointName(pointName);
        for (int i = 0; i < dataArr.size(); i++) {
            String qrCode = dataArr.getStr(i);
            itAssetTaskResult.setQrCode(qrCode);
            if(!itAssetTaskResultDao.checkExist(itAssetTaskResult)){
                itAssetTaskResultDao.add(itAssetTaskResult);
            }
        }
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

        itAssetTaskInfoDao.batchDelete(inPara);
    }
}
