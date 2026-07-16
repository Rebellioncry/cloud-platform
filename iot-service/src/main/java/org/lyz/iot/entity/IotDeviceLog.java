package org.lyz.iot.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备日志(TDengine)")
public class IotDeviceLog {

    @Schema(description = "时间戳")
    private LocalDateTime ts;

    @Schema(description = "日志类型")
    private String logType;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "设备ID(tag)")
    private String deviceId;

    @Schema(description = "产品密钥(tag)")
    private String productKey;

    @Schema(description = "设备名称(tag)")
    private String deviceName;
}
