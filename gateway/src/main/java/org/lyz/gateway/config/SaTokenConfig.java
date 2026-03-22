package org.lyz.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "sa-token.gateway.enabled", havingValue = "true", matchIfMissing = false)
public class SaTokenConfig {
    // Gateway passes auth to downstream services, so no need for Sa-Token interceptor here
}
