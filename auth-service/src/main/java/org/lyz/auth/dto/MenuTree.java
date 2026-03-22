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
@Schema(description = "菜单树")
public class MenuTree {
    @Schema(description = "菜单ID")
    private Long id;
    @Schema(description = "父菜单ID")
    private Long parentId;
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
    @Schema(description = "菜单类型")
    private Integer menuType;
    @Schema(description = "子菜单")
    private List<MenuTree> children;
}
