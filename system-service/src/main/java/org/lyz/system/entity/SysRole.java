package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    private Long tenantId;
    private String roleCode;
    private String roleName;
    private Integer roleSort;
    private Integer status;
    private Integer dataScope;
    private String remark;
}
