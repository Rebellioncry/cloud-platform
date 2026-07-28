package org.lyz.common.core.context;

/**
 * 租户常量 — RuoYi-Plus 风格。
 * <p>
 * 统一管理多租户体系中的关键常量，避免硬编码分散在各处。
 */
public interface TenantConstants {

    /** 默认租户ID（空字符串表示平台级别，不属于任何租户） */
    String DEFAULT_TENANT_ID = "";

    /** 平台管理员角色编码 */
    String SUPER_ADMIN_ROLE_KEY = "SUPER_ADMIN";

    /** 租户管理员角色编码 */
    String TENANT_ADMIN_ROLE_KEY = "ADMIN";

    /** 租户管理员角色名称 */
    String TENANT_ADMIN_ROLE_NAME = "管理员";

    /** 平台作用域 */
    String SCOPE_PLATFORM = "PLATFORM";

    /** 租户作用域 */
    String SCOPE_TENANT = "TENANT";
}
