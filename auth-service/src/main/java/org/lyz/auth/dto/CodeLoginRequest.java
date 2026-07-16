package org.lyz.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "验证码登录请求")
public class CodeLoginRequest {
    @NotBlank(message = "类型不能为空")
    @Schema(description = "类型: sms(短信) / email(邮箱)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "租户ID")
    private String tenantId;
}
