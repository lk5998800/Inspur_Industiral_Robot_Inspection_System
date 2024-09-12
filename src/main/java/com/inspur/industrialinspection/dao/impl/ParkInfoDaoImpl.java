package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ParkInfoDao;
import com.inspur.industrialinspection.entity.ParkInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kliu
 * @description 园区信息dao
 * @date 2022/5/6 16:43
 */
@Repository
public class ParkInfoDaoImpl implements ParkInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 依据园区id获取园区明细
     * @param parkId
     * @return com.inspur.industrialinspection.entity.ParkInfo
     * @author kliu
     * @date 2022/5/24 18:16
     */
    @Override
    public ParkInfo getDetlById(int parkId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from park_info ");
        stringBuffer.append(" where park_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, parkId);
        List<ParkInfo> list = db.dbQuery(ParkInfo.class);
        if (list.size()==0) {
            throw new RuntimeException("传入的parkId【"+parkId+"】不存在，请检查传入的数据");
        }
        return list.get(0);
    }
}
