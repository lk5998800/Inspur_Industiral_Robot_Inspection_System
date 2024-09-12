package com.inspur.config;

import com.inspur.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 权限校验配置
 * @author kliu
 * @return a
 * @date 2022/5/24 19:36
 */
@Configuration
public class LoginAuthConfig implements WebMvcConfigurer {
    /**
     * 不拦截的url
     * @param registry
     * @return void
     * @author kliu
     * @date 2022/5/24 19:36
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册Interceptor拦截器
        InterceptorRegistration registration = registry.addInterceptor(authInterceptor());
        //所有路径都被拦截
        registration.addPathPatterns("/**");
        registration.excludePathPatterns("/**/login","/v2/api-docs", "/swagger-resources/configuration/ui",
                "/swagger-resources", "/swagger-resources/configuration/security",
                "/swagger-ui.html", "/webjars/**","/**/receiveDetectionResult","/**/receivePosture","/**/receiveStreamStartResult",
                "/**/receiveRobotSysSetResult","/**/receiveTerminateResult","/**/receiveRobotStatus","/**/error","/**/csrf","/",
                "/**/startold","/**/heartold","/**/start","/**/heart","/**/getcamaraparam","/**/getCamaraParam",
                "/**/getRecentTaskBasic","/**/getAbnormalCountRecentDays7","/**/getRecentTaskCabinetsInfo","/**/getRecentTaskWarnInfo",
                "/**/getRobotRunStatusInfo", "/**/listWithoutToken", "/**/industrialRobotEndTask", "/**/receiveAlongWorkDetl", "/**/pedestrianDetectionAlarmInformation",
                "/**/invokeGating","/**/pedestrianDetectionAlarmInformation","/**/getAlongWorkDetlForRobot","/**/receiveAlongWorkPoints","/**/associatedAlongWorkPoint",
                "/**/receiveItAssetResult","/**/lifterTest","/**/testAlarmLight","/**/dciminvoke","/**/websocket/**","/**/rebootCan/","/**/clearCache/","/**/upload",
                "/**/getBackChargingPilePath","/**/getBackToPointNamePath","/industrial_robot/remotecontrol/liftResult","/industrial_robot/taskinstance/updateTaskStatus"
                ,"/industrial_robot/userinfo/getByRobotIdPersonList","/industrial_robot/robotstatus/pileReturnFailure",
                "/industrial_robot/gating/invokeOpenDoor","/industrial_robot/gating/invokeCloseDoor", "/**/taskinspect/**","/industrial_robot/uniappPhoto/add");
    }

    /**
     * 拦截器bean
     * @return com.inspur.interceptor.AuthInterceptor
     * @author kliu
     * @date 2022/5/24 19:37
     */
    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }
}
