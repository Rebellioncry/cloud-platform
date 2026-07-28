package org.lyz.common.core.context;

import lombok.extern.slf4j.Slf4j;

import java.util.Stack;

/**
 * 租户助手工具类 — RuoYi-Plus 风格。
 * <p>
 * 提供线程级别的动态租户切换和租户忽略功能。
 * <ul>
 *   <li>{@link #dynamic(String, Runnable)} — 临时切换到指定租户执行操作，完成后自动恢复</li>
 *   <li>{@link #ignore(Runnable)} — 忽略租户过滤，查询所有租户的数据</li>
 *   <li>{@link #getTenantId()} — 获取当前有效租户ID（动态 > 请求头 > 默认值）</li>
 * </ul>
 */
@Slf4j
public final class TenantHelper {

    private static final String DEFAULT_TENANT_ID = TenantConstants.DEFAULT_TENANT_ID;

    private TenantHelper() {
    }

    // ==================== 动态租户切换 ====================

    /**
     * 获取当前有效的租户ID。
     * <p>
     * 优先级：动态租户(ThreadLocal) > 请求头租户(TenantContext) > 默认租户ID
     */
    public static String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            return DEFAULT_TENANT_ID;
        }
        return tenantId;
    }

    /**
     * 设置动态租户ID（用于超管临时切换租户身份）。
     */
    public static void setDynamic(String tenantId) {
        TenantContext.setTenantId(tenantId);
    }

    /**
     * 清除动态租户ID。
     */
    public static void clearDynamic() {
        TenantContext.clear();
    }

    /**
     * 在指定租户上下文中执行操作，执行完成后自动恢复原租户。
     *
     * @param tenantId 目标租户ID
     * @param handle   要执行的逻辑
     */
    public static void dynamic(String tenantId, Runnable handle) {
        String originalTenantId = TenantContext.getTenantId();
        String originalUserId = TenantContext.getUserId();
        String originalUsername = TenantContext.getUsername();
        String originalScope = TenantContext.getTenantScope();
        try {
            TenantContext.setTenantId(tenantId);
            handle.run();
        } finally {
            if (originalTenantId != null) {
                TenantContext.setTenantId(originalTenantId);
            } else {
                TenantContext.clear();
            }
            if (originalUserId != null) {
                TenantContext.setUserId(originalUserId);
            }
            if (originalUsername != null) {
                TenantContext.setUsername(originalUsername);
            }
            if (originalScope != null) {
                TenantContext.setTenantScope(originalScope);
            }
        }
    }

    // ==================== 租户忽略 ====================

    /**
     * 开启租户忽略（所有数据库查询跳过 tenant_id 过滤）。
     */
    public static void enableIgnore() {
        TenantContext.setIgnoreTenant(true);
    }

    /**
     * 关闭租户忽略。
     */
    public static void disableIgnore() {
        TenantContext.setIgnoreTenant(false);
    }

    /**
     * 判断当前是否处于忽略租户状态。
     */
    public static boolean isIgnore() {
        return TenantContext.isIgnoreTenant();
    }

    /**
     * 在忽略租户过滤的上下文中执行操作，完成后自动恢复。
     *
     * @param handle 要执行的逻辑
     */
    public static void ignore(Runnable handle) {
        boolean wasIgnore = isIgnore();
        try {
            enableIgnore();
            handle.run();
        } finally {
            if (!wasIgnore) {
                disableIgnore();
            }
        }
    }

    // ==================== 便捷判断 ====================

    /**
     * 当前用户是否为平台管理员。
     */
    public static boolean isPlatformAdmin() {
        return UserContext.isPlatformAdmin();
    }
}
