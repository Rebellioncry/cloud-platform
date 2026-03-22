package org.lyz.gateway.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SaTokenConfig {
    // Gateway 目前不做认证，认证由下游服务处理
    // 如需启用 Gateway 认证，需要配置 Sa-Token JWT 模式
}
