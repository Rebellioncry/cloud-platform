package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联")
public class SysUserRole {
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键ID")
    private String id;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "角色ID")
    private String roleId;
}
