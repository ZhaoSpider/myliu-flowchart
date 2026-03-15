package com.myliu.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 请求日志切面
 * 统一记录所有Controller的请求日志
 *
 * @author MyLiu
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class RequestLogAspect {

    /**
     * 切入点：所有Controller层的方法
     */
    @Pointcut("execution(* com.myliu.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：记录请求和响应信息
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = getClientIp(request);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // 过滤掉敏感参数（如密码）
        Object[] filteredArgs = filterSensitiveArgs(args);
        
        // 记录请求日志
        log.info("[REQUEST] {} {} | IP: {} | {}.{} | Args: {}", 
                method, uri, ip, className, methodName, Arrays.toString(filteredArgs));
        
        try {
            // 执行方法
            Object result = joinPoint.proceed();
            
            // 记录响应日志
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[RESPONSE] {} {} | Cost: {}ms | Result: {}", 
                    method, uri, costTime, result != null ? result.getClass().getSimpleName() : "null");
            
            return result;
        } catch (Throwable e) {
            // 记录异常日志
            long costTime = System.currentTimeMillis() - startTime;
            log.error("[EXCEPTION] {} {} | Cost: {}ms | Error: {}", 
                    method, uri, costTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 过滤敏感参数（密码等）
     */
    private Object[] filterSensitiveArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }
        
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return null;
                    }
                    String argStr = arg.toString();
                    // 过滤密码字段
                    if (argStr.toLowerCase().contains("password") || 
                        argStr.toLowerCase().contains("pwd")) {
                        return "***FILTERED***";
                    }
                    // 限制参数长度，避免日志过长
                    if (argStr.length() > 500) {
                        return argStr.substring(0, 500) + "...(truncated)";
                    }
                    return arg;
                })
                .toArray();
    }
}
