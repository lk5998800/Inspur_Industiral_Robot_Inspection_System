package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.MechanicalArmParaDao;
import com.inspur.industrialinspection.entity.MechanicalArmPara;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author: kliu
 * @description: 机械臂参数
 * @date: 2022/9/8 11:08
 */
@Repository
public class MechanicalArmParaDaoImpl implements MechanicalArmParaDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public List<MechanicalArmPara> list(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select room_id, position_height, position_param from mechanical_arm_para where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        return db.dbQuery(MechanicalArmPara.class);
    }

    @Override
    public List list(long roomId, String detectionId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select room_id, position_height, position_param from mechanical_arm_para where room_id = ? and detection_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        db.set(index++, detectionId);
        return db.dbQuery(MechanicalArmPara.class);
    }
}
