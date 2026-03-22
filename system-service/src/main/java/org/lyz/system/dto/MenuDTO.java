package org.lyz.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuDTO {
    private Long id;
    private Long parentId;
    
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    
    private Integer menuType;
    private String path;
    private String component;
    private String icon;
    private String perms;
    private Integer orderNum;
    private Integer visible;
    private Integer status;
    
    private List<MenuDTO> children = new ArrayList<>();
}
