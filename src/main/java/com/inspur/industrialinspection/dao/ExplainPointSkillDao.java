package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.ExplainPointSkill;
import com.inspur.industrialinspection.entity.vo.ExplainPointSkillVo;

import java.util.List;

/**
 * @author: LiTan
 * @description:    讲解点位技能
 * @date:   2022-11-01 09:58:32
 */
public interface ExplainPointSkillDao {


    /**
     * 获取导航讲解点对应技能
     * @param roomId
     * @return
     */
    List<ExplainPointSkill> getExplainPointSkills(long roomId);

    /**
     * 获取机房下单个点位的技能信息
     * @param roomId
     * @param pointName
     * @return
     */
    ExplainPointSkill getExplainPointSkill(long roomId, String pointName);

    /**
     * 更新技能信息
     * @param explainPointSkill
     */
    void update(ExplainPointSkill explainPointSkill);

    /**
     * 插入
     * @param explainPointSkill
     */
    void insert(ExplainPointSkill explainPointSkill);

}
