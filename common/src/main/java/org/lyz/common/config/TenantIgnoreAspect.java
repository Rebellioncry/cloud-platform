package org.lyz.common.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.lyz.common.core.context.TenantContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantIgnoreAspect {

    @Around("@annotation(TenantIgnore) || @within(TenantIgnore)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        TenantContext.setIgnoreTenant(true);
        try {
            return joinPoint.proceed();
        } finally {
            TenantContext.clearIgnoreTenant();
        }
    }
}
