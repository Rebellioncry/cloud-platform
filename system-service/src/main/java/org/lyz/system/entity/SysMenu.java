package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    private Long tenantId;
    private Long parentId;
    private Integer menuType;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private String perms;
    private Integer orderNum;
    private Integer visible;
    private Integer status;
}
