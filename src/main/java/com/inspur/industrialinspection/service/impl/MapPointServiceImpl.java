package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.dao.MapPointDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.entity.MapPoint;
import com.inspur.industrialinspection.service.MapPointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地图点位服务
 * @author kliu
 * @date 2022/6/1 15:21
 */
@Service
@Slf4j
public class MapPointServiceImpl implements MapPointService {

    @Autowired
    private MapPointDao mapPointDao;
    @Autowired
    private RoomInfoDao roomInfoDao;

    @Override
    public List list(long roomId) {
        if (!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }
        return mapPointDao.list(roomId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(MapPoint mapPoint) {
        if (!roomInfoDao.checkExist(mapPoint.getRoomId())){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }
        if (mapPointDao.checkIsExist(mapPoint)){
            throw new RuntimeException("添加失败，传入的点位信息已存在");
        }
        mapPointDao.add(mapPoint);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(MapPoint mapPoint) {
        if (!roomInfoDao.checkExist(mapPoint.getRoomId())){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }
        if (!mapPointDao.checkIsExist(mapPoint)){
            throw new RuntimeException("更新失败，传入的点位信息不存在");
        }
        mapPointDao.update(mapPoint);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(MapPoint mapPoint) {
        if (!roomInfoDao.checkExist(mapPoint.getRoomId())){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }
        if (!mapPointDao.checkIsExist(mapPoint)){
            throw new RuntimeException("删除失败，传入的点位信息不存在");
        }
        mapPointDao.delete(mapPoint);
    }
}
