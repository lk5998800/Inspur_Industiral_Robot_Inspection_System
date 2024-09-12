package com.inspur.industrialinspection.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.CodeConfigDao;
import com.inspur.industrialinspection.dao.ItAssetDao;
import com.inspur.industrialinspection.entity.CodeConfig;
import com.inspur.industrialinspection.entity.ItAsset;
import com.inspur.industrialinspection.service.ItAssetService;
import com.inspur.page.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 资产
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Service
public class ItAssetServiceImpl implements ItAssetService {

    @Autowired
    private ItAssetDao itAssetDao;

    @Autowired
    private CodeConfigDao codeConfigDao;

    /**
     * 获取列表
     *
     * @param itAsset
     * @return java.util.List
     * @author kliu
     * @date 2022/7/25 13:48
     */
    @Override
    public PageBean list(ItAsset itAsset, int pageSize, int pageNum) {
        return itAssetDao.list(itAsset, pageSize, pageNum);
    }

    /**
     * 添加
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ItAsset itAsset) {

        String uBit = itAsset.getUBit();
        if (!StringUtils.isEmpty(uBit)){
            String[] split = uBit.split("-");
            if (split.length != 2) {
                throw new RuntimeException("请填入正确的u位信息");
            }

            if (split[0].compareTo(split[1]) >0) {
                throw new RuntimeException("起始u位应小于终止u位");
            }
        }


        String assetNo = itAsset.getAssetNo();
        if (itAssetDao.checkExistByAssetNo(assetNo)){
            throw new RuntimeException("已存在资产编码为【"+assetNo+"】的资产，请填写其他编码");
        }
        String pointName = "";
        String cabinetRow = itAsset.getCabinetRow();
        long cabinetColumn = itAsset.getCabinetColumn();
        //noinspection AlibabaUndefineMagicConstant
        if (cabinetColumn<=9){
            pointName = cabinetRow+"0"+cabinetColumn;
        }else{
            pointName = cabinetRow+""+cabinetColumn;
        }
        itAsset.setPointName(pointName);
        itAssetDao.add(itAsset);
    }

    /**
     * 更新
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ItAsset itAsset) {

        String uBit = itAsset.getUBit();
        if (!StringUtils.isEmpty(uBit)){
            String[] split = uBit.split("-");
            if (split.length != 2) {
                throw new RuntimeException("请填入正确的u位信息");
            }

            if (split[0].compareTo(split[1]) >0) {
                throw new RuntimeException("起始u位应小于终止u位");
            }
        }
        long id = itAsset.getId();
        String assetNo = itAsset.getAssetNo();
        ItAsset dbItAsset = itAssetDao.getDetlById(id);
        if (dbItAsset==null) {
            throw new RuntimeException("要修改的数据不存在，请检查传入的id");
        }

        String dbAssetNo = dbItAsset.getAssetNo();
        if (!assetNo.equals(dbAssetNo)){
            dbItAsset = itAssetDao.getDetlByAssetNo(assetNo);
            if (dbItAsset != null){
                throw new RuntimeException("已存在资产编码为【"+assetNo+"】的资产，请填写其他编码");
            }
        }

        String pointName = "";
        String cabinetRow = itAsset.getCabinetRow();
        long cabinetColumn = itAsset.getCabinetColumn();
        //noinspection AlibabaUndefineMagicConstant
        if (cabinetColumn<=9){
            pointName = cabinetRow+"0"+cabinetColumn;
        }else{
            pointName = cabinetRow+""+cabinetColumn;
        }
        itAsset.setPointName(pointName);

        itAssetDao.update(itAsset);
    }

    /**
     * 删除
     * @param itAsset
     * @return void
     * @author kliu
     * @date 2022/7/25 13:49
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ItAsset itAsset) {
        ItAsset dbItAsset = itAssetDao.getDetlById(itAsset.getId());
        if (dbItAsset==null) {
            throw new RuntimeException("要删除的数据不存在，请检查传入的id");
        }
        itAssetDao.delete(itAsset);
    }

    @Override
    public JSONArray getItAssetCode() {
        JSONArray jsonArray = new JSONArray();
        JSONArray codeArr;
        JSONObject codeObject;
        JSONObject valueObject;
        List<CodeConfig> list = codeConfigDao.list();
        for (CodeConfig codeConfig : list) {
            String code = codeConfig.getCode();
            String value = codeConfig.getValue();
            String content = codeConfig.getContent();
            boolean exists = false;
            for (int i = 0; i < jsonArray.size(); i++) {
                codeObject = jsonArray.getJSONObject(i);
                codeArr = codeObject.getJSONArray("codeArr");
                String cacheCode = codeObject.getStr("code");
                if (cacheCode.equals(code)){
                    exists = true;
                    valueObject = new JSONObject();
                    valueObject.set("value", value);
                    valueObject.set("content", content);
                    codeArr.add(valueObject);
                    break;
                }
            }

            if (!exists){
                codeArr = new JSONArray();
                valueObject = new JSONObject();
                valueObject.set("value", value);
                valueObject.set("content", content);
                codeArr.add(valueObject);

                codeObject = new JSONObject();
                codeObject.set("code", code);
                codeObject.set("codeArr", codeArr);
                jsonArray.add(codeObject);
            }

        }
        return jsonArray;
    }
}
