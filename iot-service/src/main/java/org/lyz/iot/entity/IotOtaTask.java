package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_ota_task")
@Schema(description = "OTA升级任务")
public class IotOtaTask extends BaseEntity {

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "固件ID")
    private String firmwareId;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "产品密钥")
    private String productKey;

    @Schema(description = "目标类型(0产品全量 1指定设备 2按版本)")
    private Integer targetType;

    @Schema(description = "目标值(设备ID列表/版本号)")
    private String targetValue;

    @Schema(description = "设备总数")
    private Integer totalCount;

    @Schema(description = "成功数")
    private Integer successCount;

    @Schema(description = "失败数")
    private Integer failCount;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "状态(0待执行 1执行中 2已完成 3已取消)")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
