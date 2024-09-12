package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ItAssetTaskResultDao;
import com.inspur.industrialinspection.entity.ItAssetTaskResult;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author: kliu
 * @description: 资产盘点任务结果
 * @date: 2022/7/29 8:35
 */
@Repository
public class ItAssetTaskResultDaoImpl implements ItAssetTaskResultDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 判断数据是否存在
     * @param itAssetTaskResult
     * @return boolean
     * @author kliu
     * @date 2022/7/29 8:37
     */
    @Override
    public boolean checkExist(ItAssetTaskResult itAssetTaskResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 ");
        stringBuffer.append("  from it_asset_task_result ");
        stringBuffer.append("  where instance_id = ? and point_name = ? and qr_code = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAssetTaskResult.getInstanceId());
        db.set(index++, itAssetTaskResult.getPointName());
        db.set(index++, itAssetTaskResult.getQrCode());
        return db.dbQuery().size()>0;
    }

    /**
     * 添加
     * @param itAssetTaskResult
     * @return void
     * @author kliu
     * @date 2022/7/29 8:38
     */
    @Override
    public void add(ItAssetTaskResult itAssetTaskResult) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into it_asset_task_result ");
        stringBuffer.append("  (instance_id, point_name, qr_code) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAssetTaskResult.getInstanceId());
        db.set(index++, itAssetTaskResult.getPointName());
        db.set(index++, itAssetTaskResult.getQrCode());
        db.dbUpdate();
    }
}
