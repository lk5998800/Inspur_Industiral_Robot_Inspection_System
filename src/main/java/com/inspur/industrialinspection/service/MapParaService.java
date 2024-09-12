package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.MapPara;

/**
 * 地图参数
 * @author kliu
 * @date 2022/11/21 9:24
 */
public interface MapParaService {
    MapPara getByRoomId(long roomId);
    void add(MapPara mapPara);
    void update(MapPara mapPara);
    void delete(long roomId);
}
