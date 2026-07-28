package org.lyz.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_tenant_package")
public class SysTenantPackage {
    private String id;
    private String packageName;
    private String menuIds;
    private Integer status;
}
