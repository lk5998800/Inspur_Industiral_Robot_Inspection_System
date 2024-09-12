package com.inspur.industrialinspection.service;

import com.inspur.gating.TreeNode;
import cn.hutool.json.JSONArray;

import java.util.List;

/**
 * @author: kliu
 * @description: 点位树
 * @date: 2022/10/24 15:14
 */
public interface PointTreeNodeService {
    /**
     * 获取机房点位树
     * @param
     * @return com.inspur.gating.TreeNode
     * @author kliu
     * @date 2022/10/24 15:16
     */
    List<TreeNode> getPointTreeNode(long roomId);

    /**
     * 根据点位计算返回充电桩路径
     * @param roomId
     * @param pointName
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/20 14:51
     */
    JSONArray getPoint2ChargingPilePath(long roomId, String pointName);
    /**
     * 根据充电桩计算到达点位路径
     * @param roomId
     * @param pointName
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/20 14:51
     */
    JSONArray getChargingPilePath2Point(long roomId, String pointName);

    /**
     * 计算点位到点位之间的路径
     * @param roomId
     * @param pointNameBasic
     * @param pointNameTarget
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/24 15:59
     */
    JSONArray getPoint2PointPath(long roomId, String pointNameBasic, String pointNameTarget);

    /**
     * 依据机房id 点位获取树节点
     * @param roomId
     * @param node
     * @return com.inspur.gating.TreeNode
     * @author kliu
     * @date 2022/10/24 19:29
     */
    TreeNode getTreeNode(long roomId, String node);
}
