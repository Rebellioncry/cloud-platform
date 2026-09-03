package org.lyz.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_menu")
public class SysMenu {

    private String id;
    private String parentId;
    private String menuType;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private String perms;
    private Integer orderNum;
    private Integer status;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private java.util.List<SysMenu> children;
}
