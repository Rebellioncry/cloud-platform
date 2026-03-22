package org.lyz.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {
    @Schema(description = "Token")
    private String token;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "过期时间")
    private Long expireTime;
}
