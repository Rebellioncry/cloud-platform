package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "MQTT配置DTO")
public class MqttConfigDTO {

    @Schema(description = "配置ID")
    private String id;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "Broker地址")
    private String broker;

    @Schema(description = "端口")
    private Integer port;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "客户端ID前缀")
    private String clientIdPrefix;

    @Schema(description = "共享订阅组名")
    private String sharedGroup;

    @Schema(description = "QoS")
    private Integer qos;

    @Schema(description = "心跳间隔(秒)")
    private Integer keepAlive;

    @Schema(description = "自动重连")
    private Integer autoReconnect;

    @Schema(description = "是否SSL")
    private Integer useSsl;

    @Schema(description = "状态(0已停止1运行中)")
    private Integer status;
}
