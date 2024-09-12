package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.TypeReference;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.ExplainTaskStatus;
import com.inspur.cron.ExplainTaskExecuteCron;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.entity.vo.ExplainPointSkillVo;
import com.inspur.industrialinspection.entity.vo.ExplainPointStatusVo;
import com.inspur.industrialinspection.entity.vo.ExplainTaskVo;
import com.inspur.industrialinspection.service.ExplainTaskService;
import com.inspur.industrialinspection.service.PointInfoService;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.mqtt.MqttPushClient;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author: LiTan
 * @description: 导览讲解服务信息
 * @date: 2022-10-31 10:21:39
 */
@Service
public class ExplainTaskServiceImpl implements ExplainTaskService {
    private volatile static ConcurrentHashMap<Long, Boolean> explainTaskMap = new ConcurrentHashMap<Long, Boolean>();
    @Autowired
    private ExplainTaskDao explainTaskDao;
    @Autowired
    private RequestService requestService;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private ExplainPointInfoDao explainPointInfoDao;
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private PointInfoService pointInfoService;
    @Autowired
    private ExplainPointSkillDao pointSkillDao;
    @Autowired
    private ExplainTaskExecuteCron cron;


    @Override
    public ExplainTask getExplainTask(long id) {
        ExplainTask explainWork = explainTaskDao.getExplainTask(id);
        if (explainWork == null) {
            return null;
        }
        return explainWork;
    }

    @Override
    public PageBean pageList(long roomId, int pageSize, int pageNum, String status, String taskTime, String keyword) {
        if (!StringUtils.isEmpty(taskTime)) {
            taskTime = taskTime.substring(0, 10);
        }
        PageBean pageBean = explainTaskDao.pageList(roomId, pageSize, pageNum, status, taskTime, keyword);
        List<ExplainTask> explainTasks = explainTaskDao.List(roomId);
        if (explainTasks != null) {
            List<ExplainTaskVo> collect = explainTasks.stream().map(page -> {
                ExplainTaskVo explainTaskVo = new ExplainTaskVo();
                RoomInfo detlById = roomInfoDao.getDetlById(page.getRoomId());
                explainTaskVo.setRoomName(detlById.getRoomName());
                BeanUtil.copyProperties(page, explainTaskVo);
                return explainTaskVo;
            }).collect(Collectors.toList());
            pageBean.setContentList(collect);
        }
        return pageBean;
    }

    @Override
    public void addOrUpdate(ExplainTask explainTask) {
        long userId = requestService.getUserIdByToken();
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            long id = explainTask.getId();
            String nowExecute = "6";
            if (nowExecute.equals(explainTask.getTaskType())) {
                explainTask.setTaskTime(DateUtil.now());
            }
            if (id > 0) {
                explainTaskDao.update(explainTask);
            } else {
                explainTask.setCreateTime(DateUtil.now());
                explainTask.setCreateUserId(userId);
                explainTask.setStatus(ExplainTaskStatus.NO);
                id = explainTaskDao.addAndReturnId(explainTask);
            }
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;

            if (nowExecute.equals(explainTask.getTaskType())) {
                explainTask = explainTaskDao.getExplainTask(id);
                if (ExplainTaskStatus.NO.equals(explainTask.getStatus())) {
                    //下发任务
                    cron.ExecuteExplain(explainTask);
                }
            }
        } catch (Exception e) {
            if (transactionStatus != null) {
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        if (id <= 0) {
            throw new RuntimeException("传入id不能为空，请检查传入的数据");
        }

        ExplainTask explainTask = explainTaskDao.getExplainTask(id);
        if (!ExplainTaskStatus.NO.equals(explainTask.getStatus())) {
            throw new RuntimeException("只能删除状态为新建的随工任务");
        }
        explainTaskDao.delete(id);
    }

    @Override
    public void startTask(long id) {
        ExplainTask explainTask = explainTaskDao.getExplainTask(id);
        if (explainTask != null) {
            if (ExplainTaskStatus.NO.equals(explainTask.getStatus())) {
                //下发任务
                cron.ExecuteExplain(explainTask);
            } else {
                throw new RuntimeException("当前任务已开始，不支持重复下发任务");
            }
        }
    }


    @Override
    public void endTask(long id) {
        ExplainTask explainTask = explainTaskDao.getExplainTask(id);
        long roomId = explainTask.getRoomId();
        if (ExplainTaskStatus.WAIT.equals(explainTask.getStatus()) || ExplainTaskStatus.RUNNING.equals(explainTask.getStatus())) {
            //将终止导览讲解任务，告诉机器人端
            JSONObject issuedJsonObject = new JSONObject();
            issuedJsonObject.set("taskId", id);
            //6、导览讲解任务
            issuedJsonObject.set("type", 6);
            mqttPushClient.publish("industrial_robot_terminate/" + robotRoomDao.getRobotIdByRoomId(roomId), issuedJsonObject.toString());
            explainTaskMap.put(id, false);

            int i = 0;
            int terminateWaitCount = 50;
            while (i < terminateWaitCount) {
                if (explainTaskMap.get(id)) {
                    explainTaskMap.remove(id);
                    explainTask.setStatus(ExplainTaskStatus.END);
                    explainTask.setEndTime(DateUtil.now());
                    explainTask.setReason("手动终止任务");
                    explainTaskDao.update(explainTask);
                    break;
                } else {
                    i++;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            throw new RuntimeException("当前任务未在执行");
        }
    }

    @Override
    public List getExplainPointInfos(long roomId) {
        return explainPointInfoDao.list(roomId);
    }

    @Override
    public void associatedExplainPoint(PointInfo pointInfo) throws InterruptedException {
        long roomId = pointInfo.getRoomId();
        if (!roomInfoDao.checkExist(pointInfo.getRoomId())) {
            throw new RuntimeException("传入的机房id不存在，请检查");
        }
        String pointName = pointInfo.getPointName();
        pointInfo = pointInfoService.getRealTimePosture(roomId);
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName(pointName);
        if (explainPointInfoDao.checkExist(pointInfo)) {
            explainPointInfoDao.update(pointInfo);
        } else {
            explainPointInfoDao.add(pointInfo);
        }
    }

    @Override
    public List<ExplainPointSkillVo> getExplainPointSkills(long roomId) {
        List<ExplainPointSkill> explainPointSkills = pointSkillDao.getExplainPointSkills(roomId);
        List<ExplainPointSkillVo> collect = null;
        if (explainPointSkills != null) {
            collect = explainPointSkills.stream().map(item -> {
                ExplainPointSkillVo skillVo = new ExplainPointSkillVo();
                //根据roomID跟pointName查询点位坐标
                ExplainPointInfo pointInfo = explainPointInfoDao.getPointInfo(item.getRoomId(), item.getPointName());
                if (pointInfo != null){
                    if (pointInfo.getLocationX() != null && pointInfo.getLocationY() != null) {
                        skillVo.setHasExplainPointStatus(true);
                    } else {
                        skillVo.setHasExplainPointStatus(false);
                    }
                }
                RoomInfo detlById = roomInfoDao.getDetlById(item.getRoomId());
                skillVo.setRoomName(detlById.getRoomName());
                BeanUtil.copyProperties(item, skillVo);
                return skillVo;
            }).collect(Collectors.toList());

        }
        return collect;
    }

    @Override
    public ExplainPointSkill getExplainPointSkill(long roomId, String pointName) {

        return pointSkillDao.getExplainPointSkill(roomId, pointName);
    }

    @Override
    public void addOrUpdateExplainSkill(ExplainPointSkill explainPointSkill) {
        ExplainPointSkill skill = getExplainPointSkill(explainPointSkill.getRoomId(), explainPointSkill.getPointName());
        if (skill != null) {
            //存在，更新
            pointSkillDao.update(explainPointSkill);
        } else {
            //不存在，插入
            pointSkillDao.insert(explainPointSkill);
        }
    }

    @Override
    public List<ExplainPointStatusVo> getExplainPointInfoStatus(long roomId) {
        List<ExplainPointInfo> list = explainPointInfoDao.list(roomId);
        List<ExplainPointStatusVo> pointStatusVos = null;
        if(list != null){
           pointStatusVos = list.stream().map(item -> {
                ExplainPointStatusVo explainPointStatusVo = new ExplainPointStatusVo();
                explainPointStatusVo.setRoomId(item.getRoomId());
                explainPointStatusVo.setPointName(item.getPointName());
                if (item.getLocationX() != null && item.getLocationY() != null) {
                    explainPointStatusVo.setHasExplainPointStatus(true);
                } else {
                    explainPointStatusVo.setHasExplainPointStatus(false);
                }
                return explainPointStatusVo;
            }).collect(Collectors.toList());
        }
        return pointStatusVos;
    }
}


