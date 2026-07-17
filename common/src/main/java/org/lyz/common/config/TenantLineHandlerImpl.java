package org.lyz.common.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import org.lyz.common.core.context.SecurityUtils;
import org.lyz.common.core.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Arrays;
import java.util.List;

public class TenantLineHandlerImpl implements TenantLineHandler {

    private static final List<String> IGNORE_TABLES = Arrays.asList(
            "sys_user_role",
            "sys_role_menu",
            "sys_tenant"
    );

    @Override
    public Expression getTenantId() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            return null;
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
        if (SecurityUtils.isSuperAdmin()) {
            return true;
        }
        return IGNORE_TABLES.contains(tableName);
    }
}
