package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.code.Detection;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.DetectionInfoDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.entity.DetectionCombination;
import com.inspur.industrialinspection.entity.DetectionInfo;
import com.inspur.industrialinspection.service.DetectionCombinationService;
import com.inspur.industrialinspection.service.RoomParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 检测项组合service实现
 * @author kliu
 * @date 2022/4/28 8:30
 */
@Service
public class DetectionCombinationServiceImpl implements DetectionCombinationService {

    @Autowired
    RoomParamService roomParamService;
    @Autowired
    DetectionInfoDao detectionInfoDao;
    @Autowired
    RoomInfoDao roomInfoDao;

    /**
     * 获取检测项组合列表
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionCombination>
     * @author kliu
     * @date 2022/5/25 8:41
     */
    @Override
    public List<DetectionCombination> list(long roomId){
        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        List<DetectionCombination> list = new ArrayList<DetectionCombination>();
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray detectionCombinationArray;
        JSONObject detectionCombinationObject;
        JSONArray combinationDetlArray;
        if (roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
            if (inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION)) {
                detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);
                for (int i = 0; i < detectionCombinationArray.size(); i++) {
                    detectionCombinationObject = detectionCombinationArray.getJSONObject(i);
                    combinationDetlArray = detectionCombinationObject.getJSONArray("combination_detl");
                    String detectionGroupDetlStr = "";
                    for (int j = 0; j < combinationDetlArray.size(); j++) {
                        String detectionId = combinationDetlArray.getStr(j);
                        String detectionName = detectionInfoDao.getDetlById(detectionId).getDetectionName();
                        detectionGroupDetlStr += detectionName+"、";
                    }
                    if(detectionGroupDetlStr.endsWith("、")){
                        detectionGroupDetlStr = detectionGroupDetlStr.substring(0, detectionGroupDetlStr.length()-1);
                    }
                    detectionCombinationObject.set("detectionGroupDetlStr",detectionGroupDetlStr);
                }
                list = detectionCombinationArray.toList(DetectionCombination.class);
            }
        }
        return list;
    }

    /**
     * 添加检测项组合
     * @param detectionCombination
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    @Override
    public void add(DetectionCombination detectionCombination) {
        long roomId = detectionCombination.getRoomId();
        String combinationCode = detectionCombination.getCombinationCode();
        String combinationName = detectionCombination.getCombinationName();
        List<String> combinationDetl = detectionCombination.getCombinationDetl();

        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray detectionCombinationArray;
        JSONObject detectionCombinationObject;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            roomParamObject.set(ParaKey.INSPECT_SETTING, new JSONObject());
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION)) {
            inspectSettingObject.set(ParaKey.DETECTION_COMBINATION, new JSONArray());
        }

        detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);
        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            detectionCombinationObject = detectionCombinationArray.getJSONObject(i);
            String combinationCode1 = detectionCombinationObject.getStr("combination_code");
            if (combinationCode1.equals(combinationCode)) {
                throw new RuntimeException("传入的检测项代码重复，请检查传入的数据");
            }
            String combinationName1 = detectionCombinationObject.getStr("combination_name");
            if (combinationName1.equals(combinationName)) {
                throw new RuntimeException("传入的检测项名称重复，请检查传入的数据");
            }
        }
        for (String detectionId : combinationDetl) {
            if (!detectionInfoDao.checkExist(detectionId)) {
                throw new RuntimeException("传入的检测项id不存在请检查传入的数据");
            }
        }

        JSONObject tempObject = new JSONObject();
        tempObject.set("combination_code", combinationCode);
        tempObject.set("combination_name", combinationName);
        tempObject.set("combination_detl", combinationDetl);
        detectionCombinationArray.add(tempObject);

        roomParamService.add(roomId, roomParamObject.toString());
        
    }

    /**
     * 更新检测项组合
     * @param detectionCombination
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    @Override
    public void update(DetectionCombination detectionCombination) {
        long roomId = detectionCombination.getRoomId();
        String combinationCode = detectionCombination.getCombinationCode();

        String combinationName = detectionCombination.getCombinationName();
        List<String> combinationDetl = detectionCombination.getCombinationDetl();

        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray detectionCombinationArray;
        JSONObject detectionCombinationObject;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("当前机房参数中没有该检测项组合代码");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION)) {
            throw new RuntimeException("当前机房参数中没有该检测项组合代码");
        }

        detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);
        boolean combinationCodeExist = false;
        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            detectionCombinationObject = detectionCombinationArray.getJSONObject(i);
            String combinationCode1 = detectionCombinationObject.getStr("combination_code");
            if (combinationCode1.equals(combinationCode)) {
                combinationCodeExist = true;
                detectionCombinationArray.remove(i);
                i--;
            }
        }
        if(!combinationCodeExist){
            throw new RuntimeException("传入的检测项代号不存在，请检查传入的数据");
        }
        for (String detectionId : combinationDetl) {
            if (!detectionInfoDao.checkExist(detectionId)) {
                throw new RuntimeException("传入的检测项id不存在请检查传入的数据");
            }
        }

        JSONObject tempObject = new JSONObject();
        tempObject.set("combination_code", combinationCode);
        tempObject.set("combination_name", combinationName);
        tempObject.set("combination_detl", combinationDetl);
        detectionCombinationArray.add(tempObject);
        roomParamService.add(roomId, roomParamObject.toString());
    }

    /**
     * 添加检测项组合
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    @Override
    public void add(JSONObject jsonObject) {
        long roomId = jsonObject.getLong("roomId");
        String combinationCode = jsonObject.getStr("combinationCode");
        String combinationName = jsonObject.getStr("combinationName");
        JSONArray detectionParaArr = jsonObject.getJSONArray("detectionPara");
        JSONObject tempObject;

        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray detectionCombinationArray;
        JSONObject detectionCombinationObject;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            roomParamObject.set(ParaKey.INSPECT_SETTING, new JSONObject());
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION)) {
            inspectSettingObject.set(ParaKey.DETECTION_COMBINATION, new JSONArray());
        }

        detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);
        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            detectionCombinationObject = detectionCombinationArray.getJSONObject(i);
            String combinationCode1 = detectionCombinationObject.getStr("combination_code");
            if (combinationCode1.equals(combinationCode)) {
                throw new RuntimeException("传入的检测项代码重复，请检查传入的数据");
            }
            String combinationName1 = detectionCombinationObject.getStr("combination_name");
            if (combinationName1.equals(combinationName)) {
                throw new RuntimeException("传入的检测项名称重复，请检查传入的数据");
            }
        }
        JSONArray combinationDetl = new JSONArray();
        for (int i = 0; i < detectionParaArr.size(); i++) {
            tempObject = detectionParaArr.getJSONObject(i);
            String detectionId = tempObject.getStr("detectionId");
            if (!detectionInfoDao.checkExist(detectionId)) {
                throw new RuntimeException("传入的检测项id不存在请检查传入的数据");
            }
            combinationDetl.add(detectionId);

            tempObject.set("detection_id", detectionId);
            tempObject.remove("detectionId");
        }

        tempObject = new JSONObject();
        tempObject.set("combination_code", combinationCode);
        tempObject.set("combination_name", combinationName);
        tempObject.set("combination_detl", combinationDetl);
        tempObject.set("detection_para", detectionParaArr);
        detectionCombinationArray.add(tempObject);

        roomParamService.add(roomId, roomParamObject.toString());

    }

    /**
     * 更新检测项组合
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    @Override
    public void update(JSONObject jsonObject) {
        long roomId = jsonObject.getLong("roomId");
        String combinationCode = jsonObject.getStr("combinationCode");
        String combinationName = jsonObject.getStr("combinationName");
        JSONArray combinationDetlArr = jsonObject.getJSONArray("detectionPara");
        JSONObject tempObject;

        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray detectionCombinationArray;
        JSONObject detectionCombinationObject;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            roomParamObject.set(ParaKey.INSPECT_SETTING, new JSONObject());
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION)) {
            inspectSettingObject.set(ParaKey.DETECTION_COMBINATION, new JSONArray());
        }

        detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);

        //将当前的配置删除
        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            detectionCombinationObject = detectionCombinationArray.getJSONObject(i);
            String combinationCode1 = detectionCombinationObject.getStr("combination_code");
            if (combinationCode1.equals(combinationCode)) {
                detectionCombinationArray.remove(i);
                i--;
            }
        }

        //新添加配置
        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            detectionCombinationObject = detectionCombinationArray.getJSONObject(i);
            String combinationCode1 = detectionCombinationObject.getStr("combination_code");
            if (combinationCode1.equals(combinationCode)) {
                throw new RuntimeException("传入的检测项代码重复，请检查传入的数据");
            }
            String combinationName1 = detectionCombinationObject.getStr("combination_name");
            if (combinationName1.equals(combinationName)) {
                throw new RuntimeException("传入的检测项名称重复，请检查传入的数据");
            }
        }
        JSONArray combinationDetl = new JSONArray();
        for (int i = 0; i < combinationDetlArr.size(); i++) {
            tempObject = combinationDetlArr.getJSONObject(i);
            String detectionId = tempObject.getStr("detectionId");
            if (!detectionInfoDao.checkExist(detectionId)) {
                throw new RuntimeException("传入的检测项id不存在请检查传入的数据");
            }
            combinationDetl.add(detectionId);
            tempObject.set("detection_id", detectionId);
            tempObject.remove("detectionId");
        }
        tempObject = new JSONObject();
        tempObject.set("combination_code", combinationCode);
        tempObject.set("combination_name", combinationName);
        tempObject.set("combination_detl", combinationDetl);
        tempObject.set("detection_para", combinationDetlArr);
        detectionCombinationArray.add(tempObject);

        roomParamService.add(roomId, roomParamObject.toString());
    }

    /**
     * 删除检测项组合
     * @param detectionCombination
     * @return void
     * @author kliu
     * @date 2022/5/25 8:41
     */
    @Override
    public void delete(DetectionCombination detectionCombination){
        long roomId = detectionCombination.getRoomId();
        String combinationCode = detectionCombination.getCombinationCode();

        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        JSONArray detectionCombinationArray;
        JSONObject detectionCombinationObject;
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("当前机房参数中没有该检测项组合代码");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION)) {
            throw new RuntimeException("当前机房参数中没有该检测项组合代码");
        }

        detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);
        boolean combinationCodeExist = false;
        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            detectionCombinationObject = detectionCombinationArray.getJSONObject(i);
            String combinationCode1 = detectionCombinationObject.getStr("combination_code");
            if (combinationCode1.equals(combinationCode)) {
                combinationCodeExist = true;
                detectionCombinationArray.remove(i);
                i--;
            }
        }
        if(!combinationCodeExist){
            throw new RuntimeException("传入的检测项代号不存在，请检查传入的数据");
        }

        //删除时检测项组合代码不能再被巡检类型设置
        if (inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            JSONArray inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);
            for (int i = 0; i < inspectTypeArray.size(); i++) {
                JSONArray inspectDetectionCombinationArray = inspectTypeArray.getJSONObject(i).getJSONArray("inspect_detection_combination");
                for (int j = 0; j < inspectDetectionCombinationArray.size(); j++) {
                    String combinationCode1 = inspectDetectionCombinationArray.getJSONObject(j).getStr("combination_code");
                    if(combinationCode.equals(combinationCode1)){
                        throw new RuntimeException("删除失败，传入的检测项组合代码在巡检类型中被使用");
                    }
                }
                
            }
        }

        roomParamService.add(roomId, roomParamObject.toString());
    }

    /**
     * 获取检测项信息 读取文件
     * @param roomId
     * @param pointName
     * @param inspectTypeId
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionInfo>
     * @author kliu
     * @date 2022/5/25 8:41
     */
    @Override
    public List<DetectionInfo> list(long roomId, String pointName, long inspectTypeId) {
        List<DetectionInfo> list = new ArrayList<>();
        JSONObject inspectTypeObject, inspectDetectionCombinationObject;
        String combinationCode = "";
        JSONArray inspectDetectionCombinationArray, pointNames;
        JSONArray inspectTypeArray, combinationDetlArray;

        if (!roomInfoDao.checkExist(roomId)) {
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);

        JSONArray detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);

        for (int i = 0; i < inspectTypeArray.size(); i++) {
            inspectTypeObject = inspectTypeArray.getJSONObject(i);
            long inspectTypeId1 = inspectTypeObject.getLong("inspect_type_id");
            if (inspectTypeId == inspectTypeId1) {
                inspectDetectionCombinationArray = inspectTypeObject.getJSONArray("inspect_detection_combination");
                for (int j = 0; j < inspectDetectionCombinationArray.size(); j++) {
                    inspectDetectionCombinationObject = inspectDetectionCombinationArray.getJSONObject(j);
                    String tempCombinationCode = inspectDetectionCombinationObject.getStr("combination_code");
                    pointNames = inspectDetectionCombinationObject.getJSONArray("point_names");
                    for (int i1 = 0; i1 < pointNames.size(); i1++) {
                        String tempPointName = pointNames.getStr(i1);
                        if (tempPointName.equals(pointName)) {
                            combinationCode = tempCombinationCode;
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            String combinationCode1 = detectionCombinationArray.getJSONObject(i).getStr("combination_code");
            if(combinationCode.equals(combinationCode1)){
                combinationDetlArray = detectionCombinationArray.getJSONObject(i).getJSONArray("combination_detl");
                for (int j = 0; j < combinationDetlArray.size(); j++) {
                    String detectionId = combinationDetlArray.getStr(j);
                    list.add(detectionInfoDao.getDetlById(detectionId));
                }
                break;
            }
        }
        return list;
    }

    /**
     * 获取检测项信息，不读取文件
     * @param roomParamObject
     * @param pointName
     * @param inspectTypeId
     * @return java.util.List<com.inspur.industrialinspection.entity.DetectionInfo>
     * @author kliu
     * @date 2022/5/25 8:42
     */
    @Override
    public List<DetectionInfo> list(JSONObject roomParamObject, String pointName, long inspectTypeId) {
        List<DetectionInfo> list = new ArrayList<>();
        JSONObject inspectTypeObject, inspectDetectionCombinationObject;
        String combinationCode = "";
        JSONArray inspectDetectionCombinationArray, pointNames;
        JSONArray inspectTypeArray, combinationDetlArray;

        if (!roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        JSONObject inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
        if (!inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE)) {
            throw new RuntimeException("要查询的数据不存在，请检查传入的数据");
        }

        inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE);

        JSONArray detectionCombinationArray;

        for (int i = 0; i < inspectTypeArray.size(); i++) {
            inspectTypeObject = inspectTypeArray.getJSONObject(i);
            long inspectTypeId1 = inspectTypeObject.getLong("inspect_type_id");
            if (inspectTypeId == inspectTypeId1) {
                inspectDetectionCombinationArray = inspectTypeObject.getJSONArray("inspect_detection_combination");
                for (int j = 0; j < inspectDetectionCombinationArray.size(); j++) {
                    if (!StringUtils.isEmpty(combinationCode)){
                        break;
                    }
                    inspectDetectionCombinationObject = inspectDetectionCombinationArray.getJSONObject(j);
                    String tempCombinationCode = inspectDetectionCombinationObject.getStr("combination_code");
                    pointNames = inspectDetectionCombinationObject.getJSONArray("point_names");
                    for (int i1 = 0; i1 < pointNames.size(); i1++) {
                        String tempPointName = pointNames.getStr(i1);
                        if (tempPointName.equals(pointName)) {
                            combinationCode = tempCombinationCode;
                            break;
                        }
                    }
                }
            }
        }

        detectionCombinationArray = inspectSettingObject.getJSONArray("detection_combination");
        DetectionInfo detectionInfo;
        JSONObject jsonObject;

        for (int i = 0; i < detectionCombinationArray.size(); i++) {
            String combinationCode1 = detectionCombinationArray.getJSONObject(i).getStr("combination_code");
            if(combinationCode.equals(combinationCode1)){
                combinationDetlArray = detectionCombinationArray.getJSONObject(i).getJSONArray("combination_detl");
                JSONArray detectionParaArray = detectionCombinationArray.getJSONObject(i).getJSONArray("detection_para");
                for (int j = 0; j < combinationDetlArray.size(); j++) {
                    String detectionId = combinationDetlArray.getStr(j);
                    detectionInfo = detectionInfoDao.getDetlById(detectionId);

                    for (int p = 0; p < detectionParaArray.size(); p++) {
                        jsonObject = detectionParaArray.getJSONObject(p);
                        String detectionId1 = jsonObject.getStr("detection_id");
                        if (detectionId.equals(detectionId1)){
                            if (jsonObject.containsKey("lifter_height")) {
                                detectionInfo.setLifterHeight(jsonObject.getInt("lifter_height"));
                            }
                            if (jsonObject.containsKey("threshold")) {
                                detectionInfo.setThreshold(jsonObject.getStr("threshold"));
                            }
                        }
                    }
                    list.add(detectionInfo);
                }
                break;
            }
        }
        return list;
    }
}
