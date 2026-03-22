package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {
    private String tenantCode;
    private String tenantName;
    private String contact;
    private String mobile;
    private String email;
    private Integer status;
    private LocalDateTime expireTime;
    private String remark;
}
