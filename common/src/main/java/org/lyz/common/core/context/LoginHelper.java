package org.lyz.common.core.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    private static final String SUPER_ADMIN_USER_ID = "2";

    public static boolean isSuperAdmin() {
        String userId = UserContext.getUserId();
        return SUPER_ADMIN_USER_ID.equals(userId);
    }

    public static boolean isSuperAdmin(String userId) {
        return SUPER_ADMIN_USER_ID.equals(userId);
    }
}
