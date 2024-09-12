package com.inspur.industrialinspection.dao.impl;

import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.CodeConfigDao;
import com.inspur.industrialinspection.entity.CodeConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * code信息
 * @author kliu
 * @date 2022/9/7 10:57
 */
@Repository
public class CodeConfigDaoImpl implements CodeConfigDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 获取列表
     * @return java.util.List
     * @author kliu
     * @date 2022/9/7 10:57
     */
    @Override
    public List<CodeConfig> list() {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select code, value, content from code_config");
        db.setSql(stringBuffer.toString());
        return db.dbQuery(CodeConfig.class);
    }
}
