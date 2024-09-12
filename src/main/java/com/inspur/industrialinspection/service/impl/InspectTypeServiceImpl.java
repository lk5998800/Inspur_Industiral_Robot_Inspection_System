package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.dao.TaskInfoDao;
import com.inspur.industrialinspection.entity.InspectType;
import com.inspur.industrialinspection.service.InspectTypeService;
import com.inspur.industrialinspection.service.RoomParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 巡检类型服务
 * @author kliu
 * @date 2022/5/25 8:52
 */
@Service
public class InspectTypeServiceImpl implements InspectTypeService {

    @Autowired
    private TaskInfoDao taskInfoDao;
    @Autowired
    private RoomParamService roomParamService;
    @Autowired
    RoomInfoDao roomInfoDao;

    /**
     * 获取巡检类型列表
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.InspectType>
     * @author kliu
     * @date 2022/5/25 8:49
     */
    @Override
    public List<InspectType> list(long roomId) {
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }

        List<InspectType> list = new ArrayList<InspectType>();
        JSONObject settingObject;
        JSONArray inspectTypeArray;
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        if (roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            settingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
            if (settingObject.containsKey(ParaKey.INSPECT_TYPE)) {
                inspectTypeArray = settingObject.getJSONArray(ParaKey.INSPECT_TYPE);
                list = inspectTypeArray.toList(InspectType.class);
            }
        }
        return list;
    }

    /**
     * 添加巡检类型
     * @param inspectType
     * @return void
     * @author kliu
     * @date 2022/5/25 8:49
     */
    @Override
    public void add(InspectType inspectType) {
        long roomId = inspectType.getRoomId();
        String inspectTypeName = inspectType.getInspectTypeName();
        String runMode = inspectType.getRunMode();
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray inspectTypeArray;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            roomParamObject.set(ParaKey.INSPECT_SETTING, new JSONObject());
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            inspectSettingObject.set(ParaKey.INSPECT_TYPE, new JSONArray());
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);
        
        long tempInspectTypeId = 0;
        for (int i = 0; i < inspectTypeArray.size(); i++) {
            long inspectTypeId = inspectTypeArray.getJSONObject(i).getLong("inspect_type_id");
            if (inspectTypeName.equals(inspectTypeArray.getJSONObject(i).getStr("inspect_type_name"))){
                throw new RuntimeException("巡检类型名称不能重复，请检查");
            }
            if (inspectTypeId > tempInspectTypeId) {
                tempInspectTypeId = inspectTypeId;
            }
        }

        JSONObject tempObject = new JSONObject();
        tempObject.set("inspect_detection_combination", new JSONArray());
        tempObject.set("inspect_order", new JSONArray());
        tempObject.set("inspect_type_name", inspectTypeName);
        tempObject.set("run_mode", runMode);
        tempObject.set("inspect_type_id", ++tempInspectTypeId);
        inspectTypeArray.add(tempObject);

        roomParamService.add(roomId, roomParamObject.toString());

    }

    /**
     * 更新巡检类型
     * @param inspectType
     * @return void
     * @author kliu
     * @date 2022/5/25 8:50
     */
    @Override
    public void update(InspectType inspectType) {
        long roomId = inspectType.getRoomId();
        long inspectTypeId = inspectType.getInspectTypeId();
        String inspectTypeName = inspectType.getInspectTypeName();
        String runMode = inspectType.getRunMode();
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray inspectTypeArray;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("要更新的数据不存在，请检查传入的数据");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            throw new RuntimeException("要更新的数据不存在，请检查传入的数据");
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);

        boolean inspectTypeIdExists = false;
        JSONObject jsonObject;
        for (int i = 0; i < inspectTypeArray.size(); i++) {
            jsonObject = inspectTypeArray.getJSONObject(i);
            long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
            if (inspectTypeId == inspectTypeId1) {
                inspectTypeIdExists = true;
                jsonObject.set("run_mode", runMode);
                jsonObject.set("inspect_type_name", inspectTypeName);
            }
        }

        if (!inspectTypeIdExists) {
            throw new RuntimeException("要更新的数据不存在，请检查传入的数据");
        }

        roomParamService.add(roomId, roomParamObject.toString());
    }

    /**
     * 删除巡检类型
     * @param inspectType
     * @return void
     * @author kliu
     * @date 2022/5/25 8:50
     */
    @Override
    public void delete(InspectType inspectType){
        long roomId = inspectType.getRoomId();
        long inspectTypeId = inspectType.getInspectTypeId();
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray inspectTypeArray;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("要删除的数据不存在，请检查传入的数据");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            throw new RuntimeException("要删除的数据不存在，请检查传入的数据");
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);

        boolean inspectTypeIdExists = false;
        JSONObject jsonObject;
        for (int i = 0; i < inspectTypeArray.size(); i++) {
            jsonObject = inspectTypeArray.getJSONObject(i);
            long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
            if (inspectTypeId == inspectTypeId1) {
                //判断当前巡检类型id是否已经配置任务，如已配置不允许删除
                if (taskInfoDao.checkExistByInspectType(roomId, inspectTypeId)){
                    throw new RuntimeException("当前巡检类型已创建任务，不允许删除");
                }

                inspectTypeIdExists = true;
                inspectTypeArray.remove(i);
                break;
            }
        }

        if (!inspectTypeIdExists) {
            throw new RuntimeException("要删除的数据不存在，请检查传入的数据");
        }

        roomParamService.add(roomId, roomParamObject.toString());

        roomParamObject = null;
        inspectSettingObject = null;
        inspectTypeArray = null;
    }

    /**
     * 校验巡检类型是否存在
     * @param roomId
     * @param inspectTypeId
     * @return boolean
     * @author kliu
     * @date 2022/5/25 8:50
     */
    @Override
    public boolean checkIsExist(long roomId, long inspectTypeId) {
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray inspectTypeArray;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            return false;
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            return false;
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);

        JSONObject jsonObject;
        for (int i = 0; i < inspectTypeArray.size(); i++) {
            jsonObject = inspectTypeArray.getJSONObject(i);
            long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
            if (inspectTypeId == inspectTypeId1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取巡检类型明细 读取文件
     * @param roomId
     * @param inspectTypeId
     * @return com.inspur.industrialinspection.entity.InspectType
     * @author kliu
     * @date 2022/5/25 8:50
     */
    @Override
    public InspectType getDetlById(long roomId, long inspectTypeId) {
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray inspectTypeArray;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);

        JSONObject jsonObject;
        for (int i = 0; i < inspectTypeArray.size(); i++) {
            jsonObject = inspectTypeArray.getJSONObject(i);
            long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
            if (inspectTypeId == inspectTypeId1) {
                return jsonObject.toBean(InspectType.class);
            }
        }

        return null;
    }

    /**
     * 获取巡检类型明细不读取文件
     * @param roomParamObject
     * @param inspectTypeId
     * @return com.inspur.industrialinspection.entity.InspectType
     * @author kliu
     * @date 2022/5/25 8:50
     */
    @Override
    public InspectType getDetlById(JSONObject roomParamObject, long inspectTypeId) {
        JSONArray inspectTypeArray;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);

        JSONObject jsonObject;
        for (int i = 0; i < inspectTypeArray.size(); i++) {
            jsonObject = inspectTypeArray.getJSONObject(i);
            long inspectTypeId1 = jsonObject.getLong("inspect_type_id");
            if (inspectTypeId == inspectTypeId1) {
                return jsonObject.toBean(InspectType.class);
            }
        }
        return null;
    }
}
