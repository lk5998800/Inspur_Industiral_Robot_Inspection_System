package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.inspur.industrialinspection.dao.UniappPhotoDao;
import com.inspur.industrialinspection.entity.UniappPhoto;
import com.inspur.industrialinspection.service.UniappPhotoService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


/**
 * 图片上传
 * @author wangzhaodi
 * @date 2022/11/11 10:34
 */
@Service
public class UniappPhotoServiceImpl implements UniappPhotoService {
    @Autowired
    private UniappPhotoDao uniappPhotoDao;
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.endpoint}")
    private String minioBasic;


    private static final String UNIAPP_PHOTO_MINIO_BUCKET = "uniappphoto";


    /**
     * 添加
     *
     * @param uniappPhoto
     * @param file
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/10 18:05
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(UniappPhoto uniappPhoto, MultipartFile file) throws Exception {
        if(file == null){
            throw new RuntimeException("请选择图片上传");
        }
        uniappPhoto.setTime(DateUtil.now());
        long uniappPhotoId = uniappPhotoDao.addAndReturnId(uniappPhoto);
        uniappPhoto.setUniappId(uniappPhotoId);
        String imgUrl = saveFrofile(file);
        uniappPhoto.setImgUrl(imgUrl);
        uniappPhotoDao.update(uniappPhoto);
    }


    /**
     * 保存人像照片
     *
     * @param multipartFile
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/10 18:04
     */
    @Override
    public String saveFrofile(MultipartFile multipartFile) throws Exception {
        String imgType = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        InputStream in = multipartFile.getInputStream();
        String uuid = IdUtil.randomUUID();
        String fileName = uuid+imgType;
        minioClient.putObject(UNIAPP_PHOTO_MINIO_BUCKET, uuid+imgType, in, new PutObjectOptions(in.available(), -1));
        in.close();

        String imgUrl = minioBasic+"/"+UNIAPP_PHOTO_MINIO_BUCKET+"/"+fileName;

        return imgUrl;
    }


}
