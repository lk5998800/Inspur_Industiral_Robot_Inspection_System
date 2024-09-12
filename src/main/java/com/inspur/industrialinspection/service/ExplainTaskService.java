package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.ExplainPointInfo;
import com.inspur.industrialinspection.entity.ExplainPointSkill;
import com.inspur.industrialinspection.entity.ExplainTask;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.entity.vo.ExplainPointSkillVo;
import com.inspur.industrialinspection.entity.vo.ExplainPointStatusVo;
import com.inspur.page.PageBean;

import java.util.List;


/**
 * @author: LiTan
 * @description:    导览讲解服务信息
 * @date:   2022-10-31 10:20:35
 */
public interface ExplainTaskService {


    /**
     * 获取单个导览讲解任务详细
     * @param id
     * @return
     */
    ExplainTask getExplainTask(long id);

    /**
     * 获取导览讲解任务列表
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
     * 增加或者修改导览讲解任务
     * @param explainTask
     */
    void addOrUpdate(ExplainTask explainTask);

    /**
     * 删除导览讲解任务
     * @param id
     */
    void delete(int id);

    /**
     * 开始导览讲解任务
     * @param id
     */
    void startTask(long id);

    /**
     * 结束任务
     * @param id
     */
    void endTask(long id);

    /**
     * 获取机房下的导览讲解点位
     * @param roomId
     * @return
     */
    List getExplainPointInfos(long roomId);

    /**
     * 关联导览讲解点
     * @param pointInfo
     */
    void associatedExplainPoint(PointInfo pointInfo) throws InterruptedException;

    /**
     * 获取导览讲解点对应技能
     * @param roomId
     * @return
     */
    List<ExplainPointSkillVo> getExplainPointSkills(long roomId);

    /**
     * 获取机房下单个点位技能信息
     * @param roomId
     * @param pointName
     * @return
     */
    ExplainPointSkill getExplainPointSkill(long roomId, String pointName);

    /**
     * 增加或者修改技能信息
     * @param explainPointSkill
     */
    void addOrUpdateExplainSkill(ExplainPointSkill explainPointSkill);

    /**
     * 获取机房下的所有点位关联状态
     * @param roomId
     * @return
     */
    List<ExplainPointStatusVo> getExplainPointInfoStatus(long roomId);
}
