package org.lyz.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
@Schema(description = "租户")
public class SysTenant extends BaseEntity {
    @Schema(description = "租户编码")
    private String tenantCode;
    @Schema(description = "租户名称")
    private String tenantName;
    @Schema(description = "联系人")
    private String contact;
    @Schema(description = "联系电话")
    private String mobile;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
    @Schema(description = "备注")
    private String remark;
}
