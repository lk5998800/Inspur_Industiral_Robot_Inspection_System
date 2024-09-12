package com.inspur.industrialinspection.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.inspur.industrialinspection.dao.GatingParaDao;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.entity.GatingPara;
import com.inspur.industrialinspection.service.GatingService;
import com.inspur.industrialinspection.service.PhoneNoticeService;
import com.inspur.industrialinspection.service.SendSmsService;
import com.inspur.mqtt.GatingMqttPushClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 门控 Service
 * @author kliu
 * @date 2022/5/9 17:26
 */
@Service
@Slf4j
public class GatingServiceImpl implements GatingService {

    private volatile static ConcurrentHashMap<String, Boolean> gatingMap = new ConcurrentHashMap<String, Boolean>();
    private volatile static ConcurrentHashMap<String, Long> gatingCacheMap = new ConcurrentHashMap<String, Long>();

    @Autowired
    private GatingMqttPushClient gatingMqttPushClient;
    @Autowired
    private GatingParaDao gatingParaDao;
    @Autowired
    private RobotRoomDao robotRoomDao;
    @Autowired
    private SendSmsService sendSmsService;
    @Autowired
    private PhoneNoticeService phoneNoticeService;
    @Autowired
    private ThreadPoolExecutor executor;

    @Value("${sms.robot.exception.notifyPhoneNumber}")
    private String notifyPhoneNumber;

    /**
     * 初始化门控
     * @param
     * @return void
     * @author kliu
     * @date 2022/10/19 9:41
     */
//    @PostConstruct
//    public void initThreadPool(){
//        executor.submit(() -> {
//            try {
//                startGatingCloseDoorThread();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    /**
     * 门控关门指令
     * @param
     * @return void
     * @author kliu
     * @date 2022/10/19 10:12
     */
    public void startGatingCloseDoorThread() throws Exception {
        Long cacheTimeMillis;
        long currentTimeMillis;
        String requestOrder = "dooracs/v1/close_door";;
        JSONObject jsonObject;
        while (true){
            currentTimeMillis = System.currentTimeMillis();
            for (String key : gatingCacheMap.keySet()){
                cacheTimeMillis = gatingCacheMap.get(key);
                //10s 触发关门指令
                if (currentTimeMillis - cacheTimeMillis > 10000){
                    jsonObject = new JSONObject();
                    jsonObject.set("door_code", key);
                    jsonObject.set("request_order", requestOrder);
                    invokeGating(jsonObject);
                    log.info("当前机房门超过10s没有关闭，系统自动关闭当前门");
                }
            }
            Thread.sleep(1000);
        }
    }

    /**
     * 调用门控
     * @param jsonObject
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/5/25 8:38
     */
    @Override
    public void invokeGating(JSONObject jsonObject) throws Exception {
        String requestOrder = jsonObject.getStr("request_order");
        String doorCode = jsonObject.getStr("door_code");
        String reqid = IdUtil.simpleUUID();
        JSONObject paraObject = new JSONObject();
        paraObject.set("param", "");
        paraObject.set("order", requestOrder);
        paraObject.set("req", requestOrder);
        paraObject.set("reqid", reqid);
        gatingMqttPushClient.publish("inspur/"+doorCode+"/v1", paraObject.toString());
        gatingMap.put(reqid, false);

        int i=0;
        int whileCount = 40;
        //20s无结果返回则报错
        while (i < whileCount){
            //门开的比较慢，等1s给门一个响应时间
            Thread.sleep(500);
            if(gatingMap.get(reqid)){
                gatingMap.remove(reqid);
                //门调用成功之后，保存当前门的状态信息，如已开门，则倒计时10s后如果没有调用关门指令则自动发送关门的指令
                //门控模块当前配置15s后自动关门，应用层面按照10s进行测试
                //关门
//                if (requestOrder.indexOf("close_door")>-1){
//                    if (gatingCacheMap.containsKey(doorCode)) {
//                        gatingCacheMap.remove(doorCode);
//                    }
//                }else if (requestOrder.indexOf("open_door")>-1){
//                    //开门
//                    gatingCacheMap.put(doorCode, System.currentTimeMillis());
//                }
                return;
            }
            i++;
        }

        gatingMap.remove(reqid);

        long robotId = 0;

        GatingPara gatingPara = null;
        try {
            gatingPara = gatingParaDao.getDetlByDoorCode(doorCode);
            long roomId = gatingPara.getRoomId();
            robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        } catch (Exception e) {
           log.error(e.getMessage());
        }

        //门控失败，添加短信告警
        JSONObject paramObject = new JSONObject();
        paramObject.set("robotid", robotId+"");
        paramObject.set("warncontent", doorCode+"门控接口调用失败");
        ////回桩失败，发短信
        sendSmsService.sendSms(notifyPhoneNumber, "SMS_257678030", paramObject.toString());
        phoneNoticeService.phoneNotice(notifyPhoneNumber, "TTS_256355249", paramObject.toString());

        throw new RuntimeException("推送门控消息失败，未返回结果数据，门控id="+doorCode);
    }

    /**
     * 接收mqtt返回数据
     * @param str
     * @return void
     * @author kliu
     * @date 2022/7/5 14:50
     */
    @Override
    public void receiveMqttBack(String str) {
        String left = "{";
        if (str.startsWith(left)){
            JSONObject jsonObject = JSONUtil.parseObj(str);
            if (!jsonObject.containsKey("reqid")){
                return;
            }
            String reqid = jsonObject.getStr("reqid");
            String code = jsonObject.getStr("code");
            if (gatingMap.containsKey(reqid)) {
                gatingMap.put(reqid, true);
            }
        }
    }

    /**
     * 获取门控参数
     * @param roomId
     * @param pointName
     * @return cn.hutool.json.JSONObject
     * @author kliu
     * @date 2022/7/11 15:39
     */
    @Override
    public GatingPara getDetlById(long roomId, String pointName) {
        GatingPara gatingPara = new GatingPara();
        gatingPara.setRoomId(roomId);
        gatingPara.setPointName(pointName);
        if (gatingParaDao.checkExist(gatingPara)){
            return gatingParaDao.getDetlById(roomId, pointName);
        }
        gatingPara.setDoorCode("");
        gatingPara.setRequestOrder("");
        return gatingPara;
    }

    /**
     * 添加门控参数
     *
     * @param gatingPara
     * @return com.inspur.industrialinspection.entity.GatingPara
     * @author kliu
     * @date 2022/7/11 15:43
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addOrUpdate(GatingPara gatingPara) {
        if (gatingParaDao.checkExist(gatingPara)){
            gatingParaDao.update(gatingPara);
        }else{
            gatingParaDao.add(gatingPara);
        }
    }


    /**
     * 根据单个点位获取所有门控参数
     * @param roomId
     * @param pointName
     * @return cn.hutool.json.JSONObject
     * @author ldh
     * @date 2022/10/25
     */
    @Override
    public List<GatingPara> getDetlsById(long roomId, String pointName) {
        return gatingParaDao.getDetlsById(roomId, pointName);
    }
    /**
     * 修改多个门控参数
     * @param gatingParas
     * @return cn.hutool.json.JSONObject
     * @author ldh
     * @date 2022/10/25
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addOrUpdateList(List<GatingPara> gatingParas) {
        for (GatingPara gatingPara : gatingParas) {
            if (gatingParaDao.checkExist(gatingPara)){
                gatingParaDao.update(gatingPara);
            }else{
                gatingParaDao.add(gatingPara);
            }
        }
    }

    @Override
    public void invokeOpenDoor(int doorCode) {
        String reqid = IdUtil.simpleUUID();
        JSONObject paraObject = new JSONObject();
        paraObject.set("param", "");
        paraObject.set("order", "dooracs/v1/open_door");
        paraObject.set("req", "dooracs/v1/open_door");
        paraObject.set("reqid", reqid);
        gatingMqttPushClient.publish("inspur/"+doorCode+"_floor_front_door"+"/v1", paraObject.toString());
    }

    @Override
    public void invokeCloseDoor(int doorCode) {
        String reqid = IdUtil.simpleUUID();
        JSONObject paraObject = new JSONObject();
        paraObject.set("param", "");
        paraObject.set("order", "dooracs/v1/close_door");
        paraObject.set("req", "dooracs/v1/close_door");
        paraObject.set("reqid", reqid);
        gatingMqttPushClient.publish("inspur/"+doorCode+"_floor_front_door"+"/v1", paraObject.toString());
    }
}
