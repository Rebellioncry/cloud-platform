package org.lyz.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserInfo {
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "角色列表")
    private List<String> roles;
    @Schema(description = "菜单列表")
    private List<MenuTree> menus;
}
