package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "固件包DTO")
public class FirmwareDTO {
    @Schema(description = "ID(编辑时传)")
    private String id;

    @Schema(description = "产品ID")
    private String productId;

    @Schema(description = "固件名称")
    private String firmwareName;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "存储配置ID")
    private String storageId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件MD5")
    private String fileMd5;
}
