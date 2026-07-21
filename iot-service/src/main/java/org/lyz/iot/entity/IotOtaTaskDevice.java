package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("iot_ota_task_device")
@Schema(description = "OTA设备维度记录")
public class IotOtaTaskDevice implements Serializable {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "产品密钥")
    private String productKey;

    @Schema(description = "当前版本")
    private String currentVersion;

    @Schema(description = "目标版本")
    private String targetVersion;

    @Schema(description = "状态(0待升级 1推送中 2下载中 3升级中 4成功 5失败 6已取消)")
    private Integer status;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "推送时间")
    private LocalDateTime pushTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
