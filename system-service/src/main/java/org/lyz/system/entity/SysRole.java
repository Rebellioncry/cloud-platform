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
    private Long tenantId;
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
