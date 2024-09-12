package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.FunctionInfo;

import java.util.List;


/**
 * @author wangzhaodi
 * @description 功能信息
 * @date 2022/11/14 14:58
 */
public interface FunctionInfoDao {
    List<FunctionInfo> getFunctionsByUserId(long userId);
    List<FunctionInfo> list(long roleId);
}
