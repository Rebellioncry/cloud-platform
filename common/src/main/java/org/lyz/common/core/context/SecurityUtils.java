package org.lyz.common.core.context;

import java.util.List;

/**
 * @deprecated 请使用 {@link UserContext}，功能更全且支持缓存 CRUD。
 */
@Deprecated
public class SecurityUtils {

    public static boolean isSuperAdmin() {
        return UserContext.isSuperAdmin();
    }

    public static boolean isCurrentUserId(String userId) {
        return UserContext.isCurrentUserId(userId);
    }

    public static boolean isSuperAdminOrCurrentUser(String userId) {
        return UserContext.isSuperAdminOrCurrentUser(userId);
    }

    @SuppressWarnings("unchecked")
    private static List<String> getRoleCodes() {
        return UserContext.getRoleCodes();
    }
}
