package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.io.FileUtil;
import com.inspur.gating.TreeNode;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.dao.ParkInfoDao;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.entity.ParkInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.industrialinspection.service.PointTreeNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kliu
 * @description: 点位树
 * @date: 2022/10/24 15:15
 */
@Service
public class PointTreeNodeServiceImpl implements PointTreeNodeService {
    @Value("${param.parent.path}")
    private String paramParentPath;
    @Value("${param.filename.roomPoint}")
    private String roomPointFilename;
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private ParkInfoDao parkInfoDao;

    private volatile static ConcurrentHashMap<Long, JSONObject> pointTreeMap = new ConcurrentHashMap();

    @Override
    public List<TreeNode> getPointTreeNode(long roomId) {
        JSONObject jsonObjectCache = new JSONObject();
        if (pointTreeMap.containsKey(roomId)) {
            jsonObjectCache = pointTreeMap.get(roomId);
            long timestamp = jsonObjectCache.getLong("timestamp");
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis-timestamp<20000){
                return pointTreeMap.get(roomId).getJSONArray("nodeList").toList(TreeNode.class);
            }
        }

        RoomInfo roomInfo = roomInfoDao.getDetlById(roomId);
        ParkInfo parkInfo = parkInfoDao.getDetlById(roomInfo.getParkId());
        String filePath = (paramParentPath+roomPointFilename).replace("{roomid}", roomId+"").replace("{parkpinyin}",parkInfo.getParkPinyin());
        File file = new File(filePath);
        if (!file.exists()){
            return null;
        }

        List<TreeNode> nodeList = new ArrayList<>();
        String str = FileUtil.readUtf8String(filePath);
        JSONObject jsonObject = new JSONObject(str);
        String node = jsonObject.getStr("node");
        TreeNode rootTreeNode = new TreeNode(node, false, null);
        nodeList.add(rootTreeNode);
        JSONArray child = jsonObject.getJSONArray("child");
        createTreeNode(rootTreeNode, child, nodeList);

        jsonObjectCache.set("timestamp", System.currentTimeMillis());
        jsonObjectCache.set("nodeList", nodeList);
        pointTreeMap.put(roomId, jsonObjectCache);

        return nodeList;
    }

    @Override
    public JSONArray getPoint2ChargingPilePath(long roomId, String pointName) {
        //随工任务特殊处理
        if (pointName.equals("随工任务结束")){
            pointName = "ADMD";
        }
        pointName = pointName.replaceAll("-内开", "").replaceAll("-内关", "").replaceAll("-外开", "").replaceAll("-外关", "");
        String basicPointName = pointName;
        JSONArray jsonArray = new JSONArray();
        //点位节点数据
        JSONArray pointArray = new JSONArray();
        //充电桩节点数据
        JSONArray backChargingPileArray = new JSONArray();
        String backChargingPileNode = "charging_pile";
        List<TreeNode> pointTreeNodes = getPointTreeNode(roomId);
        if (pointTreeNodes == null || pointTreeNodes.size()==0){
            return jsonArray;
        }

        //在实际使用过程中发现过死循环的问题，此处添加个判断
        //先判断点位一定在配置中，如果不在配置中就报错，因为配置点位要是不在就都不在，不能存在两可的情况
        boolean  pointTreeNodesHasPointName = false;
        for (int i = 0; i < pointTreeNodes.size(); i++) {
            if (pointName.equals(pointTreeNodes.get(i).getNode())){
                pointTreeNodesHasPointName = true;
                break;
            }
        }

        if (!pointTreeNodesHasPointName){
            throw new RuntimeException("路径规划失败，点位【"+pointName+"】在配置文件中不存在，请检查");
        }

        //计算充电桩到根节点
        while (true){
            boolean terminate = false;
            for (int i = 0; i < pointTreeNodes.size(); i++) {
                if (backChargingPileNode.equals(pointTreeNodes.get(i).getNode())){
                    //当前节点充电桩需要添加进路径中
                    backChargingPileArray.add(backChargingPileNode);
                    backChargingPileNode = pointTreeNodes.get(i).getParentNode();
                    if (backChargingPileNode == null){
                        terminate = true;
                    }
                    break;
                }
            }
            if (terminate){
                break;
            }
        }

        while (true){
            boolean terminate = false;
            for (int i = 0; i < pointTreeNodes.size(); i++) {
                if (pointName.equals(pointTreeNodes.get(i).getNode())){
                    //当前节点不需要添加进路径中
                    pointName = pointTreeNodes.get(i).getParentNode();
                    if (pointName == null){
                        terminate = true;
                    }else{
                        pointArray.add(pointName);
                    }
                    break;
                }
            }
            if (terminate){
                break;
            }
        }

        while (true){
            if (backChargingPileArray.size()>0 && pointArray.size()>0){
                String str = backChargingPileArray.getStr(backChargingPileArray.size() - 1);
                String str1 = pointArray.getStr(pointArray.size() - 1);
                if (str.equals(str1)){
                    backChargingPileArray.remove(backChargingPileArray.size() - 1);
                    pointArray.remove(pointArray.size() - 1);
                }else{
                    break;
                }
            }else{
                break;
            }
        }

        for (int i = 0; i < pointArray.size(); i++) {
            jsonArray.add(pointArray.get(i));
        }

        for (int i = backChargingPileArray.size()-1; i>=0; i--) {
            jsonArray.add(backChargingPileArray.get(i));
        }

        //点位规划回充电桩路径时，不应包含当前点位信息
        for (int i = 0; i < jsonArray.size(); i++) {
            if (jsonArray.getStr(i).equals(basicPointName)) {
                jsonArray.remove(i);
                i--;
            }

        }

        return jsonArray;
    }

    @Override
    public JSONArray getChargingPilePath2Point(long roomId, String pointName) {
        JSONArray resultArray = new JSONArray();
        JSONArray jsonArray = getPoint2ChargingPilePath(roomId, pointName);
        //-2 过滤充电桩
        for (int i = jsonArray.size() - 2; i >= 0; i--) {
            resultArray.add(jsonArray.get(i));
        }

        //如果点位为门，则添加当前点位
        if (pointName.indexOf("门")>-1) {
            resultArray.add(pointName);
        }

        return resultArray;
    }

    @Override
    public JSONArray getPoint2PointPath(long roomId, String pointNameBasic, String pointNameTarget) {
        JSONArray pathArr = new JSONArray();
        JSONArray point2ChargingPilePath = getPoint2ChargingPilePath(roomId, pointNameBasic);
        JSONArray chargingPilePath2Point = getChargingPilePath2Point(roomId, pointNameTarget);
        for (int i = 0; i < point2ChargingPilePath.size(); i++) {
            String str = point2ChargingPilePath.getStr(i);
            if ("charging_pile".equals(str)){
                point2ChargingPilePath.remove(i);
                i--;
            }
        }

        while (point2ChargingPilePath.size()>0 && chargingPilePath2Point.size()>0){
            if (point2ChargingPilePath.getStr(point2ChargingPilePath.size()-1).equals(chargingPilePath2Point.getStr(0))){
                point2ChargingPilePath.remove(point2ChargingPilePath.size()-1);
                chargingPilePath2Point.remove(0);
            }else{
                break;
            }
        }

        for (int i = 0; i < point2ChargingPilePath.size(); i++) {
            pathArr.add(point2ChargingPilePath.getStr(i));
        }

        for (int i = 0; i < chargingPilePath2Point.size(); i++) {
            pathArr.add(chargingPilePath2Point.getStr(i));
        }

        for (int i = 0; i < pathArr.size(); i++) {
            //如果当前节点为门 下一节点也为门且门的点位带有两个横线  如去除门的最后一位其余相同，则认为在同一个冷通道内
            String str1 = pathArr.getStr(i);
            str1 = str1.substring(0, str1.length() - 1);
            if (pathArr.size() > i+1){
                String str2 = pathArr.getStr(i+1);
                str2 = str2.substring(0, str2.length() - 1);
                if (str1.equals(str2)){
                    pathArr.remove(i+1);
                    pathArr.remove(i);
                    i--;
                }
            }
        }

        return pathArr;
    }

    @Override
    public TreeNode getTreeNode(long roomId, String node) {
        TreeNode treeNode = null;
        List<TreeNode> pointTreeNodes = getPointTreeNode(roomId);
        if (pointTreeNodes != null){
            for (int i = 0; i < pointTreeNodes.size(); i++) {
                if (node.equals(pointTreeNodes.get(i).getNode())) {
                    return pointTreeNodes.get(i);
                }
            }
        }

        return treeNode;
    }

    private void createTreeNode(TreeNode parent, JSONArray child, List<TreeNode> nodeList){
        TreeNode treeNode;
        JSONObject jsonObject;
        JSONArray childArray;
        Map map;
        for (int i = 0; i < child.size(); i++) {
            jsonObject = child.getJSONObject(i);
            String node = jsonObject.getStr("node");
            boolean doorFlag = jsonObject.getBool("door", false);
            treeNode = new TreeNode(node, doorFlag, parent.getNode());
            nodeList.add(treeNode);
            childArray = jsonObject.getJSONArray("child");
            if (childArray != null && childArray.size() > 0){
                createTreeNode(treeNode, childArray, nodeList);
            }
        }
    }
}
