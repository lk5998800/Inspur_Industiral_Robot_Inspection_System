package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.code.ParaKey;
import com.inspur.industrialinspection.dao.DetectionInfoDao;
import com.inspur.industrialinspection.dao.ParkInfoDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.service.PointSetService;
import com.inspur.industrialinspection.service.RoomParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 检测点设置
 * @author kliu
 * @date 2022/5/25 8:58
 */
@Service
public class PointSetServiceImpl implements PointSetService {

    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private ParkInfoDao parkInfoDao;
    @Autowired
    private DetectionInfoDao detectionInfoDao;

    @Autowired
    RoomParamService roomParamService;

    /**
     * 获取检测点设置信息
     * @param roomId
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/5/25 8:58
     */
    @Override
    public JSONObject list(long roomId) throws RuntimeException {
        JSONObject inspectSettingObject = new JSONObject();
        JSONObject jsonObject;
        JSONArray detectionCombinationArr, detectionCombinationDetlArr;
        String detectionNameStr;
        String inspectSettingStr = "";
        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);

        if (roomParamObject.containsKey(ParaKey.INSPECT_SETTING)) {
            inspectSettingObject = roomParamObject.getJSONObject(ParaKey.INSPECT_SETTING);
            if (inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION)) {
                detectionCombinationArr = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION);
                for (int i = 0; i < detectionCombinationArr.size(); i++) {
                    jsonObject = detectionCombinationArr.getJSONObject(i);
                    detectionNameStr = "";
                    detectionCombinationDetlArr = detectionCombinationArr.getJSONObject(i).getJSONArray("combination_detl");
                    for (int j = 0; j < detectionCombinationDetlArr.size(); j++) {
                        String detectionName = detectionInfoDao.getDetlById(detectionCombinationDetlArr.getStr(j)).getDetectionName();
                        detectionNameStr+=detectionName+',';

                    }
                    if(detectionNameStr.endsWith(",")){
                        detectionNameStr = detectionNameStr.substring(0, detectionNameStr.length()-1);
                    }
                    jsonObject.set("detection_str", detectionNameStr);
                }
            }
        }
        inspectSettingStr = inspectSettingObject.toString();
        inspectSettingStr = inspectSettingStr.replace("inspect_detection_combination","inspectDetectionCombination");
        inspectSettingStr = inspectSettingStr.replace("detection_combination","detectionCombination");
        inspectSettingStr = inspectSettingStr.replace("combination_code","combinationCode");
        inspectSettingStr = inspectSettingStr.replace("combination_name","combinationName");
        inspectSettingStr = inspectSettingStr.replace("combination_detl","combinationDetl");
        inspectSettingStr = inspectSettingStr.replace("inspect_type_id","inspectTypeId");
        inspectSettingStr = inspectSettingStr.replace("inspect_type_name","inspectTypeName");
        inspectSettingStr = inspectSettingStr.replace("inspect_type","inspectType");
        inspectSettingStr = inspectSettingStr.replace("inspect_order","inspectOrder");
        inspectSettingStr = inspectSettingStr.replace("point_names","pointNames");
        inspectSettingStr = inspectSettingStr.replace("run_mode","runMode");
        inspectSettingStr = inspectSettingStr.replace("detection_str","detectionStr");
        inspectSettingStr = inspectSettingStr.replace("detection_para","detectionPara");
        inspectSettingStr = inspectSettingStr.replace("detection_id","detectionId");

        JSONObject resultObject = new JSONObject();
        resultObject.set("roomId", roomId);
        resultObject.set("inspectSetting", JSONUtil.parseObj(inspectSettingStr));
        inspectSettingObject = null;
        detectionCombinationArr = null;
        detectionCombinationDetlArr = null;
        jsonObject = null;
        inspectSettingObject = null;
        roomParamObject = null;
        return resultObject;
    }

    /**
     * 添加检测点设置信息
     * @param pointSetObject
     * @return void
     * @author kliu
     * @date 2022/5/25 8:58
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public void adds(JSONObject pointSetObject) {
        if (!pointSetObject.containsKey(ParaKey.ROOM_ID_LOWER_CAMEL_CASE)) {
            throw new RuntimeException("roomId不能为空");
        }
        long roomId = pointSetObject.getLong("roomId");
        if (roomId == 0) {
            throw new RuntimeException("roomId不能为空");
        }

        if(!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入的机房id不存在，请检查传入的数据");
        }

        JSONObject jsonObject;
        JSONArray inspectTypeArray,inspectTypeDetlArray,inspectOrderArray,tempArr,jsonArray,pointNamesArr;
        JSONArray detectionCombinationArray = new JSONArray();
        String inspectSettingKey = "inspectSetting";
        if (!pointSetObject.containsKey(inspectSettingKey)){
            throw new RuntimeException("inspectSetting字段不存在，请检查");
        }
        JSONObject inspectSettingObject = pointSetObject.getJSONObject(inspectSettingKey);

        //校验检测项组合是否符合逻辑
        checkDetectionCombination(inspectSettingObject);

        if (inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION_LOWER_CAMEL_CASE)) {
            detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION_LOWER_CAMEL_CASE);
        }

        if (inspectSettingObject.containsKey(ParaKey.INSPECT_TYPE_LOWER_CAMEL_CASE)) {
            inspectTypeArray = inspectSettingObject.getJSONArray(ParaKey.INSPECT_TYPE_LOWER_CAMEL_CASE);
            for (int i = 0; i < inspectTypeArray.size(); i++) {
                jsonObject = inspectTypeArray.getJSONObject(i);
                long inspectTypeId = 0;
                if (jsonObject.containsKey(ParaKey.INSPECT_TYPE_ID_LOWER_CAMEL_CASE)) {
                    inspectTypeId = jsonObject.getLong(ParaKey.INSPECT_TYPE_ID_LOWER_CAMEL_CASE);
                }
                if(!jsonObject.containsKey(ParaKey.INSPECT_TYPE_NAME_LOWER_CAMEL_CASE)){
                    throw new RuntimeException("传入的巡检类型名称不能为空，请检查传入的数据");
                }
                String inspectTypeName = jsonObject.getStr("inspectTypeName");
                //新增时巡检类型id为0，取最大值+1作为新建的巡检类型的id
                if(inspectTypeId ==0){
                    long tempInspectTypeId = 0;
                    for (int j = 0; j < inspectTypeArray.size(); j++) {
                        long temp = 0;
                        if (inspectTypeArray.getJSONObject(j).containsKey("inspectTypeId")) {
                            temp = inspectTypeArray.getJSONObject(j).getLong("inspectTypeId");
                        }
                        if(temp > tempInspectTypeId){
                            tempInspectTypeId = temp;
                        }
                    }
                    inspectTypeId = tempInspectTypeId+1;
                }
                jsonObject.set("inspectTypeId", inspectTypeId);

                //巡检类型id重复检测
                for (int j = i+1; j < inspectTypeArray.size(); j++) {
                    long inspectTypeId1 = inspectTypeArray.getJSONObject(j).getLong("inspectTypeId");
                    if(inspectTypeId == inspectTypeId1){
                        throw new RuntimeException("巡检类型id【"+inspectTypeId+"】不能重复，请检查传入的数据");
                    }
                }
                //巡检类型检测项组合重复检测
                if (jsonObject.containsKey("inspectDetectionCombination")) {
                    inspectTypeDetlArray = jsonObject.getJSONArray("inspectDetectionCombination");
                    for (int j = 0; j < inspectTypeDetlArray.size(); j++) {
                        //巡检类型下检测项组合代号重复检测
                        String combinationCode = inspectTypeDetlArray.getJSONObject(j).getStr("combinationCode");
                        for (int p = j+1; p < inspectTypeDetlArray.size()-1; p++) {
                            String combinationCode1 = inspectTypeDetlArray.getJSONObject(p).getStr("combinationCode");
                            if(combinationCode1.equals(combinationCode)){
                                throw new RuntimeException("巡检类型【"+inspectTypeName+"】下检测项组合代码【"+combinationCode+"】重复，请检查传入的数据");
                            }
                        }

                        //巡检组合下配置的点位重复性校验
                        pointNamesArr = inspectTypeDetlArray.getJSONObject(j).getJSONArray("pointNames");
                        for (int p = 0; p < pointNamesArr.size(); p++) {
                            String pointName = pointNamesArr.getStr(p);
                            for (int q = p+1; q < pointNamesArr.size(); q++) {
                                String tempPointName = pointNamesArr.getStr(q);
                                if (pointName.equals(tempPointName)) {
                                    throw new RuntimeException("巡检类型【"+inspectTypeName+"】下检测项组合代号【"+combinationCode+"】下配置的点位【"+pointName+"】存在重复数据，请检查");
                                }
                            }
                        }

                        //巡检类型下检测项组合代号不存在
                        boolean combinationCodeExistFlag = false;
                        String tempCombinationCode = combinationCode;
                        for (int q = 0; q < detectionCombinationArray.size(); q++) {
                            String combinationCode2 = detectionCombinationArray.getJSONObject(q).getStr("combinationCode");
                            if(combinationCode.equals(combinationCode2)){
                                combinationCodeExistFlag = true;
                                break;
                            }
                        }

                        if(!combinationCodeExistFlag){
                            throw new RuntimeException("巡检类型【"+inspectTypeName+"】中传入的检测项组合代码【"+tempCombinationCode+"】在检测项组合中不存在，请检查传入的数据");
                        }
                    }
                }

                //巡检顺序重复检测
                if (jsonObject.containsKey("inspectOrder")) {
                    inspectOrderArray = jsonObject.getJSONArray("inspectOrder");
                    tempArr = new JSONArray();
                    for (int j = 0; j < inspectOrderArray.size(); j++) {
                        jsonArray = inspectOrderArray.getJSONArray(j);
                        if (jsonArray.size()==0){
                            inspectOrderArray.remove(j);
                            j--;
                        }
                        for (int p = 0; p < jsonArray.size(); p++) {
                            tempArr.add(jsonArray.getStr(p));
                        }
                    }
                    for (int j = 0; j < tempArr.size(); j++) {
                        String pointName = tempArr.getStr(j);
                        for (int p = j+1; p < tempArr.size()-1; p++) {
                            String pointName1 = tempArr.getStr(p);
                            if(pointName1.equals(pointName)){
                                throw new RuntimeException("巡检类型【"+inspectTypeName+"】巡检顺序中存在重复数据，请检查");
                            }
                        }
                    }
                }
            }
        }

        JSONObject roomParamObject = roomParamService.getRoomParam(roomId);
        String inspectSettingStr = inspectSettingObject.toString();
        inspectSettingStr = camelCaseToUnderscore(inspectSettingStr);

        roomParamObject.set(ParaKey.INSPECT_SETTING, JSONUtil.parseObj(inspectSettingStr));

        roomParamService.add(roomId, roomParamObject.toString());

        inspectSettingObject = null;
        roomParamObject = null;
        inspectTypeArray = null;
        inspectTypeDetlArray = null;
        inspectOrderArray = null;
        tempArr = null;
        jsonArray = null;
    }


    /**
     * 校验检测项组合
     * @param inspectSettingObject
     * @return void
     * @author kliu
     * @date 2022/6/21 8:53
     */
    private void checkDetectionCombination(JSONObject inspectSettingObject){
        if (inspectSettingObject.containsKey(ParaKey.DETECTION_COMBINATION_LOWER_CAMEL_CASE)) {
            JSONArray detectionCombinationArray = inspectSettingObject.getJSONArray(ParaKey.DETECTION_COMBINATION_LOWER_CAMEL_CASE);
            JSONObject jsonObject;
            JSONArray combinationDetlArr;
            for (int i = 0; i < detectionCombinationArray.size(); i++) {
                jsonObject = detectionCombinationArray.getJSONObject(i);
                if(jsonObject.containsKey(ParaKey.DETECTION_STR_LOWER_CAMEL_CASE)){
                    jsonObject.remove(ParaKey.DETECTION_STR_LOWER_CAMEL_CASE);
                }
                String combinationCode = jsonObject.getStr("combinationCode");
                for (int j = i+1; j < detectionCombinationArray.size(); j++) {
                    String combinationCode1 = detectionCombinationArray.getJSONObject(j).getStr("combinationCode");
                    if(combinationCode.equals(combinationCode1)){
                        throw new RuntimeException("检测项代号不能重复，请检查传入的数据");
                    }
                }
                combinationDetlArr = jsonObject.getJSONArray("combinationDetl");
                for (int j = 0; j < combinationDetlArr.size(); j++) {
                    String detectionId = combinationDetlArr.getStr(j);
                    if(!detectionInfoDao.checkExist(detectionId)){
                        throw new RuntimeException("传入的检测项数据【"+detectionId+"】不存在，请检查传入的数据");
                    }
                }
            }
            detectionCombinationArray = null;
            jsonObject = null;
            combinationDetlArr = null;
        }
    }

    /**
     * 驼峰转下划线
     * @param str
     * @return java.lang.String
     * @author kliu
     * @date 2022/6/21 8:51
     */
    private String camelCaseToUnderscore(String str){
        str = str.replace("detectionCombination","detection_combination");
        str = str.replace("combinationCode","combination_code");
        str = str.replace("combinationName","combination_name");
        str = str.replace("combinationDetl","combination_detl");
        str = str.replace("inspectTypeId","inspect_type_id");
        str = str.replace("inspectTypeName","inspect_type_name");
        str = str.replace("inspectType","inspect_type");
        str = str.replace("inspectDetectionCombination","inspect_detection_combination");
        str = str.replace("inspectOrder","inspect_order");
        str = str.replace("pointNames","point_names");
        str = str.replace("runMode","run_mode");
        str = str.replace("detectionPara","detection_para");
        return str;
    }
}
