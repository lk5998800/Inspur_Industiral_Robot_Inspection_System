package com.inspur.config;

import com.inspur.interceptor.SpecialCharInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 过滤特殊字符
 * @author kliu
 * @date 2022/8/30 11:16
 */
@Configuration
public class SpecialCharConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(registry!=null){
            InterceptorRegistration interceptorRegistration = registry.addInterceptor(new SpecialCharInterceptor());
            interceptorRegistration.addPathPatterns("/**");
            interceptorRegistration.excludePathPatterns("/**/login","/v2/api-docs", "/swagger-resources/configuration/ui",
                    "/swagger-resources", "/swagger-resources/configuration/security", "/swagger-ui.html", "/webjars/**","/**/changePwd");
        }
    }
}