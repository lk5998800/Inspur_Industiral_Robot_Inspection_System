package com.inspur.industrialinspection.dao.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.db.Db;
import com.inspur.industrialinspection.dao.ItAssetDao;
import com.inspur.industrialinspection.entity.ItAsset;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 资产
 * @author: kliu
 * @date: 2022/4/8 15:25
 */
@Repository
public class ItAssetDaoImpl implements ItAssetDao {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * 获取列表
     *
     * @param itAsset
     * @return java.util.List
     * @author kliu
     * @date 2022/7/25 13:48
     */
    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public PageBean list(ItAsset itAsset, int pageSize, int pageNum) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count ");
        stringBuffer.append("  from it_asset a, room_info c ");
        stringBuffer.append(" where a.room_id = c.room_id ");
        if (!StringUtils.isEmpty(itAsset.getAssetNo())){
            stringBuffer.append("   and a.asset_no like ? ");
        }
        if (!StringUtils.isEmpty(itAsset.getAssetName())){
            stringBuffer.append("   and a.asset_name like ? ");
        }

        if (itAsset.getRoomId()>0) {
            stringBuffer.append("   and a.room_id = ? ");
        }
        if (!StringUtils.isEmpty(itAsset.getCabinetRow())){
            stringBuffer.append("   and a.cabinet_row = ? ");
        }
        if (itAsset.getCabinetColumn()>0) {
            stringBuffer.append("   and a.cabinet_column = ? ");
        }
        if (itAsset.getPersonInChargeId()>0){
            stringBuffer.append("   and a.person_in_charge_id = ? ");
        }

        String countSqlStr = stringBuffer.toString();
        stringBuffer.setLength(0);
        stringBuffer.append("select a.id, a.asset_no, a.asset_name, a.room_id, brand, (select content from code_config where code ='BRAND' and a.brand = value) brand_content, ");
        stringBuffer.append("       model, (select content from code_config where code ='MODEL' and a.model = value) model_content, a.person_in_charge_id, a.asset_desc, a.u_bit, a.cabinet_row, ");
        stringBuffer.append("       a.cabinet_column, a.create_time, c.room_name, c.building_id, ");
        stringBuffer.append("       (select personnel_name from personnel_management where personnel_id = a.person_in_charge_id ) personnel_name, ");
        stringBuffer.append("       (select personnel_department from personnel_management where personnel_id = a.person_in_charge_id ) personnel_department ");
        stringBuffer.append("  from it_asset a, room_info c ");
        stringBuffer.append(" where a.room_id = c.room_id ");
        if (!StringUtils.isEmpty(itAsset.getAssetNo())){
            stringBuffer.append("   and a.asset_no like ? ");
        }
        if (!StringUtils.isEmpty(itAsset.getAssetName())){
            stringBuffer.append("   and a.asset_name like ? ");
        }

        if (itAsset.getRoomId()>0) {
            stringBuffer.append("   and a.room_id = ? ");
        }
        if (!StringUtils.isEmpty(itAsset.getCabinetRow())){
            stringBuffer.append("   and a.cabinet_row = ? ");
        }
        if (itAsset.getCabinetColumn()>0) {
            stringBuffer.append("   and a.cabinet_column = ? ");
        }
        if (itAsset.getPersonInChargeId()>0){
            stringBuffer.append("   and a.person_in_charge_id = ? ");
        }
        stringBuffer.append(" order by create_time desc ");

        int index = 1;
        db.setSql(stringBuffer.toString());

        if (!StringUtils.isEmpty(itAsset.getAssetNo())){
            db.set(index++, "%"+itAsset.getAssetNo()+"%");
        }
        if (!StringUtils.isEmpty(itAsset.getAssetName())){
            db.set(index++, "%"+itAsset.getAssetName()+"%");
        }

        if (itAsset.getRoomId()>0) {
            db.set(index++, itAsset.getRoomId());
        }
        if (!StringUtils.isEmpty(itAsset.getCabinetRow())){
            db.set(index++, itAsset.getCabinetRow());
        }
        if (itAsset.getCabinetColumn()>0) {
            db.set(index++, itAsset.getCabinetColumn());
        }
        if (itAsset.getPersonInChargeId()>0){
            db.set(index++, itAsset.getPersonInChargeId());
        }

        PageBean objectPageBean = db.dbQueryPage(ItAsset.class, countSqlStr, pageNum, pageSize);
        return objectPageBean;
    }

    /**
     * 添加
     *
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    public void add(ItAsset itAsset) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("insert into industrial_robot.it_asset ");
        stringBuffer.append("  (asset_no, asset_name, room_id, brand, model, ");
        stringBuffer.append("   person_in_charge_id, asset_desc, u_bit, cabinet_row, cabinet_column, ");
        stringBuffer.append("   create_time, point_name) ");
        stringBuffer.append("values ");
        stringBuffer.append("  (?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?, ?, ?, ?, ");
        stringBuffer.append("   ?, ?)");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAsset.getAssetNo());
        db.set(index++, itAsset.getAssetName());
        db.set(index++, itAsset.getRoomId());
        db.set(index++, itAsset.getBrand());
        db.set(index++, itAsset.getModel());

        db.set(index++, itAsset.getPersonInChargeId());
        db.set(index++, itAsset.getAssetDesc());
        db.set(index++, itAsset.getUBit());
        db.set(index++, itAsset.getCabinetRow());
        db.set(index++, itAsset.getCabinetColumn());

        db.set(index++, DateUtil.now());
        db.set(index++, itAsset.getPointName());

        db.dbUpdate();

    }

    /**
     * 更新
     *
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    public void update(ItAsset itAsset) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("update it_asset ");
        stringBuffer.append("   set asset_no = ?, asset_name = ?, room_id = ?, brand = ?, model = ?, ");
        stringBuffer.append("       person_in_charge_id = ?, asset_desc  = ?, u_bit = ?, cabinet_row = ?, cabinet_column  = ?, point_name = ? ");
        stringBuffer.append(" where id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAsset.getAssetNo());
        db.set(index++, itAsset.getAssetName());
        db.set(index++, itAsset.getRoomId());
        db.set(index++, itAsset.getBrand());
        db.set(index++, itAsset.getModel());

        db.set(index++, itAsset.getPersonInChargeId());
        db.set(index++, itAsset.getAssetDesc());
        db.set(index++, itAsset.getUBit());
        db.set(index++, itAsset.getCabinetRow());
        db.set(index++, itAsset.getCabinetColumn());
        db.set(index++, itAsset.getPointName());

        db.set(index++, itAsset.getId());

        db.dbUpdate();
    }

    /**
     * 删除
     *
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    public void delete(ItAsset itAsset) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("delete from it_asset ");
        stringBuffer.append(" where id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, itAsset.getId());
        db.dbUpdate();
    }

    /**
     * 根据资产编号判断数据是否存在
     * @param assetNo
     * @return boolean
     * @author kliu
     * @date 2022/7/30 9:04
     */
    @Override
    public boolean checkExistByAssetNo(String assetNo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select 1 from it_asset where asset_no = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, assetNo);
        return db.dbQuery().size()>0;
    }

    @Override
    public ItAsset getDetlById(long id) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from it_asset where id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, id);
        List<ItAsset> list = db.dbQuery(ItAsset.class);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public ItAsset getDetlByAssetNo(String assetNo) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select * from it_asset where asset_no = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, assetNo);
        List<ItAsset> list = db.dbQuery(ItAsset.class);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public int countByRoomId(long roomId) {
        Db db = beanFactory.getBean(Db.class);
        StringBuffer stringBuffer=new StringBuffer();
        stringBuffer.append("select count(1) count from it_asset where room_id = ?");
        int index = 1;
        db.setSql(stringBuffer.toString());
        db.set(index++, roomId);
        List<Map> list = db.dbQuery();
        return Integer.parseInt(list.get(0).get("count")+"");
    }
}
