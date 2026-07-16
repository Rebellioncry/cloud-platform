package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("iot_device_command")
@Schema(description = "设备命令")
public class IotDeviceCommand {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "类型(property_read/property_write/function_invoke)")
    private String commandType;

    @Schema(description = "标识符")
    private String identifier;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "输入参数")
    private String inputData;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "输出结果")
    private String outputData;

    @Schema(description = "状态(0待发送1已发送2已完成3超时4失败)")
    private Integer status;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
