package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_firmware")
@Schema(description = "固件包")
public class IotFirmware extends BaseEntity {

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "产品密钥")
    private String productKey;

    @Schema(description = "固件名称")
    private String firmwareName;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "存储配置ID")
    private String storageId;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件MD5")
    private String fileMd5;
}
