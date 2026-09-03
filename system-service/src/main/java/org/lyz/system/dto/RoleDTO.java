package org.lyz.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "角色DTO")
public class RoleDTO {
    @Schema(description = "主键ID")
    private String id;
    
    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;
    
    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;
    
    @Schema(description = "排序")
    private Integer roleSort;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "数据范围 1=全部 5=仅本人")
    private Integer dataScope;
    @Schema(description = "租户ID")
    private String tenantId;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "菜单ID列表")
    private List<String> menuIds;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
