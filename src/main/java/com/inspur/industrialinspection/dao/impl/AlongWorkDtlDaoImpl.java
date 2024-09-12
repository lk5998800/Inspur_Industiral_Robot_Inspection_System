package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.AlongWorkDtlDao;
import com.inspur.industrialinspection.entity.AlongWorkDtl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 随工明细
 * @author kliu
 * @date 2022/6/14 19:55
 */
@Repository
public class AlongWorkDtlDaoImpl implements AlongWorkDtlDao {

    @Autowired
    private BeanFactory beanFactory;


    @Override
    public void add(AlongWorkDtl alongWorkDtl) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into along_work_dtl ");
        stringBuffer.append("  (pid, point_name, inspection_time, img_url) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWorkDtl.getPid());
        db.set(index++, alongWorkDtl.getPointName());
        db.set(index++, alongWorkDtl.getInspectionTime());
        db.set(index++, alongWorkDtl.getImgUrl());
        db.dbUpdate();
    }

    @Override
    public void update(AlongWorkDtl alongWorkDtl) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update along_work_dtl ");
        stringBuffer.append("   set inspection_time = ?, img_url = ? ");
        stringBuffer.append(" where pid = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWorkDtl.getInspectionTime());
        db.set(index++, alongWorkDtl.getImgUrl());
        db.set(index++, alongWorkDtl.getPid());
        db.set(index++, alongWorkDtl.getPointName());
        db.dbUpdate();
    }

    @Override
    public boolean checkExist(AlongWorkDtl alongWorkDtl) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from along_work_dtl ");
        stringBuffer.append(" where pid = ? ");
        stringBuffer.append("   and point_name = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, alongWorkDtl.getPid());
        db.set(index++, alongWorkDtl.getPointName());
        return db.dbQuery().size()>0;
    }

    /**
     * 获取随工明细数据
     *
     * @param id
     * @return java.util.List
     * @author kliu
     * @date 2022/6/30 14:11
     */
    @Override
    public List<AlongWorkDtl> list(long id) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from along_work_dtl where pid = ? order by inspection_time asc ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, id);
        return db.dbQuery(AlongWorkDtl.class);
    }

    /**
     * 获取随工明细数据
     *
     * @param id
     * @param pointName
     * @return java.util.List
     * @author kliu
     * @date 2022/6/30 14:11
     */
    @Override
    public AlongWorkDtl getDetlById(long id, String pointName) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from along_work_dtl where pid = ? and point_name = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, id);
        db.set(index++, pointName);
        List<AlongWorkDtl> list = db.dbQuery(AlongWorkDtl.class);
        if (list.size()==0){
            return null;
        }else{
            return list.get(0);
        }
    }
}
