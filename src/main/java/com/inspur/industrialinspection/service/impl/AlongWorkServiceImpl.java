package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.code.AlongWorkStatus;
import com.inspur.cron.AlongWorkExecuteCron;
import com.inspur.industrialinspection.dao.*;
import com.inspur.industrialinspection.entity.*;
import com.inspur.industrialinspection.service.AlongWorkService;
import com.inspur.industrialinspection.service.CommonService;
import com.inspur.industrialinspection.service.PointInfoService;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.mqtt.MqttPushClient;
import com.inspur.page.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 随工任务服务实现
 * @author kliu
 * @date 2022/6/13 18:26
 */
@Service
@Slf4j
public class AlongWorkServiceImpl implements AlongWorkService {

    private volatile static ConcurrentHashMap<Long, Boolean> alongWorkMap = new ConcurrentHashMap<Long, Boolean>();
    private volatile static ConcurrentHashMap<Long, Boolean> roomInitAlongPoint = new ConcurrentHashMap<Long, Boolean>();

    @Autowired
    private AlongWorkDao alongWorkDao;
    @Autowired
    private AlongWorkDtlDao alongWorkDtlDao;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private MqttPushClient mqttPushClient;
    @Autowired
    private AlongWorkPedestrianDetectionAlarmDao alongWorkPedestrianDetectionAlarmDao;
    @Autowired
    private RequestService requestService;
    @Autowired
    private AlongWorkExecuteCron alongWorkExecuteCron;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Value("${composite.image.url}")
    private String compositeImageUrl;
    @Autowired
    private CommonService commonService;
    @Autowired
    private RoomInfoDao roomInfoDao;
    @Autowired
    private PointInfoDao pointInfoDao;
    @Autowired
    private AlongWorkPointInfoDao alongWorkPointInfoDao;
    @Autowired
    private PointInfoService pointInfoService;
    @Autowired
    private PersonnelManagementDao personnelManagementDao;

    @Override
    public void setAlongWorkMap(Long key, Boolean value){
        alongWorkMap.put(key, value);
    }


    /**
     * 分页查询随工列表
     * @param roomId
     * @param pageSize
     * @param page
     * @param status
     * @param taskTime
     * @param keyword
     * @return com.inspur.page.PageBean
     * @author kliu
     * @date 2022/6/13 18:26
     */
    @Override
    public PageBean pageList(long roomId, int pageSize, int page,String status,String taskTime,String keyword) throws IOException {
        if (!StringUtils.isEmpty(taskTime)){
            taskTime = taskTime.substring(0, 10);
        }
        PageBean pageBean = alongWorkDao.pageList(roomId, pageSize, page,status,taskTime,keyword);
        return pageBean;
    }

    /**
     * 增加或更新随工任务
     * @param alongWork
     * @return void
     * @author kliu
     * @date 2022/6/13 18:27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdate(AlongWork alongWork) {
        long userId = requestService.getUserIdByToken();
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            long id = alongWork.getId();
            String nowExecute = "2";
            if (nowExecute.equals(alongWork.getTaskType())){
                alongWork.setTaskTime(DateUtil.now());
            }
            if (id > 0){
                alongWorkDao.update(alongWork);
            } else {
                alongWork.setCreateTime(DateUtil.now());
                alongWork.setCreateUserId(userId);
                alongWork.setStatus(AlongWorkStatus.NO);
                id = alongWorkDao.addAndReturnId(alongWork);
            }
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;

            if (nowExecute.equals(alongWork.getTaskType())){
                alongWork = alongWorkDao.getDtlById(id);
                if (AlongWorkStatus.NO.equals(alongWork.getStatus())) {
                    //下发任务
                    alongWorkExecuteCron.execute(alongWork);
                }
            }
        } catch (Exception e) {
            if (transactionStatus!=null){
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 删除随工任务
     * @param id
     * @return void
     * @author kliu
     * @date 2022/6/13 18:27
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(int id) {
        if (id <= 0){
            throw new RuntimeException("传入id不能为空，请检查传入的数据");
        }

        AlongWork alongWork = alongWorkDao.getDtlById(id);
        if (!AlongWorkStatus.NO.equals(alongWork.getStatus())){
            throw new RuntimeException("只能删除状态为新建的随工任务");
        }
        alongWorkDao.delete(id);
    }

    /**
     * 开始任务
     * @param id
     * @return void
     * @author kliu
     * @date 2022/6/13 18:28
     */
    @Override
    public void startTask(long id) throws IOException {
        AlongWork alongWork = alongWorkDao.getDtlById(id);
        if (AlongWorkStatus.NO.equals(alongWork.getStatus())) {
            //下发任务
            alongWorkExecuteCron.execute(alongWork);
        } else {
            throw new RuntimeException("当前任务已开始，不支持重复下发任务");
        }
    }

    /**
     * 结束任务
     * @param id
     * @return void
     * @author kliu
     * @date 2022/6/13 18:28
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endTask(long id) {
        AlongWork alongWork = alongWorkDao.getDtlById(id);
        long roomId = alongWork.getRoomId();
        if (AlongWorkStatus.WAIT.equals(alongWork.getStatus())||AlongWorkStatus.RUNNING.equals(alongWork.getStatus())) {
            //将终止随工任务，告诉机器人端
            JSONObject issuedJsonObject = new JSONObject();
            issuedJsonObject.set("taskId", id);
            //1   普通巡检    2   随工任务
            issuedJsonObject.set("type", 2);
            mqttPushClient.publish("industrial_robot_terminate/"+robotRoomDao.getRobotIdByRoomId(roomId),issuedJsonObject.toString());
            alongWorkMap.put(id, false);

            int i=0;
            int terminateWaitCount = 50;
            while (i<terminateWaitCount){
                if (alongWorkMap.get(id)) {
                    alongWorkMap.remove(id);
                    alongWork.setStatus(AlongWorkStatus.END);
                    alongWork.setEndTime(DateUtil.now());
                    alongWork.setReason("手动终止任务");
                    alongWorkDao.update(alongWork);
                    break;
                }else{
                    i++;
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        } else {
            throw new RuntimeException("当前任务未在执行");
        }
    }

    /**
     * 接收随工执行记录
     * @param alongWorkDetlJson
     * @return void
     * @author kliu
     * @date 2022/6/14 16:32
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveAlongWorkDetl(String alongWorkDetlJson) {
        JSONObject dataObject = JSONUtil.parseObj(alongWorkDetlJson);

        long taskId = dataObject.getLong("task_id");
        long robotId = dataObject.getLong("robot_id");
        String pointName = dataObject.getStr("point_id");
        String timestamp = dataObject.getStr("time");
        String uuid = dataObject.getStr("uuid");
        String type = dataObject.getStr("type");
        String url = dataObject.getStr("url");
        String pointNameType = "point_name";
        if (!pointNameType.equals(type)){
            throw new RuntimeException("云端仅能接受type=point_name的type");
        }

        String pointNameWaitStartCh = "到达待命点";
        String pointNameEndCh = "随工任务结束";

        if(pointNameWaitStartCh.equals(pointName)){
            pointName = "ADMDS";
        }else if(pointNameEndCh.equals(pointName)){
            pointName = "ADMDE";
        }

        String alongWorkStartPointName = "ADMDS";
        //随工开始节点
        if(alongWorkStartPointName.equals(pointName)){
            AlongWorkDtl alongWorkDtl = alongWorkDtlDao.getDetlById(taskId, pointName);
            //开始节点url不为空，证明为人脸认证成功
            if (!StringUtils.isEmpty(url)){
                if (alongWorkDtl != null){
                    //人脸识别成功，仅保留人脸
                    String inspectionTime = alongWorkDtl.getInspectionTime();
                    if (!StringUtils.isEmpty(inspectionTime)){
                        timestamp = alongWorkDtl.getInspectionTime();
                    }
                }
                //此时任务开始，更改状态为running，并更新开始时间
                AlongWork alongWork = alongWorkDao.getDtlById(taskId);
                alongWork.setStatus(AlongWorkStatus.RUNNING);
                alongWork.setStartTime(DateUtil.now());
                alongWorkDao.update(alongWork);
            }else{
                //为空证明是到达待命点，此处添加url是防止robotmanage 缓存数据重新上传导致错位
                if(alongWorkDtl != null){
                    url = alongWorkDtl.getImgUrl();
                }
            }
        }

        AlongWorkDtl alongWorkDtl = new AlongWorkDtl();
        alongWorkDtl.setPid(taskId);
        alongWorkDtl.setPointName(pointName);
        alongWorkDtl.setInspectionTime(timestamp);
        alongWorkDtl.setImgUrl(url);

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

        try {
            if (alongWorkDtlDao.checkExist(alongWorkDtl)){
                alongWorkDtlDao.update(alongWorkDtl);
            }else{
                alongWorkDtlDao.add(alongWorkDtl);
            }
            dataSourceTransactionManager.commit(transactionStatus);
            //发送数据保存成功标志
            mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
        }catch (Exception e){
            log.error("接收随工明细异常："+e.getMessage());
            dataSourceTransactionManager.rollback(transactionStatus);
            throw new RuntimeException(e.getMessage());
        }
    }
    /**
     * 行人检测告警信息接收
     * @param json
     * @return void
     * @author kliu
     * @date 2022/6/27 19:46
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pedestrianDetectionAlarmInformationSave(String json) {
        JSONObject dataObject = JSONUtil.parseObj(json);

        long taskId = dataObject.getLong("task_id");
        long robotId = dataObject.getLong("robot_id");
        String pointName = dataObject.getStr("point_id","");
        String timestamp = dataObject.getStr("time");
        String uuid = dataObject.getStr("uuid");
        String alarmInformation = dataObject.getStr("alarm_information");
        String url = dataObject.getStr("url");
        AlongWorkPedestrianDetectionAlarm alongWorkPedestrianDetectionAlarm = new AlongWorkPedestrianDetectionAlarm();
        alongWorkPedestrianDetectionAlarm.setPid(taskId);
        alongWorkPedestrianDetectionAlarm.setPointName(pointName);
        alongWorkPedestrianDetectionAlarm.setAlarmTime(timestamp);
        alongWorkPedestrianDetectionAlarm.setAlarmInformation(alarmInformation);
        alongWorkPedestrianDetectionAlarm.setUrl(url);

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

        try {
            if (alongWorkPedestrianDetectionAlarmDao.checkExist(alongWorkPedestrianDetectionAlarm)){
                alongWorkPedestrianDetectionAlarmDao.update(alongWorkPedestrianDetectionAlarm);
            }else{
                alongWorkPedestrianDetectionAlarmDao.add(alongWorkPedestrianDetectionAlarm);
            }
            dataSourceTransactionManager.commit(transactionStatus);
            //发送数据保存成功标志
            mqttPushClient.publish("industrial_robot_detection_receve_success/"+robotId, "{\"uuid\": \""+uuid+"\"}");
        }catch (Exception e){
            log.error("接收随工明细异常："+e.getMessage());
            dataSourceTransactionManager.rollback(transactionStatus);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 获取正在执行的随工任务
     * @param roomId
     * @return List
     * @author kliu
     * @date 2022/6/29 12:15
     */
    @Override
    public Map getRunningAlongWork(long roomId) {
        List<AlongWork> runningAlongWork = alongWorkDao.getRunningAlongWork(roomId);
        if (runningAlongWork.isEmpty()){
            return null;
        }
        AlongWork alongWork = runningAlongWork.get(0);
        List alongWorkList = getAlongWorkDetl(alongWork);
        Map returnMap = new HashMap(2);
        returnMap.put("alongWorkList", alongWorkList);
        returnMap.put("videoUrl", "-");
        returnMap.put("roomId", alongWork.getRoomId());
        return returnMap;
    }

    /**
     * 获取随工执行明细
     * @param alongWork
     * @return java.util.List
     * @author kliu
     * @date 2022/6/30 16:18
     */
    private List getAlongWorkDetl(AlongWork alongWork){
        long id = alongWork.getId();
        long createUserId = alongWork.getCreateUserId();
        String points = alongWork.getPoints();
        String createTime = alongWork.getCreateTime();
        String userName = personnelManagementDao.getDetlById(createUserId).getPersonnelName();
        List<AlongWorkDtl> list = alongWorkDtlDao.list(id);

        String[] pointArr = points.split(",");

        List returnList = new ArrayList<>();
        Map<String, String> map = new HashMap<>(4);
        map.put("type", "create");
        map.put("time", createTime);
        map.put("desc", userName+"建立任务");
        returnList.add(map);

        for (AlongWorkDtl alongWorkDtl : list) {
            if ("ADMDS".equals(alongWorkDtl.getPointName())){
                map = new HashMap<>(4);
                map.put("type", "wait_point");
                map.put("time", alongWorkDtl.getInspectionTime());
                map.put("desc", "机器人到达待命点");
                map.put("imgUrl", commonService.url2Https(alongWorkDtl.getImgUrl()));
                returnList.add(map);
                break;
            }
        }

        for (String pointName : pointArr) {
            boolean alongWorkDtlExistData = false;
            for (AlongWorkDtl alongWorkDtl : list) {
                if (alongWorkDtl.getPointName().equals(pointName)){
                    alongWorkDtlExistData = true;
                    map = new HashMap<>(4);
                    map.put("type", "along_work_point");
                    map.put("time", alongWorkDtl.getInspectionTime());
                    map.put("desc", "随工点"+pointName);
                    map.put("imgUrl", commonService.url2Https(alongWorkDtl.getImgUrl()));
                    returnList.add(map);
                    break;
                }
            }

            if (!alongWorkDtlExistData){
                map = new HashMap<>(4);
                map.put("type", "along_work_point");
                map.put("time", "");
                map.put("desc", "随工点"+pointName);
                map.put("imgUrl", "");
                returnList.add(map);
            }
        }

        return returnList;
    }

    /**
     * 获取随工任务明细
     * @param id
     * @return Map
     * @author kliu
     * @date 2022/6/30 16:08
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map getAlongWorkDetl(long id) {
        AlongWork alongWork = alongWorkDao.getDtlById(id);
        String createUser = userInfoDao.getDetlById(alongWork.getCreateUserId()).getUserName();
        String taskUser = personnelManagementDao.getDetlById(alongWork.getTaskUserId()).getPersonnelName();
        String taskName = alongWork.getTaskName();
        String taskDescribe = alongWork.getTaskDescribe();
        String videoUrl = alongWork.getVideoUrl();
        long roomId = alongWork.getRoomId();

        JSONArray videoArr = new JSONArray();

        if (StringUtils.isEmpty(videoUrl) || JSONUtil.parseArray(videoUrl).size() == 0) {
            //遍历url获取录制的视频
            long robotId = robotRoomDao.getRobotIdByRoomId(roomId);

            //需要获取指定页面的Url
            String alongWorkVideoBasicUrl = compositeImageUrl + "/realtimestream/"+robotId+"-"+id+"/";

            try {
                //返回整个页面的数据 （String类型）
                String content = commonService.getHtml(alongWorkVideoBasicUrl);
                //将字符内容解析成一个Document类（Jsoup解析html）
                Document doc = Jsoup.parse(content);
                //找到页面带有onclick属性的a元素
                Elements links = doc.select("a[href]");
                for(Element element : links){
                    //获取带有onclick属性的a元素 标签里面的内容
                    String a = element.select("a").text();
                    if (a.indexOf("..")>-1){
                        continue;
                    }
                    if (a.endsWith(".tmp")){
                        continue;
                    }
                    String url = alongWorkVideoBasicUrl+a;
                    videoArr.add(commonService.url2Https(url));
                }

                videoArr.sort(new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                });
                alongWorkDao.updateVideoUrl(id, videoArr.toString());
            }catch (Exception e){
                log.error("读取随工视频文件异常："+e.getMessage());
            }
        }else{
            videoArr = JSONUtil.parseArray(videoUrl);
            for (int i = 0; i < videoArr.size(); i++) {
                String url = videoArr.getStr(i);
                videoArr.set(i, commonService.url2Https(url));
            }
        }

        List alongWorkList = getAlongWorkDetl(alongWork);
        Map returnMap = new HashMap(4);
        returnMap.put("alongWorkList", alongWorkList);
        returnMap.put("videoUrl", videoArr);
        returnMap.put("createUser", createUser);
        returnMap.put("taskName", taskName);
        returnMap.put("taskDescribe", taskDescribe);
        returnMap.put("taskUser", taskUser);
        returnMap.put("roomId", roomId);
        return returnMap;
    }

    /**
     * 获取随工任务明细
     *
     * @param id
     * @return com.inspur.industrialinspection.entity.AlongWork
     * @author kliu
     * @date 2022/7/11 15:07
     */
    @Override
    public JSONObject getAlongWorkDetlForRobot(long id) {
        JSONObject jsonObject = new JSONObject();
        AlongWork alongWork = alongWorkDao.getDtlById(id);
        long roomId = alongWork.getRoomId();
        RoomInfo roomInfo = roomInfoDao.getDetlById(roomId);
        String roomName = roomInfo.getRoomName();
        String taskUser = personnelManagementDao.getDetlById(alongWork.getTaskUserId()).getPersonnelName();
        jsonObject.set("task_time", alongWork.getTaskTime());
        jsonObject.set("create_time", alongWork.getCreateTime());
        jsonObject.set("task_user", taskUser);
        jsonObject.set("room_name", roomName);
        jsonObject.set("task_name", alongWork.getTaskName());
        jsonObject.set("task_describe", alongWork.getTaskDescribe());
        return jsonObject;
    }

    /**
     * 正常结束任务
     * @param id
     * @return void
     * @author kliu
     * @date 2022/7/6 11:06
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void normalEndTask(long id, String endTime, String reason) {
        AlongWork alongWork = alongWorkDao.getDtlByIdForUpdate(id);
        //结束任务分多种情况
        //1.正常结束任务，不走receiveTerminateResult方法
        //1.异常结束，含云端中断任务、超时和本体端直接点结束按钮，此时走receiveTerminateResult和正常结束任务方法，此时需要进行判断及处理

        String alongWorkStatus = AlongWorkStatus.COMPLETE;
        //终止任务不带有结束时间
        if (StringUtils.isEmpty(endTime)){
            endTime = alongWork.getEndTime();
        }

        //已结束的不再更改内容，防止重复提交
        if(AlongWorkStatus.END.equals(alongWork.getStatus())){
            return;
        }
        //reason为空，证明为正常结束，此时设置为正常结束
        //云端中断任务时，此时reason也为空，但是此时不应该再修改内容
        if (StringUtils.isEmpty(reason)){
            reason = "正常结束";
            //如果数据库中已经保存原因，则用数据库中的，此时为终止任务带过来的数据
            if (!StringUtils.isEmpty(alongWork.getReason())){
                reason = alongWork.getReason();
            }
        }else{
            alongWorkStatus = AlongWorkStatus.END;
        }
        alongWork.setEndTime(endTime);
        alongWork.setReason(reason);
        alongWork.setStatus(alongWorkStatus);
        alongWorkDao.update(alongWork);
    }

    /**
     * 获取随工点位
     *
     * @param roomId
     * @return java.util.List
     * @author kliu
     * @date 2022/7/12 19:21
     */
    @Override
    public List getAlongWorkPointInfos(long roomId) {
        List<PointInfo> list = alongWorkPointInfoDao.list(roomId);
        PointInfo pointInfo;
        String pointName;
        for (int i = 0; i < list.size(); i++) {
            pointInfo = list.get(i);
            pointName = pointInfo.getPointName();
            if (pointName.startsWith("门")){
                list.remove(i);
                i--;
            }
        }
        return list;
    }

    @Override
    public void initAlongWorkPoint(RoomInfo roomInfo) throws InterruptedException {
        long roomId = roomInfo.getRoomId();
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        List<PointInfo> list = pointInfoDao.list(roomId);
        PointInfo pointInfo;
        for (int i = 0; i < list.size(); i++) {
            pointInfo = list.get(i);
            //随工待命点点位，不参与计算随工观察点
            if ("ADMD".equals(pointInfo.getPointName())){
                list.remove(i);
                i--;
                continue;
            }
            if (pointInfo.getLocationX() ==null){
                list.remove(i);
                i--;
            }
        }

        String compressStr = commonService.gzipCompress(JSONUtil.toJsonStr(list));
        mqttPushClient.publish("industrial_robot/casual_work/get_the_worker_points/"+robotId, compressStr);
        //消息下发后添加是否初始化成功的判断，即机器人是否调用云端接口告知随工点位计算成功
        roomInitAlongPoint.put(roomId, false);

        int i=0;
        int whileCount = 50;
        boolean initSuccess = false;
        //10s无结果返回则报错
        while (i < whileCount){
            initSuccess = roomInitAlongPoint.get(roomId);
            if(initSuccess){
                roomInitAlongPoint.remove(roomId);
                break;
            }else{
                i++;
                Thread.sleep(200);
            }
        }

       if (!initSuccess){
           roomInitAlongPoint.remove(roomId);
           throw new RuntimeException("随工点初始化失败，请重新初始化");
       }
    }

    /**
     * 接收随工点
     *
     * @param jsonObject
     * @return void
     * @author kliu
     * @date 2022/8/5 13:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveAlongWorkPoints(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("value");

        long roomId = 0;
        if (jsonArray.size() > 0) {
            roomId = jsonArray.getJSONObject(0).getLong("roomId");
        }else{
            throw new RuntimeException("上传的随工观察点不能为空");
        }
        alongWorkPointInfoDao.delete(roomId);
        alongWorkPointInfoDao.add(jsonArray);
        roomInitAlongPoint.put(roomId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void associatedWaitPoint(long roomId) throws InterruptedException {
        if(!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }

        PointInfo pointInfo = pointInfoService.getRealTimePosture(roomId);
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName("ADMD");
        if (pointInfoDao.checkExist(pointInfo)) {
            pointInfoDao.update(pointInfo);
        }else{
            pointInfoDao.add(pointInfo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void associatedAlongWorkPoint(PointInfo pointInfo) throws InterruptedException {
        long roomId = pointInfo.getRoomId();
        if(!roomInfoDao.checkExist(pointInfo.getRoomId())){
            throw new RuntimeException("传入的机房id不存在，请检查");
        }
        String pointName = pointInfo.getPointName();
        pointInfo = pointInfoService.getRealTimePosture(roomId);
        pointInfo.setRoomId(roomId);
        pointInfo.setPointName(pointName);
        if (alongWorkPointInfoDao.checkExist(pointInfo)) {
            alongWorkPointInfoDao.update(pointInfo);
        }else{
            alongWorkPointInfoDao.add(pointInfo);
        }
    }

    @Override
    public JSONArray getAlongWorkPointInfosIncludeAll(long roomId) {
        List<PointInfo> pointInfos = pointInfoDao.list(roomId);
        List<PointInfo> alongWorkPointInfos = alongWorkPointInfoDao.list(roomId);

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;

        for (PointInfo pointInfo : pointInfos) {
            jsonObject = new JSONObject();
            String pointName = pointInfo.getPointName();
            if ("ADMD".equals(pointName)){
                continue;
            }
            boolean hasAlongWorkPoint = false;
            for (PointInfo alongWorkPointInfo : alongWorkPointInfos) {
                String pointName1 = alongWorkPointInfo.getPointName();
                if (pointName.equals(pointName1)){
                    hasAlongWorkPoint = true;
                    break;
                }
            }
            jsonObject.set("pointName", pointName);
            jsonObject.set("hasAlongWorkPoint", hasAlongWorkPoint);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
