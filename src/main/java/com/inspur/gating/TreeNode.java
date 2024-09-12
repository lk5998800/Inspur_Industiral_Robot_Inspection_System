package com.inspur.gating;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author: kliu
 * @description: 树节点
 * @date: 2022/9/20 11:11
 */
@Data
@AllArgsConstructor
public class TreeNode {
    private String node;
    private boolean doorFlag;
    private String parentNode;
}
