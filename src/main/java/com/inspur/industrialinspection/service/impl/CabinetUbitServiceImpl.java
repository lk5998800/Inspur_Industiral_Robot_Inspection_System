package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.dao.CabinetUbitDao;
import com.inspur.industrialinspection.entity.CabinetUbit;
import com.inspur.industrialinspection.service.CabinetUbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 机柜u位
 * @author kliu
 * @date 2022/11/9 20:21
 */
@Service
@Slf4j
public class CabinetUbitServiceImpl implements CabinetUbitService {

    @Autowired
    private CabinetUbitDao cabinetUbitDao;

    /**
     * 获取列表
     *
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/11/9 20:21
     */
    @Override
    public List list(long roomId) {
        return cabinetUbitDao.list(roomId);
    }

    /**
     * 添加
     * @param cabinetUbit
     * @return void
     * @author kliu
     * @date 2022/11/9 20:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(CabinetUbit cabinetUbit) {
        if (cabinetUbitDao.checkExist(cabinetUbit)){
            cabinetUbitDao.update(cabinetUbit);
        }else{
            cabinetUbitDao.add(cabinetUbit);
        }
    }
}
