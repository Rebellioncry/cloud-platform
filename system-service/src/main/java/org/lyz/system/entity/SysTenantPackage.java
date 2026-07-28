package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant_package")
@Schema(description = "租户套餐")
public class SysTenantPackage extends BaseEntity {
    @Schema(description = "套餐名称")
    private String packageName;
    @Schema(description = "关联的菜单ID(逗号分隔)")
    private String menuIds;
    @Schema(description = "状态 1正常 0停用")
    private Integer status;
    @Schema(description = "备注")
    private String remark;
}
