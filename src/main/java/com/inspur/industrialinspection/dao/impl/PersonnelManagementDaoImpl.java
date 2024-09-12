package com.inspur.industrialinspection.dao.impl;

import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.PersonnelManagementDao;
import com.inspur.industrialinspection.entity.PersonnelManagement;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户信息dao实现
 * @author kliu
 * @date 2022/4/21 17:56
 */
@Repository
public class PersonnelManagementDaoImpl implements PersonnelManagementDao {

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private RequestService requestService;

    /**
     * 获取人员列表
     * @param personnelManagement
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/8/1 16:28
     */
    @Override
    public PageBean pageList(PersonnelManagement personnelManagement, int pageSize, int pageNum) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from personnel_management ");
        stringBuffer.append(" where park_id = ? ");
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelType())){
            stringBuffer.append("   and personnel_type = ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelName())){
            stringBuffer.append("   and personnel_name like ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelTel())){
            stringBuffer.append("   and personnel_tel like ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelFirm())){
            stringBuffer.append("   and personnel_firm like ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelDepartment())){
            stringBuffer.append("   and personnel_department like ? ");
        }

        String countSqlStr = stringBuffer.toString();
        stringBuffer.setLength(0);
        stringBuffer.append("select personnel_id, personnel_name, personnel_tel, personnel_firm, personnel_department, ");
        stringBuffer.append("       personnel_type, personnel_email, personnel_introduce, personnel_effective_date, personnel_expiration_date, personnel_url ");
        stringBuffer.append("  from personnel_management ");
        stringBuffer.append(" where park_id = ? ");
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelType())){
            stringBuffer.append("   and personnel_type = ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelName())){
            stringBuffer.append("   and personnel_name like ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelTel())){
            stringBuffer.append("   and personnel_tel like ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelFirm())){
            stringBuffer.append("   and personnel_firm like ? ");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelDepartment())){
            stringBuffer.append("   and personnel_department like ? ");
        }

        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, parkId);
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelType())){
            db.set(index++, personnelManagement.getPersonnelType());
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelName())){
            db.set(index++, "%"+personnelManagement.getPersonnelName()+"%");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelTel())){
            db.set(index++, "%"+personnelManagement.getPersonnelTel()+"%");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelFirm())){
            db.set(index++, "%"+personnelManagement.getPersonnelFirm()+"%");
        }
        if (!StringUtils.isEmpty(personnelManagement.getPersonnelDepartment())){
            db.set(index++, "%"+personnelManagement.getPersonnelDepartment()+"%");
        }

        PageBean objectPageBean = db.dbQueryPage(PersonnelManagement.class, countSqlStr, pageNum, pageSize);
        return objectPageBean;
    }

    /**
     * 添加
     *
     * @param personnelManagement
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    @Override
    public long addAndReturnId(PersonnelManagement personnelManagement) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into personnel_management ");
        stringBuffer.append("  (personnel_id, personnel_name, personnel_tel, personnel_firm, personnel_department, ");
        stringBuffer.append("   personnel_type, personnel_email, personnel_introduce, personnel_effective_date, personnel_expiration_date, ");
        stringBuffer.append("   park_id) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?) ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, personnelManagement.getPersonnelName());
        db.set(index++, personnelManagement.getPersonnelTel());
        db.set(index++, personnelManagement.getPersonnelFirm());
        db.set(index++, personnelManagement.getPersonnelDepartment());
        db.set(index++, personnelManagement.getPersonnelType());

        db.set(index++, personnelManagement.getPersonnelEmail());
        db.set(index++, personnelManagement.getPersonnelIntroduce());
        db.set(index++, personnelManagement.getPersonnelEffectiveDate());
        db.set(index++, personnelManagement.getPersonnelExpirationDate());

        db.set(index++, parkId);
        return db.dbUpdateAndReturnId();
    }

    /**
     * 更新
     *
     * @param personnelManagement
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    @Override
    public void update(PersonnelManagement personnelManagement) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update personnel_management ");
        stringBuffer.append("   set personnel_name = ?, ");
        stringBuffer.append("       personnel_tel = ?, ");
        stringBuffer.append("       personnel_firm = ?, ");
        stringBuffer.append("       personnel_department = ?, ");
        stringBuffer.append("       personnel_type = ?, ");
        stringBuffer.append("       personnel_email = ?, ");
        stringBuffer.append("       personnel_introduce = ?, ");
        stringBuffer.append("       personnel_effective_date = ?, ");
        stringBuffer.append("       personnel_expiration_date = ? ");
        stringBuffer.append(" where personnel_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, personnelManagement.getPersonnelName());
        db.set(index++, personnelManagement.getPersonnelTel());
        db.set(index++, personnelManagement.getPersonnelFirm());
        db.set(index++, personnelManagement.getPersonnelDepartment());
        db.set(index++, personnelManagement.getPersonnelType());
        db.set(index++, personnelManagement.getPersonnelEmail());
        db.set(index++, personnelManagement.getPersonnelIntroduce());
        db.set(index++, personnelManagement.getPersonnelEffectiveDate());
        db.set(index++, personnelManagement.getPersonnelExpirationDate());
        db.set(index++, personnelManagement.getPersonnelId());
        db.dbUpdate();
    }

    /**
     * 删除
     *
     * @param personnelManagement
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    @Override
    public void delete(PersonnelManagement personnelManagement) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from personnel_management where personnel_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, personnelManagement.getPersonnelId());
        db.dbUpdate();
    }

    /**
     * 批量删除
     *
     * @param inPara
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    @Override
    public void batchDelete(String inPara) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from personnel_management where personnel_id in ("+inPara+") ");
        db.setSql(stringBuffer.toString());
        db.dbUpdate();
    }

    /**
     * 更改人像url和人脸特征值
     *
     * @param personnelManagement
     * @return void
     * @author kliu
     * @date 2022/7/21 19:36
     */
    @Override
    public void saveFaceProfileUrlAndFacialFeature(PersonnelManagement personnelManagement) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update personnel_management ");
        stringBuffer.append("   set personnel_url = ?, personnel_facial_feature = ? ");
        stringBuffer.append(" where personnel_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, personnelManagement.getPersonnelUrl());
        db.set(index++, personnelManagement.getPersonnelFacialFeature());
        db.set(index++, personnelManagement.getPersonnelId());
        db.dbUpdate();

    }

    /**
     * 依据人员类型计算总数
     *
     * @return java.util.List
     * @author kliu
     * @date 2022/7/22 16:30
     */
    @Override
    public List countByType() {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select personnel_type, count(1) count ");
        stringBuffer.append("  from personnel_management ");
        stringBuffer.append(" where park_id = ?");
        stringBuffer.append(" group by personnel_type");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, parkId);
        List list = db.dbQuery();
        return list;
    }

    /**
     * 获取访客人数
     * @param startDateStr
     * @return java.util.List
     * @author kliu
     * @date 2022/7/22 16:30
     */
    @Override
    public List visitorCountByDate(String startDateStr) {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from personnel_management ");
        stringBuffer.append(" where park_id = ? ");
        stringBuffer.append("   and personnel_type = 'visitor' ");
        stringBuffer.append("   and (personnel_effective_date > ? or personnel_expiration_date > ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, parkId);
        db.set(index++, startDateStr);
        db.set(index++, startDateStr);
        return db.dbQuery();
    }

    @Override
    public List getDistinctPersonnelDepartment() {
        int parkId = requestService.getParkIdByToken();
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select distinct personnel_department personnelDepartment ");
        stringBuffer.append("  from personnel_management ");
        stringBuffer.append(" where park_id = ? ");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, parkId);
        return db.dbQuery();
    }

    @Override
    public PersonnelManagement getDetlById(long personnelId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select * ");
        stringBuffer.append("  from personnel_management ");
        stringBuffer.append(" where personnel_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, personnelId);
        List<PersonnelManagement> list = db.dbQuery(PersonnelManagement.class);
        if (list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }
}
