package org.lyz.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_tenant")
public class SysTenant {
    private String id;
    private String tenantCode;
    private String tenantName;
    private String packageId;
    private Integer status;
}
