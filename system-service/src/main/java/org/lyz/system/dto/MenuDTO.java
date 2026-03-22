package org.lyz.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "菜单DTO")
public class MenuDTO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "父菜单ID")
    private Long parentId;
    
    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuName;
    
    @Schema(description = "菜单类型")
    private Integer menuType;
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
    
    @Schema(description = "子菜单")
    private List<MenuDTO> children = new ArrayList<>();
}
