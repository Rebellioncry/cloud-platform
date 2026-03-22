package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
@Schema(description = "菜单")
public class SysMenu extends BaseEntity {
    @Schema(description = "租户ID")
    private Long tenantId;
    @Schema(description = "父菜单ID")
    private Long parentId;
    @Schema(description = "菜单类型")
    private Integer menuType;
    @Schema(description = "菜单名称")
    private String menuName;
    @Schema(description = "路由地址")
    private String path;
    @Schema(description = "组件路径")
    private String component;
    @Schema(description = "图标")
    private String icon;
    @Schema(description = "权限标识")
    private String perms;
    @Schema(description = "排序")
    private Integer orderNum;
    @Schema(description = "是否可见")
    private Integer visible;
    @Schema(description = "状态")
    private Integer status;
}
