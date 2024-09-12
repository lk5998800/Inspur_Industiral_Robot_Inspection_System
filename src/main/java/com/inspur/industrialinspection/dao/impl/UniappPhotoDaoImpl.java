package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.UniappPhotoDao;
import com.inspur.industrialinspection.entity.UniappPhoto;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 图片上传
 * @author wangzhaodi
 * @date 2022/11/10 18:11
 */
@Repository
public class UniappPhotoDaoImpl implements UniappPhotoDao {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public long addAndReturnId(UniappPhoto uniappPhoto) throws Exception {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into uniapp_photo ");
        stringBuffer.append("  (uniapp_id, img_url,room_name,img_use_type,time,task_inspect_id,point_name) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ? ,? ,?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, uniappPhoto.getImgUrl());
        db.set(index++, uniappPhoto.getRoomName());
        db.set(index++, uniappPhoto.getImgUseType());
        db.set(index++, uniappPhoto.getTime());
        db.set(index++, uniappPhoto.getTaskInspectId());
        db.set(index++, uniappPhoto.getPointName());
        return db.dbUpdateAndReturnId();
    }

    @Override
    public void update(UniappPhoto uniappPhoto) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update uniapp_photo ");
        stringBuffer.append("   set img_url = ?, ");
        stringBuffer.append("       time = ? ");
        stringBuffer.append(" where uniapp_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, uniappPhoto.getImgUrl());
        db.set(index++, DateUtil.now());
        db.set(index++, uniappPhoto.getUniappId());
        db.dbUpdate();
    }

    @Override
    public List list(long taskInspectId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from uniapp_photo where task_inspect_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, taskInspectId);
        return db.dbQuery(UniappPhoto.class);
    }
}
