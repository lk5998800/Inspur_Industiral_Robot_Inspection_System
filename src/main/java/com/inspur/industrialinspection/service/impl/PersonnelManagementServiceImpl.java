package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.PersonnelManagementDao;
import com.inspur.industrialinspection.entity.PersonnelManagement;
import com.inspur.industrialinspection.service.AiAgentService;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.PersonnelManagementService;
import com.inspur.page.PageBean;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 用户管理
 * @author kliu
 * @date 2022/6/7 16:10
 */
@Service
public class PersonnelManagementServiceImpl implements PersonnelManagementService {
    @Autowired
    private PersonnelManagementDao personnelManagementDao;
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.endpoint}")
    private String minioBasic;
    @Autowired
    private AiAgentService aiAgentService;
    @Value("${aiagent.service.url}")
    private String aiagentUrl;
    @Value("${aiagent.service.faceverifyfeature.url}")
    private String faceverifyfeatureUrl;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private CommonService commonService;

    private static final String PERSONNEL_FACE_PROFILE_MINIO_BUCKET = "personnelfaceprofile";


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
        PageBean pageBean = personnelManagementDao.pageList(personnelManagement, pageSize, pageNum);
        List<PersonnelManagement> personnelManagements = pageBean.getContentList();
        for (PersonnelManagement management : personnelManagements) {
            management.setPersonnelUrl(commonService.url2Https(management.getPersonnelUrl()));
        }
        return pageBean;
    }

    /**
     * 添加
     *
     * @param personnelManagement
     * @param file
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(PersonnelManagement personnelManagement, MultipartFile file) throws Exception {
        long personnelId = personnelManagementDao.addAndReturnId(personnelManagement);
        saveFrofile(file, personnelId);
    }

    /**
     * 更新
     *
     * @param personnelManagement
     * @param file
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PersonnelManagement personnelManagement, MultipartFile file) throws Exception {
        personnelManagementDao.update(personnelManagement);
        if (file != null){
            saveFrofile(file, personnelManagement.getPersonnelId());
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void delete(PersonnelManagement personnelManagement) {
        personnelManagementDao.delete(personnelManagement);
    }

    /**
     * 批量删除
     *
     * @param jsonArray
     * @return void
     * @author kliu
     * @date 2022/7/21 18:01
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(JSONArray jsonArray) {
        if (jsonArray.size() == 0) {
            throw new RuntimeException("请选择要删除的数据");
        }
        String inPara = "";
        Long personnelId;
        for (int i = 0; i < jsonArray.size(); i++) {
            personnelId = jsonArray.getLong(i);
            if (StringUtils.isEmpty(inPara)){
                inPara+=personnelId;
            }else{
                inPara+=","+personnelId;
            }
        }

        personnelManagementDao.batchDelete(inPara);
    }

    /**
     * 保存人像照片
     *
     * @param multipartFile
     * @param personnelId
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/21 18:02
     */
    @Override
    public void saveFrofile(MultipartFile multipartFile, long personnelId) throws Exception {
        String imgType = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        InputStream in = multipartFile.getInputStream();
        String fileName = personnelId+imgType;
        minioClient.putObject(PERSONNEL_FACE_PROFILE_MINIO_BUCKET, personnelId+imgType, in, new PutObjectOptions(in.available(), -1));
        in.close();

        String faceprofileUrl = minioBasic+"/"+PERSONNEL_FACE_PROFILE_MINIO_BUCKET+"/"+fileName;
        //调用ai获取人脸特征值
        String url = aiagentUrl+faceverifyfeatureUrl;
        JSONObject serviceObject = new JSONObject();
        serviceObject.set("image_url", new String[]{faceprofileUrl});
        JSONObject serviceResult = aiAgentService.invokeHttp(url, serviceObject.toString());
        Object facialFeature = serviceResult.get("face_feature");
        //做一层转化存到数据库中
        JSONObject json = new JSONObject();
        json.set("face_feature", facialFeature);

        PersonnelManagement personnelManagement = new PersonnelManagement();
        personnelManagement.setPersonnelId(personnelId);
        personnelManagement.setPersonnelUrl(faceprofileUrl);
        personnelManagement.setPersonnelFacialFeature(json.toString());
        personnelManagementDao.saveFaceProfileUrlAndFacialFeature(personnelManagement);
    }

    /**
     * 获取人员信息概览
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/7/22 16:27
     */
    @Override
    public JSONObject getOverView() {
        long allCount = 0;
        long visitorCount = 0;
        long blacklistCount = 0;
        long normalCount = 0;
        long vipCount = 0;
        List<Map> list = personnelManagementDao.countByType();
        for (Map map : list) {
            if ("normal".equals(map.get("personnel_type").toString())){
                normalCount = (long) map.get("count");
            }else if ("visitor".equals(map.get("personnel_type").toString())){
                visitorCount = (long) map.get("count");
            }else if ("vip".equals(map.get("personnel_type").toString())){
                vipCount = (long) map.get("count");
            }else if ("blacklist".equals(map.get("personnel_type").toString())){
                blacklistCount = (long) map.get("count");
            }
        }

        allCount = visitorCount+blacklistCount+normalCount+vipCount;


        String day6Before = DateUtil.format(DateUtil.offsetDay(DateUtil.date(),-6), "yyyy-MM-dd");
        list = personnelManagementDao.visitorCountByDate(day6Before);
        if (list.size() == 0){
            visitorCount = 0;
        }else{
            visitorCount = (long) list.get(0).get("count");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.set("allCount", allCount);
        jsonObject.set("blacklistCount", blacklistCount);
        jsonObject.set("visitorCount", visitorCount);

        return jsonObject;
    }

    @Override
    public List getDistinctPersonnelDepartment() {
        return personnelManagementDao.getDistinctPersonnelDepartment();
    }
}
