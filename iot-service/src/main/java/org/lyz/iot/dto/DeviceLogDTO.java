package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备日志DTO")
public class DeviceLogDTO {

    @Schema(description = "日志类型")
    private String logType;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "时间戳")
    private LocalDateTime timestamp;
}
