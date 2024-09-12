package com.inspur.industrialinspection.dao;


import com.inspur.industrialinspection.entity.UniappPhoto;

import java.util.List;

/**
 * 图片上传
 * @author wangzhaodi
 * @date 2022/11/10 17:55
 */
public interface UniappPhotoDao {
    /**
     * 添加
     * @param uniappPhoto
     * @return long
     * @throws Exception
     * @author wangzhaodi
     * @date 2022/11/10 17:56
     */
    long addAndReturnId(UniappPhoto uniappPhoto) throws Exception;

    /**
     * 更新
     * @param uniappPhoto
     * @return void
     * @author wangzhaodi
     * @date 2022/11/11 15:28
     */
    void update(UniappPhoto uniappPhoto);

    /**
     * 依据任务id获取拍摄图片数据
     * @param taskInspectId
     * @return java.util.List
     * @author kliu
     * @date 2022/11/21 18:01
     */
    List list(long taskInspectId);

}
