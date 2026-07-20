package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "规则DTO")
public class RuleDTO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "规则类型(0设备触发1定时2外部触发)")
    private Integer ruleType;

    @Schema(description = "规则模型(LogicFlow JSON)")
    private String ruleModel;

    @Schema(description = "状态(0禁用1启用)")
    private Integer status;
}
