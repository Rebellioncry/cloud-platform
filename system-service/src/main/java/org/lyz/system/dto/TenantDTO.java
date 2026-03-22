package org.lyz.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TenantDTO {
    private Long id;
    
    @NotBlank(message = "租户编码不能为空")
    private String tenantCode;
    
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;
    
    private String contact;
    private String mobile;
    private String email;
    private Integer status;
    private LocalDateTime expireTime;
    private String remark;
}
