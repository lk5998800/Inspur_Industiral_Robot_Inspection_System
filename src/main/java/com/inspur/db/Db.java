package com.inspur.db;

import com.inspur.page.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * jdbctemplate封装类，主要封装访问数据库代码逻辑
 * @author kliu
 * @date 2022/5/24 19:41
 */
@Component
@Scope("prototype")
public class Db {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private String sql = "";
    @SuppressWarnings("rawtypes")
    private ArrayList param = null;
    private ArrayList<Object[]> batchParaList = null;
    private boolean batchFlag = false;

    public Db(){
    }

    /**
     * 设置sql执行语句
     * @param sql
     * @author kliu
     * @date 2022/5/24 17:54
     */
    @SuppressWarnings("rawtypes")
    public void setSql(String sql){
        this.sql = sql;
        this.param = new ArrayList();
    }

    public void addBatch() {
        if (this.batchParaList == null) {
            this.batchParaList = new java.util.ArrayList();
        }

        if (this.param != null) {
            this.batchParaList.add(this.param.toArray());
            this.param = new java.util.ArrayList();
            this.batchFlag = true;
        }
    }

    /**
     * 设置sql语句中？变量值
     * @param index
     * @param value
     * @author kliu
     * @date 2022/5/24 17:54
     */
    @SuppressWarnings("unchecked")
    public void set(int index, Object value) throws RuntimeException {
        if(index > this.param.size()) {
            this.param.add(index - 1, value);
        } else {
            this.param.set(index - 1, value);
        }
    }

    /**
     * 执行数据查询
     * @author kliu
     * @date 2022/5/24 17:54
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List dbQuery(){
        return jdbcTemplate.queryForList(this.sql, this.param.toArray());
    }

    /**
     * 执行数据查询-带实体类
     * @author kliu
     * @date 2022/5/24 17:54
     */
    public List dbQuery(Class c){
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(c), this.param.toArray());
    }

    /**
     * 执行数据更新
     * @author kliu
     * @date 2022/5/24 17:55
     */
    public int dbUpdate(){
        int count = jdbcTemplate.update(this.sql, this.param.toArray());
        return count;
    }

    /**
     * 批量执行
     * @return int[]
     * @author kliu
     * @date 2022/8/5 14:08
     */
    public int[] dbBatchUpdate(){
        int[] count = jdbcTemplate.batchUpdate(this.sql, this.batchParaList);
        return count;
    }

    /**
     * 执行更新并返回id
     * @return long
     * @author kliu
     * @date 2022/5/24 17:55
     */
    public long dbUpdateAndReturnId(){
        // 必须要有keyHolder
        KeyHolder keyHolder = new GeneratedKeyHolder();
        // 改写如下
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                int i = 1;
                // 自增主键为null
                ps.setObject(i, null);
                for (Object p : param){
                    i++;
                    //用Object可以添加null参数
                    ps.setObject(i, p);
                }
                return ps;
            }
        }, keyHolder);
        // 返回主键id
        return Long.parseLong(Objects.requireNonNull(keyHolder.getKey()).toString());
    }

    /**
     * 分页查询数据
     * @param c
     * @param countSql
     * @param page
     * @param size
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/5/24 17:56
     */
    public PageBean dbQueryPage(Class c, String countSql, int page, int size) {
        if (page <= 0){
            throw new RuntimeException("当前页数必须大于等于1");
        }
        if (size <= 0){
            throw new RuntimeException("每页大小必须大于等于1");
        }
        //总共数量
        int totalSize = jdbcTemplate.queryForObject(countSql,this.param.toArray(),Integer.class);
        if (totalSize == 0){
            return PageBean.<Class>builder()
                    .contentList(new ArrayList<>())
                    .listTotalSize(0)
                    .currentpage(0)
                    .pageSize(0)
                    .totalPage(0)
                    .totalSize(0)
                    .build();
        }
        //总页数
        int totalPage = totalSize%size == 0 ? totalSize/size : totalSize/size + 1;
        //开始位置
        int offset = (page -1)*size;
        //return item size
        int limit =  size;
        this.sql = sql +" limit "+ limit +" offset "+offset;
        List content = jdbcTemplate.query(this.sql, new BeanPropertyRowMapper<>(c), this.param.toArray());;
        return PageBean.builder()
                .contentList(content)
                .listTotalSize(content.size())
                .totalSize(totalSize)
                .totalPage(totalPage)
                .currentpage(page)
                .pageSize(size)
                .build();
    }
}