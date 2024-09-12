package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.RoomInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 机房信息服务
 * @author kliu
 * @date 2022/6/7 16:10
 */
public interface RoomInfoService {
    /**
     * 获取机房信息
     * @param robotId
     * @param buildingId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/20 20:24
     */
    List list(long robotId, long buildingId);
    /**
     * 获取机房信息-dcim
     * @param parkId
     * @param paramObject
     * @return java.util.List
     * @author kliu
     * @date 2022/9/1 16:37
     */
    List getRoomInfos(int parkId, JSONObject paramObject);
    /**
     * 获取机房信息-不校验token 取所有
     * @param robotId
     * @param parkId
     * @return java.util.List
     * @author kliu
     * @date 2022/6/14 16:45
     */
    List listWithoutToken(long robotId, int parkId);
    /**
     * 添加机房信息
     * @param roomInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:46
     */
    void add(RoomInfo roomInfo);
    void add(RoomInfo roomInfo, MultipartFile file);
    /**
     * 更新机房信息
     * @param roomInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:46
     */
    void update(RoomInfo roomInfo);
    void update(RoomInfo roomInfo, MultipartFile file);
    /**
     * 删除机房信息
     * @param roomInfo
     * @return void
     * @author kliu
     * @date 2022/6/14 16:46
     */
    void delete(RoomInfo roomInfo);
    /**
     * 机房机器人用户信息列表
     * @param
     * @return java.util.List
     * @author kliu
     * @date 2022/6/14 16:46
     */
    List roomRobotUserList();


}
