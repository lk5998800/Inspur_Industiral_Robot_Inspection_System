package com.inspur.industrialinspection.service;

import com.inspur.industrialinspection.entity.UniappPhoto;
import org.springframework.web.multipart.MultipartFile;


/**
 * 图片上传
 * @author wangzhaodi
 * @date 2022/11/11 10:34
 */
public interface UniappPhotoService {
    /**
     * 添加
     * @param uniappPhoto
     * @param multipartFile
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/10 18:01
     */
    void add(UniappPhoto uniappPhoto, MultipartFile multipartFile) throws Exception;

    /**
     * 保存人像照片
     * @param multipartFile
     * @return void
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/10 18:02
     */
    String saveFrofile(MultipartFile multipartFile) throws Exception;
}
