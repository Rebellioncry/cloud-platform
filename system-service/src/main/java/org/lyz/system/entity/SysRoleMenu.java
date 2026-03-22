package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("sys_role_menu")
@Schema(description = "角色菜单关联")
public class SysRoleMenu {
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "角色ID")
    private Long roleId;
    @Schema(description = "菜单ID")
    private Long menuId;
}
