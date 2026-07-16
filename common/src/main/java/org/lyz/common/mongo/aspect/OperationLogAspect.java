package org.lyz.common.mongo.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.lyz.common.mongo.entity.OperationLog;
import org.lyz.common.mongo.service.OperationLogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Around("execution(* org.lyz.*.controller.*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLog opLog = new OperationLog();

        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                opLog.setUrl(request.getRequestURI());
                opLog.setHttpMethod(request.getMethod());
                opLog.setIp(getClientIp(request));
                opLog.setUserAgent(request.getHeader("User-Agent"));
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            opLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
            opLog.setParams(buildParams(joinPoint));

            try {
                if (StpUtil.isLogin()) {
                    opLog.setUserId(StpUtil.getLoginIdAsString());
                    Object username = StpUtil.getSession().get("username");
                    if (username != null) {
                        opLog.setUsername(username.toString());
                    }
                    Object tenantId = StpUtil.getSession().get("tenantId");
                    if (tenantId != null) {
                        opLog.setTenantId(tenantId.toString());
                    }
                }
            } catch (Exception ignored) {
            }

            String className = joinPoint.getTarget().getClass().getSimpleName();
            opLog.setModule(resolveModule(className));

            Object result = joinPoint.proceed();
            opLog.setStatus(1);
            opLog.setResult(truncate(objectMapper.writeValueAsString(result), 2000));
            return result;
        } catch (Throwable ex) {
            opLog.setStatus(0);
            opLog.setErrorMsg(truncate(ex.getMessage(), 2000));
            throw ex;
        } finally {
            opLog.setDuration(System.currentTimeMillis() - startTime);
            opLog.setCreateTime(LocalDateTime.now());
            operationLogService.save(opLog);
        }
    }

    private Map<String, Object> buildParams(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (args[i] instanceof HttpServletRequest) continue;
                try {
                    params.put(names[i], objectMapper.readValue(
                            objectMapper.writeValueAsString(args[i]), Object.class));
                } catch (Exception e) {
                    params.put(names[i], String.valueOf(args[i]));
                }
            }
        }
        return params;
    }

    private String resolveModule(String className) {
        if (className.toLowerCase().contains("product")) return "产品管理";
        if (className.toLowerCase().contains("device")) return "设备管理";
        if (className.toLowerCase().contains("mqtt")) return "MQTT配置";
        if (className.toLowerCase().contains("user")) return "用户管理";
        if (className.toLowerCase().contains("role")) return "角色管理";
        if (className.toLowerCase().contains("menu")) return "菜单管理";
        if (className.toLowerCase().contains("tenant")) return "租户管理";
        if (className.toLowerCase().contains("auth")) return "认证";
        if (className.toLowerCase().contains("dashboard")) return "看板";
        if (className.toLowerCase().contains("emqx")) return "EMQX";
        return className;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}
