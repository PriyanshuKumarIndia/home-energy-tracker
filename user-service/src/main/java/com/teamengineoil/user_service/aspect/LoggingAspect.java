package com.teamengineoil.user_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.teamengineoil.user_service.service.*.*(..))")
    public void servicePointcut() {
    }

    @Before("servicePointcut()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Called service method: {} with args: {}", joinPoint.getSignature().getName(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "servicePointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Called service method: {} with result: {}", joinPoint.getSignature().getName(), result);
    }
}
