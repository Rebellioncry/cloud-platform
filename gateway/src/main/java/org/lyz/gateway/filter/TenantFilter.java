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
    private static final String DEFAULT_TENANT_ID = "1";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String tenantId = request.getHeaders().getFirst(TENANT_ID_HEADER);
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = DEFAULT_TENANT_ID;
        }

        ServerHttpRequest.Builder builder = request.mutate()
                .header(TENANT_ID_HEADER, tenantId);

        try {
            if (StpUtil.isLogin()) {
                String userId = StpUtil.getLoginIdAsString();
                builder.header(USER_ID_HEADER, userId);

                Object username = StpUtil.getSession().get("username");
                if (username != null) {
                    builder.header(USERNAME_HEADER, username.toString());
                }
            }
        } catch (Exception ignored) {
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
