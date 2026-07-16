package org.lyz.iot.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备遥测数据(TDengine)")
public class IotDeviceTelemetry {

    @Schema(description = "时间戳")
    private LocalDateTime ts;

    @Schema(description = "值")
    private String value;

    @Schema(description = "值类型")
    private String valueType;

    @Schema(description = "质量标记")
    private String quality;

    @Schema(description = "设备ID(tag)")
    private String deviceId;

    @Schema(description = "产品密钥(tag)")
    private String productKey;

    @Schema(description = "属性标识符(tag)")
    private String propertyId;

    @Schema(description = "设备名称(tag)")
    private String deviceName;

    @Schema(description = "产品名称(tag)")
    private String productName;
}
