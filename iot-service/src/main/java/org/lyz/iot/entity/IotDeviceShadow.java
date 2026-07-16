package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("iot_device_shadow")
@Schema(description = "设备影子")
public class IotDeviceShadow {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "属性标识符")
    private String propertyIdentifier;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "期望值")
    private String desiredValue;

    @Schema(description = "期望版本")
    private Long desiredVersion;

    @Schema(description = "期望设置时间")
    private LocalDateTime desiredTime;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "上报值")
    private String reportedValue;

    @Schema(description = "上报版本")
    private Long reportedVersion;

    @Schema(description = "上报时间")
    private LocalDateTime reportedTime;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "扩展元数据")
    private String metadata;

    @Version
    @Schema(description = "乐观锁版本号")
    private Long version;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
