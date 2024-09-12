package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 机柜图片服务
 * @author kliu
 * @date 2022/5/9 17:26
 */
public interface CabinetPicService {
    /**
     * 创建图片文件夹
     * @param roomId
     * @param response
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/13 14:18
     */
    void createPicFile(long roomId, HttpServletResponse response) throws Exception;
    /**
     * 创建图片文件夹-带有红色指示灯的
     * @param roomId
     * @param response
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/13 14:18
     */
    void createPicFileWithRedlight(long roomId, HttpServletResponse response) throws Exception;

    /**
     * 获取机柜拍摄照片
     * @param parkId
     * @param paramObject
     * @return java.util.List
     * @author kliu
     * @date 2022/9/2 9:59
     */
    List getPicInfos(int parkId, JSONObject paramObject);
    /**
     * 获取机柜拍摄照片-异常
     * @param parkId
     * @param paramObject
     * @return java.util.List
     * @author kliu
     * @date 2022/9/2 9:59
     */
    List getAbnormalPicInfos(int parkId, JSONObject paramObject);

    /**
     * 创建红外图片文件夹
     * @param roomId
     * @param response
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/13 14:18
     */
    void createInfraredPicFile(long roomId, HttpServletResponse response) throws Exception;
    /**
     * 创建红外图片文件夹-转换后的
     * @param roomId
     * @param response
     * @return void
     * @throws Exception
     * @author kliu
     * @date 2022/7/13 14:18
     */
    void createInfraredPicFileTranslate(long roomId, HttpServletResponse response) throws Exception;
}
