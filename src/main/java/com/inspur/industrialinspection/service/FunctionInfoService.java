package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.FunctionInfo;

import java.util.List;

/**
 * 功能服务
 * @author wangzhaodi
 * @date 2022/11/14 15:22
 */
public interface FunctionInfoService {
    List<FunctionInfo> getFunctionsByUserId(long userId);
    List<FunctionInfo> list(long roleId);
}
