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
    @Schema(description = "父菜单ID")
    private String parentId;
    @Schema(description = "菜单类型: M=目录 C=菜单 F=按钮")
    private String menuType;
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
    @Schema(description = "是否可见 0显示 1隐藏")
    private Integer visible;
    @Schema(description = "状态 0正常 1停用")
    private Integer status;
    @Schema(description = "是否为外链 Y是 N否")
    private String isFrame;
    @Schema(description = "是否缓存 Y缓存 N不缓存")
    private String isCache;
    @Schema(description = "激活菜单路径")
    private String activeMenu;
    @Schema(description = "路由参数")
    private String queryParam;
}
