package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "设备功能DTO")
public class DeviceFunctionDTO {

    @Schema(description = "功能标识符")
    private String identifier;

    @Schema(description = "功能名称")
    private String name;

    @Schema(description = "功能类型(property/service/event)")
    private String type;

    @Schema(description = "数据类型(int/float/double/bool/string/enum/date/text/struct)")
    private String dataType;

    @Schema(description = "数据类型规格(JSON)")
    private String specs;

    @Schema(description = "访问模式(r/rw)")
    private String accessMode;

    @Schema(description = "调用类型(async/sync) - 仅service类型")
    private String callType;
}
