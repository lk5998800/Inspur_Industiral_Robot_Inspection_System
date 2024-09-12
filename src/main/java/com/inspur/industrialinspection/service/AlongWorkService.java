package com.inspur.industrialinspection.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.inspur.industrialinspection.entity.AlongWork;
import com.inspur.industrialinspection.entity.PointInfo;
import com.inspur.industrialinspection.entity.RoomInfo;
import com.inspur.page.PageBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 随工管理服务
 * @author kliu
 * @date 2022/6/13 18:25
 */
public interface AlongWorkService {
    /**
     * 设置私有变量的值
     * @param key
     * @param value
     * @return void
     * @author kliu
     * @date 2022/6/28 13:46
     */
    void setAlongWorkMap(Long key, Boolean value);
    /**
     * 分页查询
     * @param roomId
     * @param pageSize
     * @param page
     * @param status
     * @param taskTime
     * @param keyword
     * @return com.inspur.page.PageBean
     * @throws IOException
     * @author kliu
     * @date 2022/6/14 16:55
     */
    PageBean pageList(long roomId, int pageSize, int page,String status,String taskTime,String keyword) throws IOException;
    /**
     * 增加或更新
     * @param alongWork
     * @return void
     * @author kliu
     * @date 2022/6/13 18:25
     */
    void addOrUpdate(AlongWork alongWork);
    /**
     * 删除
     * @param id
     * @return void
     * @author kliu
     * @date 2022/6/13 18:25
     */
    void delete(int id);
    /**
     * 开始任务
     * @param id
     * @return void
     * @throws IOException
     * @author kliu
     * @date 2022/8/30 13:34
     */
    void startTask(long id) throws IOException;
    /**
     * 结束任务
     * @param id
     * @return void
     * @author kliu
     * @date 2022/6/13 18:26
     */
    void endTask(long id);

    /**
     * 接收随工执行记录
     * @param alongWorkDetlJson
     * @return void
     * @author kliu
     * @date 2022/6/14 16:32
     */
    void receiveAlongWorkDetl(String alongWorkDetlJson);
    /**
     * 行人检测告警信息接收
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/27 19:46
     */
    void pedestrianDetectionAlarmInformationSave(String json);

    /**
     * 获取正在执行的随工任务
     * @param roomId
     * @return List
     * @author kliu
     * @date 2022/6/29 12:15
     */
    Map getRunningAlongWork(long roomId);

    /**
     * 获取随工任务明细
     * @param id
     * @return Map
     * @author kliu
     * @date 2022/6/30 16:08
     */
    Map getAlongWorkDetl(long id);
    /**
     * 获取随工任务明细
     * @param id
     * @return com.inspur.industrialinspection.entity.AlongWork
     * @author kliu
     * @date 2022/7/11 15:07
     */
    JSONObject getAlongWorkDetlForRobot(long id);

    /**
     * 正常结束任务
     * @param id
     * @param endTime
     * @param reason
     * @return void
     * @author kliu
     * @date 2022/7/13 14:23
     */
    void normalEndTask(long id, String endTime, String reason);

    /**
     * 获取随工点位
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/7/12 19:21
     */
    List getAlongWorkPointInfos(long roomId);

    /**
     * 初始化随工点
     * @param roomInfo
     * @return void
     * @author kliu
     * @date 2022/8/5 11:17
     */
    void initAlongWorkPoint(RoomInfo roomInfo) throws InterruptedException;

    /**
     * 接收随工点
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/8/5 13:51
     */
    void receiveAlongWorkPoints(JSONObject jsonObject);

    /**
     * 关联随工待命点
     * @param roomId
     * @return void
     * @throws InterruptedException
     * @author kliu
     * @date 2022/8/29 10:24
     */
    void associatedWaitPoint(long roomId) throws InterruptedException;
    /**
     * 关联随工点
     * @param pointInfo
     * @return void
     * @author kliu
     * @date 2022/8/26 11:00
     */
    void associatedAlongWorkPoint(PointInfo pointInfo) throws InterruptedException;

    /**
     * 获取机房下的随工点位-包含未关联的随工点
     * @param roomId
     * @return cn.hutool.json.JSONArray
     * @author kliu
     * @date 2022/10/18 14:14
     */
    JSONArray getAlongWorkPointInfosIncludeAll(long roomId);
}
