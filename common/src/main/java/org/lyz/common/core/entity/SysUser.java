package org.lyz.common.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser extends BaseEntity {
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String mobile;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "状态")
    private Integer status;
}
