package com.inspur.industrialinspection.dao;

import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.entity.AlongWork;
import com.inspur.industrialinspection.entity.WorkDay;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 随工任务dao实现
 * @author kliu
 * @date 2022/6/13 11:21
 */
@Repository
public class WorkDayDaoImpl implements WorkDayDao {

    @Autowired
    private BeanFactory beanFactory;


    /**
     * 获取明细数据
     * @param dateStr
     * @return com.inspur.industrialinspection.entity.WorkDay
     * @author kliu
     * @date 2022/7/27 15:21
     */
    @Override
    public WorkDay getDetlById(String dateStr) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from work_day where date_str = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, dateStr);
        List<WorkDay> list = db.dbQuery(WorkDay.class);
        if (list.size()==0){
            return null;
        }else{
            return list.get(0);
        }
    }
}
