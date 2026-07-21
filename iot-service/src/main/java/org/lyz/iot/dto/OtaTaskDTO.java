package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "OTA升级任务DTO")
public class OtaTaskDTO {
    @Schema(description = "ID(编辑时传)")
    private String id;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "固件ID")
    private String firmwareId;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "目标类型(0产品全量 1指定设备 2按版本)")
    private Integer targetType;

    @Schema(description = "目标值(JSON数组设备ID列表/版本号字符串)")
    private String targetValue;
}
