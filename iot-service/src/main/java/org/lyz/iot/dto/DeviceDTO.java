package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备DTO")
public class DeviceDTO {

    @Schema(description = "设备ID")
    private String id;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备密钥")
    private String deviceKey;

    @Schema(description = "设备昵称")
    private String nickname;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @Schema(description = "设备标签(JSON)")
    private String tags;

    @Schema(description = "父设备ID")
    private String parentDeviceId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
