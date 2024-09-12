package com.inspur.config;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * minio相关
 * @author kliu
 * @date 2022/6/25 15:30
 */
@Data
@ConfigurationProperties(prefix = "minio")
@Component
public class MinioConfig {
    private String endpoint;
    private String accesskey;
    private String secretKey;

    /**
     * 返回minio客户端
     * @return io.minio.MinioClient
     * @author kliu
     * @date 2022/6/25 15:30
     */
    @Bean
    public MinioClient minioClient() throws InvalidPortException, InvalidEndpointException {
        MinioClient client = new MinioClient(endpoint, accesskey, secretKey);
        return client;
    }
}
