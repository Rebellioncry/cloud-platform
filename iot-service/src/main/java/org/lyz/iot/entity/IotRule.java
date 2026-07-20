package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_rule")
@Schema(description = "IoT规则引擎")
public class IotRule extends BaseEntity {

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "规则类型(0设备触发1定时2外部触发)")
    private Integer ruleType;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "规则模型(LogicFlow JSON)")
    private String ruleModel;

    @Schema(description = "状态(0禁用1启用)")
    private Integer status;

    @Schema(description = "匹配次数")
    private Long matchCount;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;
}
