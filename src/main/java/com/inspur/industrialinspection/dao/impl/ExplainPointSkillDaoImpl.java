package com.inspur.industrialinspection.dao.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ExplainPointInfoDao;
import com.inspur.industrialinspection.dao.ExplainPointSkillDao;
import com.inspur.industrialinspection.entity.ExplainPointSkill;
import com.inspur.industrialinspection.entity.PointInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: LiTan
 * @description: 导览点技能基本信息dao实现
 * @date: 2022-11-01 10:00:03
 */
@Repository
public class ExplainPointSkillDaoImpl implements ExplainPointSkillDao {

    @Autowired
    private BeanFactory beanFactory;


    @Override
    public List<ExplainPointSkill> getExplainPointSkills(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        if (roomId == 0) {
            stringBuffer.append("select * from explain_point_skill");
            db.setSql(stringBuffer.toString());
        } else {
            stringBuffer.append("select * from explain_point_skill where room_id = ? ");
            db.setSql(stringBuffer.toString());
            db.set(1, roomId);
        }
        List<ExplainPointSkill> list = db.dbQuery(ExplainPointSkill.class);
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    @Override
    public ExplainPointSkill getExplainPointSkill(long roomId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from explain_point_skill where room_id = ? and point_name = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, roomId);
        db.set(2, pointName);
        List<ExplainPointSkill> list = db.dbQuery(ExplainPointSkill.class);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public void update(ExplainPointSkill explainPointSkill) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update explain_point_skill set room_id = ?,point_name = ?,waiting_time = ?,broadcast = ? where room_id = ? and point_name = ?");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, explainPointSkill.getRoomId());
        db.set(index++, explainPointSkill.getPointName());
        db.set(index++, explainPointSkill.getWaitingTime());
        db.set(index++, explainPointSkill.getBroadcast());

        db.set(index++, explainPointSkill.getRoomId());
        db.set(index++, explainPointSkill.getPointName());
        db.dbUpdate();
    }

    @Override
    public void insert(ExplainPointSkill explainPointSkill) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into explain_point_skill(room_id, point_name, waiting_time,broadcast)values (?, ?, ?, ?)");
        db.setSql(stringBuffer.toString());
        int index = 1;
        db.set(index++, explainPointSkill.getRoomId());
        db.set(index++, explainPointSkill.getPointName());
        db.set(index++, explainPointSkill.getWaitingTime());
        db.set(index++, explainPointSkill.getBroadcast());
        db.dbUpdate();
    }
}