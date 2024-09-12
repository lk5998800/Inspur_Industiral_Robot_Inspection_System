package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.PersonnelManagement;
import com.inspur.page.PageBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 人员管理服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface PersonnelManagementService {
    /**
     * 获取人员列表
     * @param personnelManagement
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/8/1 16:28
     */
    PageBean pageList(PersonnelManagement personnelManagement, int pageSize, int pageNum);
    /**
     * 添加
     * @param personnelManagement
     * @param file
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void add(PersonnelManagement personnelManagement, MultipartFile file) throws Exception;
    /**
     * 更新
     * @param personnelManagement
     * @param file
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void update(PersonnelManagement personnelManagement, MultipartFile file) throws Exception;
    /**
     * 删除
     * @param personnelManagement
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void delete(PersonnelManagement personnelManagement);
    /**
     * 批量删除
     * @param jsonArray
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void batchDelete(JSONArray jsonArray);
    /**
     * 保存人像照片
     * @param multipartFile
     * @param personnelId
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:02
     */
    void saveFrofile(MultipartFile multipartFile, long personnelId) throws Exception;
    /**
     * 获取人员信息概览
     * @param
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/7/22 16:27
     */
    JSONObject getOverView();

    /**
     * 获取唯一部门信息
     * @return java.util.List
     * @author kliu
     * @date 2022/9/7 10:46
     */
    List getDistinctPersonnelDepartment();
}
