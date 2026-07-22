package com.realestate.backend.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class RequestLoggingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        String httpInfo = currentRequestInfo();
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            log.info("{} -> {} completed in {}ms", httpInfo, method, System.currentTimeMillis() - start);
            return result;
        } catch (Exception ex) {
            log.warn("{} -> {} failed after {}ms: {}", httpInfo, method,
                    System.currentTimeMillis() - start, ex.getMessage());
            throw ex;
        }
    }

    private String currentRequestInfo() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "N/A";
        return attrs.getRequest().getMethod() + " " + attrs.getRequest().getRequestURI();
    }
}