package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.DetectionInfoDao;
import com.inspur.industrialinspection.entity.DetectionInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kliu
 * @description: 检测项基本信息dao实现
 * @date: 2022/4/16 10:21
 */
@Repository
public class DetectionInfoDaoImpl implements DetectionInfoDao {

    @Autowired
    private BeanFactory beanFactory;

    private volatile static ConcurrentHashMap<String, DetectionInfo> detectionInfoMap = new ConcurrentHashMap();

    /**
     * 获取所有列表
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionInfo>
     * @author kliu
     * @date 2022/5/24 18:09
     */
    @Override
    public List<DetectionInfo> list(){
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from detection_info ");
        db.setSql(stringBuffer.toString());
        return db.dbQuery(DetectionInfo.class);
    }

    /**
     * 依据id获取明细
     * @param detectionId
     * @return com.inspur.industrialinspection.entity.DetectionInfo
     * @author kliu
     * @date 2022/5/24 18:09
     */
    @Override
    public DetectionInfo getDetlById(String detectionId) {
        if (detectionInfoMap.containsKey(detectionId)) {
            return detectionInfoMap.get(detectionId);
        }

        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * from detection_info where detection_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, detectionId);
        List<DetectionInfo> list = db.dbQuery(DetectionInfo.class);
        if (list.size()==0) {
            throw new RuntimeException("传入的detectionId【"+detectionId+"】不存在，请检查传入的数据");
        }
        DetectionInfo detectionInfo = list.get(0);
        detectionInfoMap.put(detectionInfo.getDetectionId(), detectionInfo);
        return detectionInfo;
    }

    /**
     * 校验是否存在
     * @param detectionId
     * @return boolean
     * @author kliu
     * @date 2022/5/24 18:10
     */
    @Override
    public boolean checkExist(String detectionId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select 1 from detection_info where detection_id = ?");
        db.setSql(stringBuffer.toString());
        db.set(1, detectionId);
        return db.dbQuery().size() > 0;
    }
}
