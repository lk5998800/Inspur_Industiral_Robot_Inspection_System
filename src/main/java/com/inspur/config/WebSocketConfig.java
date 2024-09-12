package com.inspur.config;

import com.inspur.industrialinspection.dao.RobotRoomDao;
import com.inspur.industrialinspection.service.JwtService;
import com.inspur.industrialinspection.service.RemoteControlService;
import com.inspur.industrialinspection.service.RemoteControlTaskInstanceService;
import com.inspur.industrialinspection.service.RequestService;
import com.inspur.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author: kliu
 * @description: websocket配置
 * @date: 2022/8/25 14:36
 */
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Autowired
    public void setRequestService(RequestService requestService) {
        WebSocketServer.requestService = requestService;
    }

    @Autowired
    public void setRemoteControlService(RemoteControlService remoteControlService) {
        WebSocketServer.remoteControlService = remoteControlService;
    }
    @Autowired
    public void setRobotRoomDao(RobotRoomDao robotRoomDao) {
        WebSocketServer.robotRoomDao = robotRoomDao;
    }
    @Autowired
    public void setJwtService(JwtService jwtService) {
        WebSocketServer.jwtService = jwtService;
    }
    @Autowired
    public void setRemoteControlTaskInstanceService(RemoteControlTaskInstanceService remoteControlTaskInstanceService) {
        WebSocketServer.remoteControlTaskInstanceService = remoteControlTaskInstanceService;
    }
    @Autowired
    public void setDataSourceTransactionManager(DataSourceTransactionManager dataSourceTransactionManager) {
        WebSocketServer.dataSourceTransactionManager = dataSourceTransactionManager;
    }
    @Autowired
    public void setTransactionDefinition(TransactionDefinition transactionDefinition) {
        WebSocketServer.transactionDefinition = transactionDefinition;
    }
}
