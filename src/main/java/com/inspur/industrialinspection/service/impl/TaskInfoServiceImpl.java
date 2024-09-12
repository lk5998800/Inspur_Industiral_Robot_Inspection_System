package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.code.TaskStatus;
import com.inspur.cron.TaskExecuteCron;
import com.inspur.industrialinspection.dao.RobotInfoDao;
import com.inspur.industrialinspection.dao.TaskInfoDao;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.entity.TaskInfo;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.service.InspectTypeService;
import com.inspur.industrialinspection.service.RoomParamService;
import com.inspur.industrialinspection.service.TaskInfoService;
import com.inspur.page.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author kliu
 * @description 任务信息服务实现
 * @date 2022/5/17 19:39
 */
@Service
@Slf4j
public class TaskInfoServiceImpl implements TaskInfoService {

    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private RobotInfoDao robotInfoDao;

    @Autowired
    private TaskExecuteCron taskExecuteCron;
    @Autowired
    private InspectTypeService inspectTypeService;
    @Autowired
    private RoomParamService roomParamService;

    @Override
    public PageBean list(long roomId, long robotId, int pageSize, int page) {
        PageBean pageBean = taskInfoDao.list(roomId, robotId, pageSize, page);
        List<TaskInfo> taskInstances = pageBean.getContentList();
        JSONObject roomParamObject;
        JSONArray inspectTypeArray;
        for (TaskInfo taskInfo : taskInstances) {
            long inspectTypeId = taskInfo.getInspectTypeId();
            roomParamObject = roomParamService.getRoomParam(taskInfo.getRoomId());
            inspectTypeArray = roomParamObject.getJSONObject("inspect_setting").getJSONArray("inspect_type");
            for (int i = 0; i < inspectTypeArray.size(); i++) {
                long fileInspectTypeId = inspectTypeArray.getJSONObject(i).getLong("inspect_type_id");
                if (inspectTypeId == fileInspectTypeId) {
                    String inspectTypeName = inspectTypeArray.getJSONObject(i).getStr("inspect_type_name");
                    taskInfo.setInspectTypeName(inspectTypeName);
                }
            }
        }
        return pageBean;
    }

    @Override
    public PageBean listWithoutPark(long roomId, long robotId, int pageSize, int page) {
        PageBean pageBean = taskInfoDao.listWithoutPark(roomId, robotId, pageSize, page);
        List<TaskInfo> taskInstances = pageBean.getContentList();
        JSONObject roomParamObject;
        JSONArray inspectTypeArray;
        TaskInfo taskInfo;
        for (int i = 0; i < taskInstances.size(); i++) {
            taskInfo = taskInstances.get(i);
            String inUse = taskInfo.getInUse();
            //剔除无效的任务
            if ("1".equals(inUse)) {
                taskInstances.remove(i);
                i--;
                continue;
            }
            long inspectTypeId = taskInfo.getInspectTypeId();
            roomParamObject = roomParamService.getRoomParam(taskInfo.getRoomId());
            inspectTypeArray = roomParamObject.getJSONObject("inspect_setting").getJSONArray("inspect_type");
            for (int j = 0; j < inspectTypeArray.size(); j++) {
                long fileInspectTypeId = inspectTypeArray.getJSONObject(j).getLong("inspect_type_id");
                if (inspectTypeId == fileInspectTypeId) {
                    String inspectTypeName = inspectTypeArray.getJSONObject(j).getStr("inspect_type_name");
                    taskInfo.setInspectTypeName(inspectTypeName);
                }
            }
        }
        return pageBean;
    }

    @Override
    public void add(TaskInfo taskInfo) {
        long roomId = taskInfo.getRoomId();
        //添加任务时，对于同一个机房、机器人、巡检类型，仅能存在一条任务数据，不能存在多条
        if (taskInfoDao.checkExistByInspectTypeRobot(taskInfo)) {
            throw new RuntimeException("已存在相同配置的任务信息，如需更改任务执行时间，请使用任务编辑功能");
        }

        String execTime = taskInfo.getExecTime();
        if (execTime.endsWith(StrUtil.COMMA)) {
            execTime = execTime.substring(0, execTime.length() - 1).trim();
        }

        //对传入的日期进行处理，仅保留时分
        String[] execTimeArr = execTime.split(",");
        execTime = "";
        DateTime execTimeDateTime, tempDateTime;
        Arrays.sort(execTimeArr);
        String execTimeStr = "", tempStr = "";
        for (int i = 0; i < execTimeArr.length; i++) {
            execTimeStr = execTimeArr[i];
            execTimeDateTime = DateUtil.parse(execTimeStr);
            for (int j = i + 1; j < execTimeArr.length; j++) {
                tempStr = execTimeArr[j];
                if (tempStr.equals(execTimeStr)) {
                    throw new RuntimeException("传入的执行时间不能相同，请检查传入的数据");
                }
                tempDateTime = DateUtil.parse(tempStr);
                long abs = Math.abs(tempDateTime.getTime() - execTimeDateTime.getTime());
                if (abs / 1000 / 60 < 5) {
                    throw new RuntimeException("执行时间间隔不能低于5分钟，请检查传入的数据");
                }
            }

            String format = DateUtil.format(execTimeDateTime, "HH:mm");
            execTime += format + ",";
        }

        if (execTime.endsWith(StrUtil.COMMA)) {
            execTime = execTime.substring(0, execTime.length() - 1).trim();
        }

        taskInfo.setExecTime(execTime);


        long robotId = taskInfo.getRobotId();
        if (!robotInfoDao.checkIsExist(robotId)) {
            throw new RuntimeException("传入的机器人不存在，请检查传入的数据");
        }

        long inspectTypeId = taskInfo.getInspectTypeId();
        if (!inspectTypeService.checkIsExist(roomId, inspectTypeId)) {
            throw new RuntimeException("传入的巡检类型不存在，请检查传入的数据");
        }

        taskInfoDao.add(taskInfo);
    }

    @Override
    public void delete(TaskInfo taskInfo) {
        long taskId = taskInfo.getTaskId();
        if (taskId <= 0) {
            throw new RuntimeException("传入任务id不能为空，请检查传入的数据");
        }

        boolean instanceExistFlag = taskInstanceDao.checkExistByTaskId(taskId);
        if (instanceExistFlag) {
            throw new RuntimeException("当前任务已经执行过，不能删除");
        }

        taskInfoDao.delete(taskInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startNow(TaskInfo taskInfo) throws IOException {
        long taskId = taskInfo.getTaskId();
        if (taskId <= 0) {
            throw new RuntimeException("传入任务id不能为空，请检查传入的数据");
        }
        //判断当前是否在任务中，如果下发，把执行的任务改为暂停任务，下发新任务。
        TaskInstance task = taskInstanceDao.getSuspendTask(taskInfo.getRobotId(), TaskStatus.RUNNING);
        if (task != null) {
            //修改正在执行的任务为暂停状态
            TaskInstance taskInstance = taskInstanceDao.getTaskById(task.getInstanceId());
            taskInstance.setEndTime(DateUtil.now());
            taskInstance.setExecStatus(TaskStatus.SUSPEND);
            taskInstanceDao.updateById(taskInstance);
        }
        taskInfo = taskInfoDao.getDetlById(taskInfo.getTaskId());

        taskExecuteCron.executeTask(taskInfo, DateUtil.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TaskInfo taskInfo) {
        String execTime = taskInfo.getExecTime();
        long robotId = taskInfo.getRobotId();
        long roomId = taskInfo.getRoomId();
        if (execTime.endsWith(StrUtil.COMMA)) {
            execTime = execTime.substring(0, execTime.length() - 1).trim();
        }

        //对传入的日期进行处理，仅保留时分
        String[] execTimeArr = execTime.split(",");
        execTime = "";
        DateTime execTimeDateTime, tempDateTime;
        String execTimeStr = "", tempStr = "";
        Arrays.sort(execTimeArr);
        for (int i = 0; i < execTimeArr.length; i++) {
            execTimeStr = execTimeArr[i];
            execTimeDateTime = DateUtil.parse(execTimeStr);
            for (int j = i + 1; j < execTimeArr.length; j++) {
                tempStr = execTimeArr[j];
                if (tempStr.equals(execTimeStr)) {
                    throw new RuntimeException("传入的执行时间不能相同，请检查传入的数据");
                }
                tempDateTime = DateUtil.parse(tempStr);
                long abs = Math.abs(tempDateTime.getTime() - execTimeDateTime.getTime());
                if (abs / 1000 / 60 < 5) {
                    throw new RuntimeException("执行时间间隔不能低于5分钟，请检查传入的数据");
                }
            }

            String format = DateUtil.format(execTimeDateTime, "HH:mm");
            execTime += format + ",";
        }

        if (execTime.endsWith(StrUtil.COMMA)) {
            execTime = execTime.substring(0, execTime.length() - 1).trim();
            taskInfo.setExecTime(execTime);
        }

        if (!robotInfoDao.checkIsExist(robotId)) {
            throw new RuntimeException("传入的机器人不存在，请检查传入的数据");
        }

        long inspectTypeId;

        long taskId = taskInfo.getTaskId();

        //对于更新操作中，如果已经有执行记录了，仅允许更新时间，如没有执行记录，则都可以修改
        boolean instanceFlag = taskInstanceDao.checkExistByTaskId(taskId);
        if (instanceFlag) {
            TaskInfo dbTaskInfo = taskInfoDao.getDetlById(taskId);
            long robotId1 = dbTaskInfo.getRobotId();
            if (robotId1 != robotId) {
                throw new RuntimeException("当前任务已有执行记录，不允许修改机器人信息");
            }
            inspectTypeId = dbTaskInfo.getInspectTypeId();
            if (inspectTypeId != taskInfo.getInspectTypeId()) {
                throw new RuntimeException("当前任务已有执行记录，不允许修改巡检类型");
            }

            taskInfoDao.updateExecTime(taskInfo);
            return;
        }
        taskInfoDao.updateAll(taskInfo);
    }

    @Override
    public JSONObject getRobotRunningTaskDesc(long robotId) {
        List<TaskInstance> runningTaskInstances = taskInstanceDao.getTaskInstances(robotId, TaskStatus.RUNNING);
        List<TaskInstance> suspendTaskInstances = taskInstanceDao.getTaskInstances(robotId, TaskStatus.SUSPEND);
        String desc = "当前存在";
        if (runningTaskInstances.size() > 0) {
            desc+=runningTaskInstances.size()+"条正在执行的巡检任务，";
        }
        if (suspendTaskInstances.size() > 0) {
            desc+=suspendTaskInstances.size()+"条待执行的暂停任务，";
        }
        desc+="是否立即执行临时任务？";

        if (desc.equals("当前存在是否立即执行临时任务？")){
            desc = "";
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("desc", desc);
        return jsonObject;
    }
}
