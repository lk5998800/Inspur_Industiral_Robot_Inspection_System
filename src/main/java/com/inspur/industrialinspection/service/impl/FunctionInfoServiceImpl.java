package com.inspur.industrialinspection.service.impl;

import com.inspur.industrialinspection.dao.FunctionInfoDao;
import com.inspur.industrialinspection.entity.FunctionInfo;
import com.inspur.industrialinspection.service.FunctionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 功能信息服务实现
 * @author wangzhaodi
 * @date 2022/11/14 15:22
 */
@Service
public class FunctionInfoServiceImpl implements FunctionInfoService {

    @Autowired
    private FunctionInfoDao functionInfoDao;

    @Override
    public List<FunctionInfo> getFunctionsByUserId(long userId) {
        return functionInfoDao.getFunctionsByUserId(userId);
    }

    @Override
    public List<FunctionInfo> list(long roleId) {
        return functionInfoDao.list(roleId);
    }
}
