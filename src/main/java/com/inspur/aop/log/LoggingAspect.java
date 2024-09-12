package com.inspur.aop.log;

import com.inspur.code.RuntimeEnvironmentStatus;
import com.inspur.config.LogConfig;
import com.inspur.industrialinspection.service.RequestService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

/**
 * 日志记录切面
 * @author kliu
 * @date 2022/5/24 17:49
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Autowired
    private RequestService requestService;

    @Autowired
    private LogConfig logConfig;

    private final Environment env;

    public LoggingAspect(Environment env) {
        this.env = env;
    }

    @Pointcut("execution(* com.inspur.industrialinspection.web.*.*(..))")
    public void controllerPointCut() {
    }

    @Pointcut("execution(* com.inspur.industrialinspection.service.*.*(..))")
    public void servicePointCut() {
    }

    /**
     * 记录方法抛出异常的通知
     * @param joinPoint
     * @param e
     * @return void
     * @author kliu
     * @date 2022/5/24 19:33
     */
    @AfterThrowing(value = "controllerPointCut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        long userId = requestService.getUserIdByToken();
        // 判断环境，dev、test or prod
        if (env.acceptsProfiles(Profiles.of(RuntimeEnvironmentStatus.DEV, RuntimeEnvironmentStatus.TEST))) {
            log.error("Userid:{} Exception in {}.{}() with cause = '{}' and exception = '{}'", userId, joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), e.getCause() != null? e.getCause() : "NULL", e.getMessage(), e);
        } else {
            log.error("Userid:{} Exception in {}.{}() with cause = {}", userId, joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), e.getCause() != null? e.getCause() : e.getMessage());
        }
    }

    /**
     * 在方法进入和退出时记录日志的通知
     * @param joinPoint
     * @return java.lang.Object
     * @author kliu
     * @date 2022/5/24 19:34
     */
    @Around(value = "controllerPointCut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long endTime;
        long userId = 0;
        long startTime = 0;
        if (logConfig.getNotRecordingMethodStr().indexOf(joinPoint.getSignature().getName())==-1){
            userId = requestService.getUserIdByToken();
            startTime = System.currentTimeMillis();
            log.info("Userid:{} Enter: {}.{}() with argument[s] = {}", userId, joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            if (logConfig.getNotRecordingMethodStr().indexOf(joinPoint.getSignature().getName())==-1){
                endTime = System.currentTimeMillis();
                log.info("Userid:{} Exit: {}.{}() with result = {} 耗时{}ms", userId, joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), result, endTime-startTime);
            }
            return result;
        } catch (Exception e) {
            throw e;
        }

    }

}