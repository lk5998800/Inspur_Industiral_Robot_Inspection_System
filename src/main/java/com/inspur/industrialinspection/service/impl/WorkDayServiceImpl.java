package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.industrialinspection.dao.WorkDayDao;
import com.inspur.industrialinspection.entity.WorkDay;
import com.inspur.industrialinspection.service.WorkDayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 工作日服务
 * @author kliu
 * @date 2022/7/27 15:24
 */
@Service
@Slf4j
public class WorkDayServiceImpl implements WorkDayService {

    @Autowired
    private WorkDayDao workDayDao;

    /**
     * 判断是否是工作日
     * @param dateStr
     * @return boolean
     * @author kliu
     * @date 2022/7/27 15:23
     */
    @Override
    public boolean isWorkDay(String dateStr) {
        //判断周几的时候需要减1,   1-7  1代表周日
        int dayOfWeek = DateUtil.dayOfWeek(DateUtil.parse(dateStr))-1;
        if (dayOfWeek == 0){
            dayOfWeek = 7;
        }
        boolean workDayFlag = false;
        WorkDay workDay = workDayDao.getDetlById(dateStr);
        //工作日表里不存在数据，则认为按照正常工作日进行，无调休无假期
        if (workDay==null){
            //noinspection AlibabaUndefineMagicConstant
            if (dayOfWeek>=1 && dayOfWeek<=5) {
                workDayFlag = true;
            }else{
                workDayFlag = false;
            }
        }else{
            //工作日存在表里，认为是假期
            //noinspection AlibabaUndefineMagicConstant
            if (dayOfWeek>=1 && dayOfWeek<=5) {
                workDayFlag = false;
            }else{//非工作日在表里，认为是调休
                workDayFlag = true;
            }
        }
        return workDayFlag;
    }
}