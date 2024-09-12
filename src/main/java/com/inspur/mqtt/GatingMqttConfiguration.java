package com.inspur.mqtt;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author kliu
 * @description 门控mqtt配置类，获取mqtt连接
 * @date 2022/4/18 20:30
 */
@Component
@Configuration
@Data
@Slf4j
public class GatingMqttConfiguration {

    @Autowired
    private GatingMqttPushClient gatingMqttPushClient;

    @Value("${gatingmqtt.host}")
    private String host;

    @Value("${gatingmqtt.clientid}")
    private String clientid;

    @Value("${gatingmqtt.username}")
    private String username;

    @Value("${gatingmqtt.password}")
    private String password;

    @Value("${gatingmqtt.timeout}")
    private String timeout;

    @Value("${gatingmqtt.keepalive}")
    private String keepAlive;

    /**
     * @author kliu
     * @description 连接至mqtt服务器，获取mqtt连接
     * @date 2022/5/24 17:58
     */
    @Bean
    public GatingMqttPushClient getGatingMqttPushClient() {
        //连接至mqtt服务器，获取mqtt连接
        while (true){
            try {
                gatingMqttPushClient.connect(host, clientid, username, password, Integer.parseInt(timeout), Integer.parseInt(keepAlive));
                break;
            }catch (Exception e) {
                log.error("门控mqtt连接失败："+e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
        //一连接mqtt,就订阅默认需要订阅的主题
        new GatingMqttSubClient(gatingMqttPushClient);
        return gatingMqttPushClient;
    }
}