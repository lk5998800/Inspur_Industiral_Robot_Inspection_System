package com.inspur.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 日志配置-将不需要记录的日志过滤掉
 * @author kliu
 * @date 2022/7/14 20:27
 */
@Data
@ConfigurationProperties(prefix = "log")
@Component
public class LogConfig {

    private String notrecordingmethod;

    private static String notRecordingMethodStr="";

    @Bean
    public LogConfig getLogConfig(){
        return new LogConfig();
    }

    @PostConstruct
    public void init(){
        notRecordingMethodStr = notrecordingmethod;
    }

    public String getNotRecordingMethodStr(){
        return notRecordingMethodStr;
    }

    public void addMethod(String method){
        notRecordingMethodStr = notRecordingMethodStr+"," + method;
    }

    public void removeMethod(String method){
        notRecordingMethodStr = notRecordingMethodStr.replace(method,"");
    }
}