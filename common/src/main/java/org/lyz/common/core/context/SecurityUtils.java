package org.lyz.common.core.context;

/**
 * @deprecated 请使用 {@link UserContext}，功能更全且支持缓存 CRUD。
 */
@Deprecated
public class SecurityUtils {

    public static boolean isSuperAdmin() {
        return UserContext.isPlatformAdmin();
    }

    public static boolean isCurrentUserId(String userId) {
        return UserContext.isCurrentUserId(userId);
    }

    public static boolean isSuperAdminOrCurrentUser(String userId) {
        return UserContext.isPlatformAdminOrCurrentUser(userId);
    }
}
