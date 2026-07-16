package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "设备运行状态DTO")
public class DeviceStatusDTO {

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "产品密钥")
    private String productKey;

    @Schema(description = "设备状态(0未激活1在线2离线3已禁用)")
    private Integer status;

    @Schema(description = "最后上线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "设备IP")
    private String ipAddress;

    @Schema(description = "属性最新值(key=属性标识符, value=最新值)")
    private Map<String, PropertyLatest> properties;

    @Data
    @Schema(description = "属性最新值")
    public static class PropertyLatest {
        @Schema(description = "属性标识符")
        private String identifier;

        @Schema(description = "属性值")
        private String value;

        @Schema(description = "值类型")
        private String valueType;

        @Schema(description = "上报时间")
        private LocalDateTime timestamp;
    }
}
