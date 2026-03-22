package org.lyz.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_social")
@Schema(description = "社交账号绑定")
public class SysSocial {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "平台")
    private String platform;
    @Schema(description = "OpenID")
    private String openid;
    @Schema(description = "UnionID")
    private String unionid;
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "头像")
    private String avatar;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
