package org.lyz.common.core.context;

import cn.dev33.satoken.stp.StpUtil;
import org.lyz.common.core.entity.SysUser;

import java.util.Collections;
import java.util.List;

/**
 * 用户上下文工具类 — 统一获取当前登录用户信息。
 * <p>
 * 读取优先级：TenantContext (ThreadLocal, 请求级缓存) → Sa-Token Session (Redis, 持久缓存)
 * <p>
 * Session CRUD：
 * <ul>
 *   <li>C (创建)：LoginServiceImpl.writeSessionData() 登录时写入（含完整用户对象，密码已剔除）</li>
 *   <li>R (读取)：本工具类的 getXxx() / getUser() 方法</li>
 *   <li>U (更新)：refreshUser() / updateSession() 用户信息或角色变更后刷新</li>
 *   <li>D (删除)：登出时 StpUtil.logout() 自动清除</li>
 * </ul>
 */
public final class UserContext {

    private UserContext() {
    }

    // ==================== 完整用户对象 ====================

    /**
     * 获取当前登录用户的完整信息（不含密码）。
     * 从 Sa-Token Session 中读取缓存的 SysUser 对象。
     */
    public static SysUser getUser() {
        return safeGet(() -> (SysUser) StpUtil.getSession().get("user"));
    }

    /**
     * 刷新 Session 中缓存的用户对象。
     * 用户资料修改、角色变更后调用，保持缓存与数据库一致。
     */
    public static void refreshUser(SysUser user) {
        if (user == null) {
            return;
        }
        SysUser safeUser = new SysUser();
        safeUser.setId(user.getId());
        safeUser.setTenantId(user.getTenantId());
        safeUser.setUsername(user.getUsername());
        safeUser.setNickname(user.getNickname());
        safeUser.setEmail(user.getEmail());
        safeUser.setMobile(user.getMobile());
        safeUser.setAvatar(user.getAvatar());
        safeUser.setStatus(user.getStatus());
        safeUser.setCreateTime(user.getCreateTime());
        updateSession("user", safeUser);
        updateSession("userId", user.getId());
        updateSession("username", user.getUsername());
        updateSession("tenantId", user.getTenantId());
    }

    /**
     * 刷新 Session 中缓存的角色列表。
     * 角色分配后调用。
     */
    public static void refreshRoles(List<String> roleCodes) {
        updateSession("roleCodes", roleCodes != null ? roleCodes : Collections.emptyList());
    }

    // ==================== 读取（优先 TenantContext，回退 Sa-Token Session） ====================

    public static String getUserId() {
        String id = TenantContext.getUserId();
        if (id != null && !id.isEmpty()) {
            return id;
        }
        return safeGet(() -> StpUtil.getSession().getString("userId"));
    }

    public static String getUsername() {
        String name = TenantContext.getUsername();
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return safeGet(() -> StpUtil.getSession().getString("username"));
    }

    public static String getTenantId() {
        String tid = TenantContext.getTenantId();
        if (tid != null && !tid.isEmpty()) {
            return tid;
        }
        return safeGet(() -> StpUtil.getSession().getString("tenantId"));
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRoleCodes() {
        List<String> roles = safeGet(() ->
                (List<String>) StpUtil.getSession().get("roleCodes"));
        return roles != null ? roles : Collections.emptyList();
    }

    // ==================== 便捷判断 ====================

    public static boolean isSuperAdmin() {
        return getRoleCodes().contains("SUPER_ADMIN");
    }

    public static boolean hasRole(String roleCode) {
        return getRoleCodes().contains(roleCode);
    }

    public static boolean isCurrentUserId(String userId) {
        String currentId = getUserId();
        return currentId != null && currentId.equals(userId);
    }

    public static boolean isSuperAdminOrCurrentUser(String userId) {
        return isSuperAdmin() || isCurrentUserId(userId);
    }

    // ==================== Session CRUD（缓存管理） ====================

    /**
     * 刷新本地缓存：清空 TenantContext，
     * 下次 getXxx() 会从 Sa-Token Session 重新读取。
     */
    public static void refreshLocalCache() {
        TenantContext.clear();
    }

    /**
     * 更新 Sa-Token Session 中的指定字段。
     */
    public static void updateSession(String key, Object value) {
        try {
            StpUtil.getSession().set(key, value);
        } catch (Exception ignored) {
        }
    }

    /**
     * 移除 Sa-Token Session 中的指定字段。
     */
    public static void removeSession(String key) {
        try {
            StpUtil.getSession().delete(key);
        } catch (Exception ignored) {
        }
    }

    // ==================== 内部工具 ====================

    @FunctionalInterface
    private interface SessionGetter<T> {
        T get();
    }

    private static <T> T safeGet(SessionGetter<T> getter) {
        try {
            return getter.get();
        } catch (Exception e) {
            return null;
        }
    }
}
