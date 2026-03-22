package org.lyz.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "租户DTO")
public class TenantDTO {
    @Schema(description = "主键ID")
    private Long id;
    
    @NotBlank(message = "租户编码不能为空")
    @Schema(description = "租户编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantCode;
    
    @NotBlank(message = "租户名称不能为空")
    @Schema(description = "租户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tenantName;
    
    @Schema(description = "联系人")
    private String contact;
    @Schema(description = "联系电话")
    private String mobile;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
    @Schema(description = "备注")
    private String remark;
}
