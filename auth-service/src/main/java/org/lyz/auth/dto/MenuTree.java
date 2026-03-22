package org.lyz.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuTree {
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private String perms;
    private Integer menuType;
    private List<MenuTree> children;
}
