package com.inspur.industrialinspection.dao;

import com.inspur.industrialinspection.entity.AlongWork;
import com.inspur.page.PageBean;

import java.util.List;
import java.util.Map;

/**
 * @author 
 * @description 
 * @date 
 */
public interface AlongWorkDao {
    /**
     * 分页查询
     * @param roomId
     * @param pageSize
     * @param page
     * @param status
     * @param taskTime
     * @param keyword
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/6/14 16:56
     */
    PageBean pageList(long roomId, int pageSize, int page,String status,String taskTime,String keyword);

    /**
     * 增加
     * @param alongWork
     * @return void
     * @author kliu
     * @date 2022/6/14 16:56
     */
    long addAndReturnId(AlongWork alongWork);
    /**
     * 修改
     * @param alongWork
     * @return void
     * @author kliu
     * @date 2022/6/14 16:56
     */
    void update(AlongWork alongWork);
    /**
     * 删除
     * @param id
     * @return void
     * @author kliu
     * @date 2022/6/14 16:56
     */
    void delete(long id);
    /**
     * 获取随工明细
     * @param id
     * @return com.inspur.industrialinspection.entity.AlongWork
     * @author kliu
     * @date 2022/6/14 16:57
     */
    AlongWork getDtlById(long id);
    /**
     * 获取随工明细
     * @param id
     * @return com.inspur.industrialinspection.entity.AlongWork
     * @author kliu
     * @date 2022/6/14 16:57
     */
    AlongWork getDtlByIdForUpdate(long id);

    /**
     * 获取列表
     * @param params
     * @return java.util.List<com.inspur.industrialinspection.entity.AlongWork>
     * @author kliu
     * @date 2022/6/14 16:57
     */
    List<AlongWork> getList(Map<String,Object> params);

    /**
     * 获取未执行的随工
     * @param
     * @return java.util.List<com.inspur.industrialinspection.entity.AlongWork>
     * @author kliu
     * @date 2022/6/14 16:57
     */
    List<AlongWork> getListByCron();
    /**
     * 获取正在运行的任务
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.AlongWork>
     * @author kliu
     * @date 2022/6/30 14:09
     */
    List<AlongWork> getRunningAlongWork(long roomId);

    /**
     * 更新视频url
     * @param id
     * @param videoUrl
     * @return void
     * @author kliu
     * @date 2022/7/4 20:19
     */
    void updateVideoUrl(long id, String videoUrl);

    /**
     * 获取图片列表
     * @param roomId
     * @return java.util.List<com.inspur.industrialinspection.entity.AlongWork>
     * @author kliu
     * @date 2022/9/24 10:25
     */
    List<AlongWork> getPictureList(long roomId, String startTime, String endTime, String taskName);

    /**
     * 依据机房id和日期计算数量
     * @param roomId
     * @param startDate
     * @return int
     * @author kliu
     * @date 2022/10/29 11:44
     */
    int countByRoomIdAndDate(long roomId, String startDate);

    /**
     * 依据机房id和日期获取列表，大于日期到当日
     * 带有开始时间的，正常人脸识别之后的
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.AlongWork>
     * @author kliu
     * @date 2022/10/31 8:46
     */
    List<AlongWork> listByRoomIdAndDate(long roomId, String dateStr);

    /**
     * 获取随工任务下发次数
     * @param roomId
     * @param startDate
     * @return int
     * @author kliu
     * @date 2022/10/29 11:44
     */
    int workIssueCountByRoomIdAndDate(long roomId, String startDate);

    /**
     * 获取没有开始时间的列表数据
     * @param roomId
     * @param dateStr
     * @return java.util.List<com.inspur.industrialinspection.entity.AlongWork>
     * @author kliu
     * @date 2022/11/5 11:31
     */
    List<AlongWork> listByRoomIdAndDateWithOutStartTime(long roomId, String dateStr);

    /**
     * 随工使用访客超期
     * @param roomId
     * @param dateStr
     * @return java.util.List
     * @author kliu
     * @date 2022/11/5 14:01
     */
    List listByRoomIdAndDateWithVisitorOverTime(long roomId, String dateStr);
}
