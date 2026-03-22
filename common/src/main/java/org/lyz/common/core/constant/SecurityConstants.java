package org.lyz.common.core.constant;

public interface SecurityConstants {
    String TOKEN_PREFIX = "Bearer ";
    String TOKEN_HEADER = "Authorization";
    String TENANT_ID_HEADER = "X-Tenant-Id";
    String USER_ID_HEADER = "X-User-Id";
    String USER_TYPE_HEADER = "X-User-Type";

    String JWT_SECRET = "cloud-platform-jwt-secret-key-2026-lyz";
    long JWT_EXPIRATION = 86400000L;

    int DEFAULT_PAGE_NUM = 1;
    int DEFAULT_PAGE_SIZE = 10;
    int MAX_PAGE_SIZE = 100;
}
