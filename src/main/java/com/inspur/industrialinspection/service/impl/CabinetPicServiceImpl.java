package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.inspur.industrialinspection.dao.RoomInfoDao;
import com.inspur.industrialinspection.dao.TaskDetectionResultDao;
import com.inspur.industrialinspection.dao.TaskInstanceDao;
import com.inspur.industrialinspection.entity.TaskDetectionResult;
import com.inspur.industrialinspection.entity.TaskInstance;
import com.inspur.industrialinspection.service.AiAgentService;
import com.inspur.industrialinspection.service.CabinetPicService;
import com.inspur.industrialinspection.service.CommonService;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * 机柜图片服务
 * @author kliu
 * @date 2022/5/9 17:26
 */
@Service
@Slf4j
public class CabinetPicServiceImpl implements CabinetPicService {

    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private TaskDetectionResultDao taskDetectionResultDao;

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private AiAgentService aiAgentService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private RoomInfoDao roomInfoDao;

    private static String BUCKET_NAME = "botdata";

    /**
     * 创建图片文件夹
     * @param roomId
     * @author kliu
     * @date 2022/7/9 10:43
     */
    @Override
    public void createPicFile(long roomId, HttpServletResponse response) throws Exception {
        List<TaskInstance> taskInstances = taskInstanceDao.getTaskByRoomIdAndDate(roomId, "2022-07-01");
        Iterable<Result<Item>> myObjects = minioClient.listObjects(BUCKET_NAME);
        Iterator<Result<Item>> iterator = myObjects.iterator();
        InputStream inputStream;
        File file;
        while (iterator.hasNext()) {
            Item item = iterator.next().get();
            String objectName = item.objectName();
            //判断对应的文件名称是否是报警灯，如果是，则保存，如果不是，则不保存
            //判断文件名称中，是否是用于显示的，用于显示的保存下来
            if (objectName.indexOf("alarm_light")>-1 && objectName.indexOf("show")>-1){
                String instanceIdStr = objectName.substring(objectName.indexOf("/")+1, objectName.indexOf("/", objectName.indexOf("/")+1));
                Long instanceId = Long.parseLong(instanceIdStr);

                for (int i = 0; i < taskInstances.size(); i++) {
                    long listInstanceId = taskInstances.get(i).getInstanceId();
                    if (instanceId == listInstanceId) {
                        //开始保存数据
                        inputStream = minioClient.getObject(BUCKET_NAME, objectName);

                        file = new File("D://pic//"+objectName);
                        FileUtil.writeFromStream(inputStream, file);
                        Thread.sleep(200);
                    }
                }
            }
        }
    }
    /**
     * 创建图片文件夹-带有红色指示灯的
     * @param roomId
     * @author kliu
     * @date 2022/7/9 10:43
     */
    @Override
    public void createPicFileWithRedlight(long roomId, HttpServletResponse response) throws Exception {
        List<TaskInstance> taskInstances = taskInstanceDao.getTaskByRoomIdAndDate(roomId, "2022-05-01");
        List<TaskDetectionResult> list = null;
        JSONObject jsonObject;
        JSONArray redLightCountArr;
        InputStream inputStream;
        File file;
        for (TaskInstance taskInstance : taskInstances) {
            long instanceId = taskInstance.getInstanceId();
            list = taskDetectionResultDao.list(instanceId);
            for (TaskDetectionResult detectionResult : list) {
                String alarmLight = detectionResult.getAlarmLight();
                if (!StringUtils.isEmpty(alarmLight)){
                    jsonObject = JSONUtil.parseObj(alarmLight);
                    if (jsonObject.containsKey("red_light_count")) {
                        redLightCountArr = jsonObject.getJSONArray("red_light_count");
                        for (int j = 0; j < redLightCountArr.size(); j++) {
                            Integer redLightCount = redLightCountArr.getInt(j);
                            if (redLightCount>0){
                                if (jsonObject.containsKey("path")){
                                    String path = jsonObject.getJSONArray("path").getStr(j).replace("http://59.110.25.180:9001/botdata/", "");
                                    if (path.indexOf("detection")>-1){
                                        //下载该条数据
                                        inputStream = minioClient.getObject(BUCKET_NAME, path);

                                        file = new File("D://pic//"+System.currentTimeMillis()+".jpg");
                                        FileUtil.writeFromStream(inputStream, file);
                                        Thread.sleep(200);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List getPicInfos(int parkId, JSONObject paramObject) {
        if (!paramObject.containsKey("roomId")){
            throw new RuntimeException("机房id不能为空，请检查传入的数据");
        }
        if (!paramObject.containsKey("qsrq")){
            throw new RuntimeException("起始日期不能为空，请检查传入的数据");
        }
        if (!paramObject.containsKey("zzrq")){
            throw new RuntimeException("终止日期不能为空，请检查传入的数据");
        }

        Long roomId = paramObject.getLong("roomId");
        String qsrq = paramObject.getStr("qsrq");
        String zzrq = paramObject.getStr("zzrq");

        if (!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入机房id在系统中不存在，请检查传入的数据");
        }

        try {
            DateUtil.parse(qsrq,"yyyy-MM-dd");
        } catch (Exception e) {
            throw new RuntimeException("传入的起始日期应符合yyyy-MM-dd，请检查传入的数据");
        }

        try {
            DateUtil.parse(zzrq,"yyyy-MM-dd");
        } catch (Exception e) {
            throw new RuntimeException("传入的终止日期应符合yyyy-MM-dd，请检查传入的数据");
        }


        zzrq = DateUtil.offsetDay(DateUtil.parseDate(zzrq), 1).toString("yyyy-MM-dd");

        long betweenDay = DateUtil.betweenDay(DateUtil.parseDate(qsrq), DateUtil.parseDate(zzrq), false);

        if (betweenDay>30){
            throw new RuntimeException("日期跨度不能超过30天");
        }

        JSONArray picList = new JSONArray();
        String pointName = "";
        String alarmLight = "";
        boolean existsFlag;
        String cachePointName ="";
        JSONArray cacheUrlList;
        JSONObject tempObject;
        JSONObject alarmLightObject;
        JSONArray pathArray;
        JSONObject picObject;

        List<TaskDetectionResult> list;
        List<TaskInstance> taskInstances = taskInstanceDao.getTaskInstanceByRoomIdAndDate(roomId, qsrq, zzrq);
        for (TaskInstance taskInstance : taskInstances) {
            long instanceId = taskInstance.getInstanceId();
            list = taskDetectionResultDao.list(instanceId);
            for (TaskDetectionResult detectionResult : list) {
                pointName = detectionResult.getPointName();
                alarmLight = detectionResult.getAlarmLight();
                if (StringUtils.isEmpty(alarmLight)){
                    continue;
                }

                alarmLightObject = JSONUtil.parseObj(alarmLight);

                existsFlag = false;
                for (int i = 0; i < picList.size(); i++) {
                    cachePointName = picList.getJSONObject(i).getStr("pointName");
                    if(cachePointName.equals(pointName)){
                        existsFlag = true;
                        cacheUrlList = picList.getJSONObject(i).getJSONArray("urlList");
                        pathArray = alarmLightObject.getJSONArray("path");
                        for (int j = 0; j < pathArray.size(); j++) {
                            String str = pathArray.getStr(j);
                            if (str.indexOf("show")>-1){
                                tempObject = new JSONObject();
                                tempObject.set("url", str);
                                cacheUrlList.add(tempObject);
                            }
                        }

                        break;
                    }
                }

                if (!existsFlag){
                    pathArray = alarmLightObject.getJSONArray("path");
                    cacheUrlList = new JSONArray();
                    for (int j = 0; j < pathArray.size(); j++) {
                        String str = pathArray.getStr(j);
                        if (str.indexOf("show")>-1){
                            tempObject = new JSONObject();
                            tempObject.set("url", str);
                            cacheUrlList.add(tempObject);
                        }
                    }

                    picObject = new JSONObject();
                    picObject.set("pointName", pointName);
                    picObject.set("urlList", cacheUrlList);

                    picList.add(picObject);
                }
            }
        }

        return picList;
    }

    @Override
    public List getAbnormalPicInfos(int parkId, JSONObject paramObject) {

        if (!paramObject.containsKey("roomId")){
            throw new RuntimeException("机房id不能为空，请检查传入的数据");
        }
        if (!paramObject.containsKey("qsrq")){
            throw new RuntimeException("起始日期不能为空，请检查传入的数据");
        }
        if (!paramObject.containsKey("zzrq")){
            throw new RuntimeException("终止日期不能为空，请检查传入的数据");
        }

        Long roomId = paramObject.getLong("roomId");
        String qsrq = paramObject.getStr("qsrq");
        String zzrq = paramObject.getStr("zzrq");

        if (!roomInfoDao.checkExist(roomId)){
            throw new RuntimeException("传入机房id在系统中不存在，请检查传入的数据");
        }

        try {
            DateUtil.parse(qsrq,"yyyy-MM-dd");
        } catch (Exception e) {
            throw new RuntimeException("传入的起始日期应符合yyyy-MM-dd，请检查传入的数据");
        }

        try {
            DateUtil.parse(zzrq,"yyyy-MM-dd");
        } catch (Exception e) {
            throw new RuntimeException("传入的终止日期应符合yyyy-MM-dd，请检查传入的数据");
        }

        zzrq = DateUtil.offsetDay(DateUtil.parseDate(zzrq), 1).toString("yyyy-MM-dd");

        long betweenDay = DateUtil.betweenDay(DateUtil.parseDate(qsrq), DateUtil.parseDate(zzrq), false);

        if (betweenDay>30){
            throw new RuntimeException("日期跨度不能超过30");
        }

        JSONArray picList = new JSONArray();
        String pointName = "";
        String alarmLight = "";
        boolean existsFlag;
        String cachePointName ="";
        JSONArray alarmLightCacheUrlList;
        JSONArray infraredCacheUrlList;
        JSONObject tempObject;
        JSONObject alarmLightObject;
        JSONObject picObject;

        List<TaskDetectionResult> list;
        List<TaskInstance> taskInstances = taskInstanceDao.getTaskInstanceByRoomIdAndDate(roomId, qsrq, zzrq);
        for (TaskInstance taskInstance : taskInstances) {
            long instanceId = taskInstance.getInstanceId();
            list = taskDetectionResultDao.abnormalList(instanceId);
            for (TaskDetectionResult detectionResult : list) {
                pointName = detectionResult.getPointName();
                alarmLight = detectionResult.getAlarmLight();
                String infrared = detectionResult.getInfrared();
                if (!StringUtils.isEmpty(alarmLight)){
                    alarmLightObject = JSONUtil.parseObj(alarmLight);
                    if (alarmLightObject.containsKey("status")){
                        String status = alarmLightObject.getStr("status");
                        if ("normal".equals(status)){
                            continue;
                        }else{
                            JSONArray pathArr = alarmLightObject.getJSONArray("path");
                            for (int i = 0; i < pathArr.size(); i++) {
                                String str = pathArr.getStr(i);
                                if (str.indexOf("detection")>-1){
                                    pathArr.remove(i);
                                    i--;
                                }
                            }

                            existsFlag = false;
                            for (int i = 0; i < picList.size(); i++) {
                                cachePointName = picList.getJSONObject(i).getStr("pointName");
                                if(cachePointName.equals(pointName)){
                                    existsFlag = true;
                                    alarmLightCacheUrlList = picList.getJSONObject(i).getJSONArray("alarmLightUrlList");
                                    if (alarmLightCacheUrlList==null){
                                        alarmLightCacheUrlList = new JSONArray();
                                    }
                                    JSONArray redLightCountArr = alarmLightObject.getJSONArray("red_light_count");
                                    for (int j = 0; j < redLightCountArr.size(); j++) {
                                        Integer count = redLightCountArr.getInt(j);
                                        if(count>0){
                                            tempObject = new JSONObject();
                                            tempObject.set("url", pathArr.getStr(j));
                                            alarmLightCacheUrlList.add(tempObject);
                                        }
                                    }

                                    break;
                                }
                            }

                            if (!existsFlag){
                                alarmLightCacheUrlList = new JSONArray();
                                JSONArray redLightCountArr = alarmLightObject.getJSONArray("red_light_count");
                                for (int j = 0; j < redLightCountArr.size(); j++) {
                                    Integer count = redLightCountArr.getInt(j);
                                    if(count>0){
                                        tempObject = new JSONObject();
                                        tempObject.set("url", pathArr.getStr(j));
                                        alarmLightCacheUrlList.add(tempObject);
                                    }
                                }

                                picObject = new JSONObject();
                                picObject.set("pointName", pointName);
                                picObject.set("alarmLightUrlList", alarmLightCacheUrlList);

                                picList.add(picObject);
                            }
                        }
                    }
                }

                if (!StringUtils.isEmpty(infrared)){
                    JSONArray infraredArray = JSONUtil.parseArray(infrared);
                    for (int i = 0; i < infraredArray.size(); i++) {
                        JSONObject jsonObj = infraredArray.getJSONObject(i);
                        String status = jsonObj.getStr("status");
                        if ("normal".equals(status)){
                            continue;
                        }else{
                            String infraredMergeUrl = infraredArray.getJSONObject(0).getStr("infrared_merge_url");
                            infraredMergeUrl = commonService.url2Https(infraredMergeUrl);

                            existsFlag = false;
                            for (int j = 0; j < picList.size(); j++) {
                                cachePointName = picList.getJSONObject(j).getStr("pointName");
                                if(cachePointName.equals(pointName)){
                                    existsFlag = true;
                                    infraredCacheUrlList = picList.getJSONObject(j).getJSONArray("infraredUrlList");
                                    if (infraredCacheUrlList==null){
                                        infraredCacheUrlList = new JSONArray();
                                    }
                                    tempObject = new JSONObject();
                                    tempObject.set("url", infraredMergeUrl);
                                    infraredCacheUrlList.add(tempObject);

                                    break;
                                }
                            }

                            if (!existsFlag){
                                infraredCacheUrlList = new JSONArray();
                                tempObject = new JSONObject();
                                tempObject.set("url", infraredMergeUrl);
                                infraredCacheUrlList.add(tempObject);

                                picObject = new JSONObject();
                                picObject.set("pointName", pointName);
                                picObject.set("infraredUrlList", infraredCacheUrlList);

                                picList.add(picObject);
                            }
                        }
                    }
                }
            }
        }

        return picList;
    }

    @Override
    public void createInfraredPicFile(long roomId, HttpServletResponse response) throws Exception {
        List<TaskInstance> taskInstances = taskInstanceDao.getTaskByRoomIdAndDate(roomId, "2022-09-01");
        Iterable<Result<Item>> myObjects = minioClient.listObjects(BUCKET_NAME);
        Iterator<Result<Item>> iterator = myObjects.iterator();
        InputStream inputStream;
        File file;
        while (iterator.hasNext()) {
            Item item = iterator.next().get();
            String objectName = item.objectName();
            //判断对应的文件名称是否是报警灯，如果是，则保存，如果不是，则不保存
            //判断文件名称中，是否是用于显示的，用于显示的保存下来
            if (objectName.indexOf("infrared")>-1){
                String instanceIdStr = objectName.substring(objectName.indexOf("/")+1, objectName.indexOf("/", objectName.indexOf("/")+1));
                Long instanceId = Long.parseLong(instanceIdStr);

                for (int i = 0; i < taskInstances.size(); i++) {
                    long listInstanceId = taskInstances.get(i).getInstanceId();
                    if (instanceId == listInstanceId) {
                        //开始保存数据
                        inputStream = minioClient.getObject(BUCKET_NAME, objectName);

                        file = new File("D://pic//"+objectName);
                        FileUtil.writeFromStream(inputStream, file);
                        Thread.sleep(200);
                    }
                }
            }
        }
    }

    @Override
    public void createInfraredPicFileTranslate(long roomId, HttpServletResponse response) throws Exception {
        List<TaskInstance> taskInstances = taskInstanceDao.getTaskByRoomIdAndDate(roomId, "2022-08-13");
        Iterable<Result<Item>> myObjects = minioClient.listObjects(BUCKET_NAME);
        Iterator<Result<Item>> iterator = myObjects.iterator();
        InputStream inputStream;
        File file;
        while (iterator.hasNext()) {
            Item item = iterator.next().get();
            String objectName = item.objectName();
            //判断对应的文件名称是否是报警灯，如果是，则保存，如果不是，则不保存
            //判断文件名称中，是否是用于显示的，用于显示的保存下来
            if (objectName.indexOf("infrared")>-1){
                String instanceIdStr = objectName.substring(objectName.indexOf("/")+1, objectName.indexOf("/", objectName.indexOf("/")+1));
                Long instanceId = Long.parseLong(instanceIdStr);

                for (int i = 0; i < taskInstances.size(); i++) {
                    long listInstanceId = taskInstances.get(i).getInstanceId();
                    if (instanceId == listInstanceId) {

                        JSONArray pathArray = new JSONArray();
                        pathArray.add("http://ibot.icloudbot.com:9001/botdata/"+objectName);
                        JSONObject serviceObject = new JSONObject();
                        serviceObject.set("image_url", pathArray);
                        serviceObject.set("infrared_draw_thresh", 100);

                        JSONObject serviceResult = aiAgentService.invokeHttp("http://10.180.151.232:8001/aicloud/detection/infrared", serviceObject.toString());
                        String infraredMergeUrl = "http://10.180.151.232:8803/"+serviceResult.getStr("infrared_merge_url").replace("./repic","/repic");

                        HttpResponse execute = HttpUtil.createGet(infraredMergeUrl).execute();
                        inputStream = execute.bodyStream();
                        file = new File("D://pic//"+objectName.replace(".tiff",".jpg"));
                        FileUtil.writeFromStream(inputStream, file);
                        Thread.sleep(200);
                    }
                }
            }
        }
    }
}
