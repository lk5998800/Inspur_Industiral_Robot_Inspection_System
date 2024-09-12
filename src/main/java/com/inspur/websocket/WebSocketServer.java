package com.inspur.websocket;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.entity.RemoteControlTaskInstance;
import com.inspur.industrialinspection.service.JwtService;
import com.inspur.industrialinspection.service.RemoteControlService;
import com.inspur.industrialinspection.service.RemoteControlTaskInstanceService;
import com.inspur.industrialinspection.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: kliu
 * @description: websocket server
 * @date: 2022/8/25 14:38
 */
@Component
@Slf4j
@Service
@ServerEndpoint("/industrial_robot/websocket/{token}/{roomId}")
public class WebSocketServer {

    @Autowired
    public static RequestService requestService;
    @Autowired
    public static RemoteControlService remoteControlService;
    @Autowired
    public static RemoteControlTaskInstanceService remoteControlTaskInstanceService;
    @Autowired
    public static RobotRoomDao robotRoomDao;
    @Autowired
    public static JwtService jwtService;
    @Autowired
    public static DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    public static TransactionDefinition transactionDefinition;

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static ConcurrentHashMap<Long, Long> onlineMap = new ConcurrentHashMap();

    /**
     * 连接建立成功调用的方法
     * @param session
     * @param token
     * @return void
     * @author kliu
     * @date 2022/8/25 14:43
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token, @PathParam("roomId") long roomId) throws IOException {
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        JSONObject json = new JSONObject();
        boolean tokenValid = true;
        try {
            tokenValid = jwtService.isTokenValid(token);
        }catch (TokenExpiredException e){
            tokenValid = false;
        }
        if (!tokenValid) {
            json.set("code", 1);
            json.set("message", "token已过期");
            sendMessage(json.toString(), session);
            log.error("申请机器人【"+robotId+"】远程控制权限失败，token已过期");
            return;
        }

        long userId = requestService.getUserIdByToken(token);

        if (robotIsContro(robotId)) {
            json.set("code", 2);
            json.set("message", "机器人【"+robotId+"】当前已经被远程控制，不允许多人控制");
            sendMessage(json.toString(), session);
            log.error("userid:"+userId+"申请远程控制权限失败，当前仅允许一人申请远程控制权限");
            return;
        }

        addUser(robotId, userId);
        json.set("code", 0);
        json.set("message", "success");
        sendMessage(json.toString(), session);
        log.info("userid:"+userId+"申请机器人【"+robotId+"】远程控制权限成功");

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        RemoteControlTaskInstance remoteControlTaskInstance = new RemoteControlTaskInstance();
        remoteControlTaskInstance.setStartTime(DateUtil.now());
        remoteControlTaskInstance.setExecStatus("running");
        remoteControlTaskInstance.setUserId(userId);
        remoteControlTaskInstance.setRobotId(robotId);
        remoteControlTaskInstance.setRoomId(roomId);
        try {
            remoteControlTaskInstanceService.add(remoteControlTaskInstance);
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;
        } catch (Exception e) {
            if (transactionStatus !=null){
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            json = new JSONObject();
            json.set("code", 1);
            json.set("message", e.getMessage());
            sendMessage(json.toString(), session);
            return;
        }

    }

    /**
     * 连接关闭调用的方法
     * @param
     * @return void
     * @author kliu
     * @date 2022/8/25 15:55
     */
    @OnClose
    public void onClose(Session session, @PathParam("token") String token, @PathParam("roomId") long roomId) throws IOException {
        long robotId = robotRoomDao.getRobotIdByRoomId(roomId);
        deleteUser(robotId); //删除用户
        boolean tokenValid = true;
        try {
            tokenValid = jwtService.isTokenValid(token);
        }catch (TokenExpiredException e){
            tokenValid = false;
        }
        if (!tokenValid) {
            log.info("userid:token已过期取消申请机器人【"+robotId+"】远程控制权限成功");
            return;
        }

        long userId = requestService.getUserIdByToken(token);
        log.info("userid:"+userId+"取消申请机器人【"+robotId+"】远程控制权限成功");

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            //将该人对应的远程遥控任务更新为结束
            remoteControlTaskInstanceService.updateTaskEndByUserId(userId);
            dataSourceTransactionManager.commit(transactionStatus);
            transactionStatus = null;
        } catch (Exception e) {
            if (transactionStatus !=null){
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            JSONObject json = new JSONObject();
            json.set("code", 1);
            json.set("message", e.getMessage());
            sendMessage(json.toString(), session);
            return;
        }
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message
     * @param session
     * @return void
     * @author kliu
     * @date 2022/8/25 15:17
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("token") String token) throws IOException {
        log.info("远程控制移动接口message="+message);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = JSONUtil.parseObj(message);
        } catch (Exception e) {
            message = e.getMessage();
            if (StringUtils.isEmpty(message)) {
                message = e.getCause().getMessage();
            }
            JSONObject json = new JSONObject();
            json.set("code", 1);
            json.set("message", message);
            sendMessage(json.toString(), session);
            return;
        }
        Long robotId = jsonObject.getLong("robotId", 0L);
        Long roomId = jsonObject.getLong("roomId", 0L);
        Double v = jsonObject.getDouble("v");
        Double w = jsonObject.getDouble("w");

        try {
            if(robotId == 0){
                robotId = robotRoomDao.getRobotIdByRoomId(roomId);
            }
            long userId = requestService.getUserIdByToken(token);
            if (userHasRight(userId, robotId)) {
                remoteControlService.move(robotId, roomId, v, w);
            }else{
                throw new RuntimeException("当前远程控制申请权限与实际控制机器人权限不一致，请检查");
            }
        } catch (Exception e) {
            message = e.getMessage();
            JSONObject json = new JSONObject();
            json.set("code", 1);
            json.set("message", message);
            sendMessage(json.toString(), session);
            return;
        }

        JSONObject json = new JSONObject();
        json.set("code", 0);
        json.set("message", "success");
        sendMessage(json.toString(), session);
    }

    /**
     * websocket发生错误时的处理
     * @param session
     * @param error
     * @return void
     * @author kliu
     * @date 2022/8/25 14:46
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("websocket发生错误"+error.getMessage());
        error.printStackTrace();
    }

    /**
     * 发送消息
     * @param message
     * @param session
     * @return void
     * @author kliu
     * @date 2022/8/25 14:48
     */
    public void sendMessage(String message, Session session) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /**
     * 机器人是否被远程控制
     * @param robotId
     * @return boolean
     * @author kliu
     * @date 2022/8/26 16:05
     */
    public static synchronized boolean robotIsContro(long robotId) {
        return onlineMap.containsKey(robotId);
    }

    public static synchronized boolean userHasRight(long userId, long robotId) {
        if (onlineMap.containsKey(robotId)){
            return onlineMap.get(robotId) == userId;
        }
        return false;
    }

    public static synchronized void addUser(long robotId, long userId) {
        onlineMap.put(robotId, userId);
    }

    public static synchronized void deleteUser(long robotId) {
        onlineMap.remove(robotId);
    }
}
