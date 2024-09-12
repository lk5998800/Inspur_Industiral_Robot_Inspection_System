package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.MapPara;

public interface MapParaDao {
    MapPara getByRoomId(long roomId);
    void add(MapPara mapPara);
    void update(MapPara mapPara);
    void delete(long roomId);
}
