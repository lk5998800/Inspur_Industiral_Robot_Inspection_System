package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.PersonnelManagement;
import com.inspur.page.PageBean;

import java.util.List;

/**
 * 人员管理
 * @author kliu
 * @date 2022/7/21 17:53
 */
public interface PersonnelManagementDao {
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
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    long addAndReturnId(PersonnelManagement personnelManagement) throws Exception;
    /**
     * 更新
     * @param personnelManagement
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void update(PersonnelManagement personnelManagement);
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
     * @param inPara
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    void batchDelete(String inPara);

    /**
     * 更改人像url和人脸特征值
     * @param personnelManagement
     * @return void
     * @author kliu
     * @date 2022/7/21 19:36
     */
    void saveFaceProfileUrlAndFacialFeature(PersonnelManagement personnelManagement);

    /**
     * 依据人员类型计算总数
     * @param
     * @return java.util.List
     * @author kliu
     * @date 2022/7/22 16:30
     */
    List countByType();

    /**
     * 获取访客人数
     * @param startDateStr
     * @return java.util.List
     * @author kliu
     * @date 2022/7/22 16:59
     */
    List visitorCountByDate(String startDateStr);

    /**
     * 获取唯一部门信息
     * @return java.util.List
     * @author kliu
     * @date 2022/9/7 10:47
     */
    List getDistinctPersonnelDepartment();

    /**
     * 获取明细
     * @param personnelId
     * @return com.inspur.industrialinspection.entity.PersonnelManagement
     * @author kliu
     * @date 2022/8/24 14:31
     */
    PersonnelManagement getDetlById(long personnelId);
}
