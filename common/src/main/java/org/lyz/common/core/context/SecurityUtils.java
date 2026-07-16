package org.lyz.common.core.context;

import cn.dev33.satoken.stp.StpUtil;

import java.util.List;

public class SecurityUtils {

    public static boolean isSuperAdmin() {
        List<String> roleCodes = getRoleCodes();
        return roleCodes != null && roleCodes.contains("SUPER_ADMIN");
    }

    public static boolean isCurrentUserId(String userId) {
        String currentUserId = StpUtil.getLoginIdAsString();
        return currentUserId != null && currentUserId.equals(userId);
    }

    public static boolean isSuperAdminOrCurrentUser(String userId) {
        return isSuperAdmin() || isCurrentUserId(userId);
    }

    @SuppressWarnings("unchecked")
    private static List<String> getRoleCodes() {
        return (List<String>) StpUtil.getSession().get("roleCodes");
    }
}
