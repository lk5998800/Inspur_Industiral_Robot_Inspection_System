package com.inspur.industrialinspection.dao.impl;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.FireExtinguisherParaDao;
import com.inspur.industrialinspection.entity.FireExtinguisherPara;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 灭火器检测参数业务层
 *
 * @author kliu
 * @since 2022-11-25 11:23:54
 */
@Repository
public class FireExtinguisherParaDaoImpl implements FireExtinguisherParaDao {

	@Autowired
    private BeanFactory beanFactory;

    @Override
    public void add(FireExtinguisherPara fireExtinguisherPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into fire_extinguisher_para ");
        stringBuffer.append("  (room_id, point_name, fire_exitinguisher_path, fire_exitinguisher_pos, fire_exitinguisher_num) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?)");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, fireExtinguisherPara.getRoomId());
        db.set(index++, fireExtinguisherPara.getPointName());
        db.set(index++, fireExtinguisherPara.getFireExitinguisherPath());
        db.set(index++, fireExtinguisherPara.getFireExitinguisherPos());
        db.set(index++, fireExtinguisherPara.getFireExitinguisherNum());
        db.dbUpdate();
    }
    
    @Override
    public void update(FireExtinguisherPara fireExtinguisherPara) {
       Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("update fire_extinguisher_para ");
        stringBuffer.append("   set fire_exitinguisher_path = ?, fire_exitinguisher_pos = ?, fire_exitinguisher_num = ? ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ?");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, fireExtinguisherPara.getFireExitinguisherPath());
        db.set(index++, fireExtinguisherPara.getFireExitinguisherPos());
        db.set(index++, fireExtinguisherPara.getFireExitinguisherNum());
        db.set(index++, fireExtinguisherPara.getRoomId());
        db.set(index++, fireExtinguisherPara.getPointName());
        db.dbUpdate();
    }

    @Override
    public FireExtinguisherPara findById(Long roomId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from fire_extinguisher_para ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ?");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, roomId);
        db.set(index++, pointName);
        List<FireExtinguisherPara> list = db.dbQuery(FireExtinguisherPara.class);
        if (list.size() == 0){
            return null;
        }else{
            return list.get(0);
        }
    }

    @Override
    public boolean checkExist(Long roomId, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from fire_extinguisher_para ");
        stringBuffer.append(" where room_id = ? ");
        stringBuffer.append("   and point_name = ?");
        db.setSql(stringBuffer.toString());
        int index =1;
        db.set(index++, roomId);
        db.set(index++, pointName);
        List list = db.dbQuery();
        return list.size() > 0;
    }
}

