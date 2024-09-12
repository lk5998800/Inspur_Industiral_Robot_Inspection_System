package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import com.inspur.page.PageBean;

/**
 * 全局相册查询
 * @author kliu
 * @date 2022/9/24 10:10
 */
public interface PictureService {

    /**
     * 列表
     * @param roomId
     * @param taskType
     * @param startTime
     * @param endTime
     * @param taskName
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/9/24 10:21
     */
    JSONArray list(long roomId, String taskType, String startTime, String endTime, String taskName);
    /**
     * 照片明细
     * @param taskType
     * @param instanceId
     * @param pageSize
     * @param pageNum
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/9/26 15:17
     */
    PageBean detl(String taskType, long instanceId, int pageSize, int pageNum);

}
