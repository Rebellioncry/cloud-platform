package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "EMQX设备上下线Webhook DTO")
public class EmqxStatusDTO {

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "事件类型(client.connected/client.disconnected)")
    private String event;

    @Schema(description = "事件时间")
    private Long timestamp;

    @Schema(description = "设备IP")
    private String peername;

    @Schema(description = "连接ID")
    private Long connid;

    @Schema(description = "断开原因")
    private String reason;
}
