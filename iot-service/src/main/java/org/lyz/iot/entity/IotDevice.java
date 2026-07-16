package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_device")
@Schema(description = "IoT设备")
public class IotDevice extends BaseEntity {

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "产品密钥")
    private String productKey;

    @Schema(description = "父设备ID")
    private String parentDeviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备密钥")
    private String deviceKey;

    @Schema(description = "设备昵称")
    private String nickname;

    @Schema(description = "状态(0未激活1在线2离线3已禁用)")
    private Integer status;

    @Schema(description = "设备IP")
    private String ipAddress;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "设备标签")
    private String tags;

    @Schema(description = "最后上线时间")
    private LocalDateTime lastOnlineTime;
}
