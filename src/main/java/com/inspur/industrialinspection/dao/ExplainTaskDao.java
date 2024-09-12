package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ExplainTask;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * @author: LiTan
 * @description:  导览讲解信息服务
 * @date:   2022-10-31 10:23:10
 */
public interface ExplainTaskDao {

    /**
     * 查询单条导览讲解任务信息
     * @param id
     * @return
     */
    ExplainTask getExplainTask(long id);

    /**
     * 获取讲解导览任务列表
     * @param roomId
     * @param pageSize
     * @param pageNum
     * @param status
     * @param taskTime
     * @param keyword
     * @return
     */
    PageBean pageList(long roomId, int pageSize, int pageNum, String status, String taskTime, String keyword);


    /**
     * 修改导览讲解任务
     * @param explainTask
     */
    void update(ExplainTask explainTask);

    /**
     * 添加任务
     * @param explainTask
     * @return
     */
    long addAndReturnId(ExplainTask explainTask);

    /**
     * 删除导览讲解任务
     * @param id
     */
    void delete(int id);

    /**
     * 查询未执行的讲解任务
     * @return
     */
    List<ExplainTask> getListByCron();

    /**
     * 获取所有任务。
     * @return
     * @param roomId
     */
    List<ExplainTask> List(long roomId);
}
