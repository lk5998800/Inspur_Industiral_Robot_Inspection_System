package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.TaskInfo;
import com.inspur.page.PageBean;

import java.io.IOException;
/**
 * 任务信息服务
 * @author kliu
 * @date 2022/6/1 20:25
 */
public interface TaskInfoService {
    /**
     * 分页获取任务列表
     * @param roomId
     * @param robotId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/6/14 16:39
     */
    PageBean list(long roomId, long robotId, int pageSize, int page);
    /**
     * 获取所有任务列表，不判断园区
     * @param roomId
     * @param robotId
     * @param pageSize
     * @param page
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/6/8 11:25
     */
    PageBean listWithoutPark(long roomId, long robotId, int pageSize, int page);
    /**
     * 添加任务
     *
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/6/1 20:26
     */
    void add(TaskInfo taskInfo);
    /**
     * 删除任务
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/6/1 20:26
     */
    void delete(TaskInfo taskInfo);
    /**
     * 任务立即开始
     * @param taskInfo
     * @return void
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:44
     */
    void startNow(TaskInfo taskInfo) throws IOException;
    /**
     * 任务更新
     * @param taskInfo
     * @return void
     * @author kliu
     * @date 2022/6/1 20:26
     */
    void update(TaskInfo taskInfo);


    JSONObject getRobotRunningTaskDesc(long robotId);

}
