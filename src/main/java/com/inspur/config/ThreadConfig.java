package com.inspur.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池创建
 * @author: kliu
 * @date: 2022/4/18 20:13
 */
@Configuration
public class ThreadConfig {
    /**
     * 线程池创建
     * @return java.util.concurrent.ThreadPoolExecutor
     * @author kliu
     * @date 2022/5/24 19:39
     */
    @Bean
    public ThreadPoolExecutor executor(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("analysedetection-pool-%d").build();
        return new ThreadPoolExecutor(4, 200,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }
}
