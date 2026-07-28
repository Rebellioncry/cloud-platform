package org.lyz.gateway.filter;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TenantFilter implements GlobalFilter, Ordered {

    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";
    private static final String TENANT_SCOPE_HEADER = "X-Tenant-Scope";
    private static final String DEFAULT_TENANT_ID = "";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String tenantId = request.getHeaders().getFirst(TENANT_ID_HEADER);
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT_ID;
        }

        String userId = null;
        String username = null;
        String tenantScope = null;

        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsString();

                Object usernameObj = StpUtil.getSession().get("username");
                if (usernameObj != null) {
                    username = usernameObj.toString();
                }

                Object tenantScopeObj = StpUtil.getSession().get("tenantScope");
                if (tenantScopeObj != null) {
                    tenantScope = tenantScopeObj.toString();
                }

                Object tenantIdObj = StpUtil.getSession().get("tenantId");
                if (tenantIdObj != null && !tenantIdObj.toString().isEmpty()) {
                    tenantId = tenantIdObj.toString();
                }
            }
        } catch (Exception ignored) {
        }

        ServerHttpRequest.Builder builder = request.mutate()
                .header(TENANT_ID_HEADER, tenantId);
        if (userId != null) {
            builder.header(USER_ID_HEADER, userId);
        }
        if (username != null) {
            builder.header(USERNAME_HEADER, username);
        }
        if (tenantScope != null) {
            builder.header(TENANT_SCOPE_HEADER, tenantScope);
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
