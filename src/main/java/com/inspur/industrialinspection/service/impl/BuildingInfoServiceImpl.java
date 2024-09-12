package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.dao.BuildingInfoDao;
import com.inspur.industrialinspection.entity.BuildingInfo;
import com.inspur.industrialinspection.service.BuildingInfoService;
import com.inspur.industrialinspection.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 楼栋信息服务实现
 * @author kliu
 * @date 2022/6/17 19:23
 */
@Service
public class BuildingInfoServiceImpl implements BuildingInfoService {

    @Autowired
    RequestService requestService;

    @Autowired
    BuildingInfoDao buildingInfoDao;

    @Override
    public List list() {
        int parkId = requestService.getParkIdByToken();
        List<BuildingInfo> list = buildingInfoDao.list(parkId);
        return list;
    }

    /**
     * 获取楼栋信息
     *
     * @param parkId
     * @return java.util.List
     * @author kliu
     * @date 2022/9/1 16:32
     */
    @Override
    public List getBuildingInfos(int parkId) {
        List<BuildingInfo> list = buildingInfoDao.list(parkId);
        for (BuildingInfo buildingInfo : list) {
            buildingInfo.setParkId(0);
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(BuildingInfo buildingInfo) {
        buildingInfo.setParkId(requestService.getParkIdByToken());
        buildingInfoDao.add(buildingInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(BuildingInfo buildingInfo) {
        buildingInfo.setParkId(requestService.getParkIdByToken());
        buildingInfoDao.update(buildingInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(BuildingInfo buildingInfo) {
        buildingInfoDao.delete(buildingInfo.getBuildingId());
    }
}
