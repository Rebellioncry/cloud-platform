package org.lyz.common.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.context.TenantConstants;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.context.LoginHelper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Set;

/**
 * MyBatis-Plus 多租户 SQL 拦截处理器 — RuoYi-Plus 风格。
 * <p>
 * 自动为所有带 tenant_id 的表追加 WHERE tenant_id = ? 条件。
 * 以下表跳过过滤：
 * <ul>
 *   <li>系统关联表：sys_user_role, sys_role_menu, sys_social</li>
 *   <li>系统管理表：sys_tenant, sys_tenant_package, sys_menu, sys_role, sys_user</li>
 *   <li>租户忽略模式：TenantContext.isIgnoreTenant() 为 true</li>
 *   <li>超级管理员：LoginHelper.isSuperAdmin() 为 true</li>
 * </ul>
 */
@Slf4j
public class TenantLineHandlerImpl implements TenantLineHandler {

    /**
     * 系统固定排除表（硬编码，防止误配置）。
     * 这些表不参与租户级别的数据隔离，由业务代码自行处理。
     */
    private static final Set<String> SYSTEM_EXCLUDE_TABLES = Set.of(
            "sys_user_role",        // 用户角色关联表
            "sys_role_menu",        // 角色菜单关联表
            "sys_social",           // 第三方登录关联表
            "sys_tenant",           // 租户管理表
            "sys_tenant_package",   // 租户套餐表
            "sys_menu",             // 菜单表（所有租户共享）
            "sys_role",             // 角色表
            "sys_user"              // 用户表
    );

    @Override
    public Expression getTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            return new StringValue(TenantConstants.DEFAULT_TENANT_ID);
        }
        return new StringValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (TenantContext.isIgnoreTenant()) {
            return true;
        }
        if (LoginHelper.isSuperAdmin()) {
            return true;
        }
        return SYSTEM_EXCLUDE_TABLES.contains(tableName.toLowerCase());
    }

}
