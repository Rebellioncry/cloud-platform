package org.lyz.common.config;

import java.lang.annotation.*;

/**
 * 标注此注解的方法（或类）内部的所有数据库查询将跳过租户过滤。
 * 适用于：启动任务、MQTT消息处理等无用户上下文的场景。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantIgnore {
}
