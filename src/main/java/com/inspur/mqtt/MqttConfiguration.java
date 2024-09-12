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
 * @description mqtt配置类，获取mqtt连接
 * @date 2022/4/18 20:30
 */
@Component
@Configuration
@Data
@Slf4j
public class MqttConfiguration {

    @Autowired
    private MqttPushClient mqttPushClient;

    @Value("${mqtt.host}")
    private String host;

    @Value("${mqtt.clientid}")
    private String clientid;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.timeout}")
    private String timeout;

    @Value("${mqtt.keepalive}")
    private String keepAlive;

    /**
     * @author kliu
     * @description 连接至mqtt服务器，获取mqtt连接
     * @date 2022/5/24 17:58
     */
    @Bean
    public MqttPushClient getMqttPushClient() {
        //连接至mqtt服务器，获取mqtt连接
        while (true){
            try {
                mqttPushClient.connect(host, clientid, username, password, Integer.parseInt(timeout), Integer.parseInt(keepAlive));
                break;
            }catch (Exception e) {
                log.error("主mqtt连接失败："+e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }

        //一连接mqtt,就订阅默认需要订阅的主题
        new MqttSubClient(mqttPushClient);
        return mqttPushClient;
    }
}