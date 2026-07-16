package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备影子DTO")
public class DeviceShadowDTO {

    @Schema(description = "属性标识符")
    private String identifier;

    @Schema(description = "期望值(JSON)")
    private String desiredValue;

    @Schema(description = "期望版本")
    private Long desiredVersion;

    @Schema(description = "期望设置时间")
    private LocalDateTime desiredTime;

    @Schema(description = "上报值(JSON)")
    private String reportedValue;

    @Schema(description = "上报版本")
    private Long reportedVersion;

    @Schema(description = "上报时间")
    private LocalDateTime reportedTime;

    @Schema(description = "是否存在差异(desired != reported)")
    private Boolean hasDiff;
}
