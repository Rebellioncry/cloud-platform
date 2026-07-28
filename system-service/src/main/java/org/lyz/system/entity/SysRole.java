package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "角色")
public class SysRole extends BaseEntity {
    @Schema(description = "租户ID")
    private String tenantId;
    @Schema(description = "角色作用域: PLATFORM=平台角色, TENANT=租户角色")
    private String scope;
    @Schema(description = "系统内置: 0=否, 1=是")
    private Integer isSystem;
    @Schema(description = "角色编码")
    private String roleCode;
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "排序")
    private Integer roleSort;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "数据范围")
    private Integer dataScope;
    @Schema(description = "备注")
    private String remark;
}
