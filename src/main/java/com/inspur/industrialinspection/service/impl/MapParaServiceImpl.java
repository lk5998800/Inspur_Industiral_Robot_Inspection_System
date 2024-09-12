package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.dao.MapParaDao;
import com.inspur.industrialinspection.entity.MapPara;
import com.inspur.industrialinspection.service.MapParaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 地图参数
 * @author kliu
 * @date 2022/11/21 9:25
 */
@Service
public class MapParaServiceImpl implements MapParaService {

    @Autowired
    private MapParaDao mapParaDao;


    @Override
    public MapPara getByRoomId(long roomId) {
        return mapParaDao.getByRoomId(roomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MapPara mapPara) {
        mapParaDao.add(mapPara);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MapPara mapPara) {
        mapParaDao.update(mapPara);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(long roomId) {
        mapParaDao.delete(roomId);
    }
}
