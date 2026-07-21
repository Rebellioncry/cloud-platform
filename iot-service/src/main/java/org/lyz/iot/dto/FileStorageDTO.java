package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件存储配置DTO")
public class FileStorageDTO {
    @Schema(description = "ID(编辑时传)")
    private String id;

    @Schema(description = "存储名称")
    private String name;

    @Schema(description = "存储类型(0本地 1MinIO)")
    private Integer storageType;

    @Schema(description = "本地存储路径")
    private String localPath;

    @Schema(description = "MinIO Endpoint")
    private String endpoint;

    @Schema(description = "MinIO AccessKey")
    private String accessKey;

    @Schema(description = "MinIO SecretKey")
    private String secretKey;

    @Schema(description = "MinIO Bucket")
    private String bucket;

    @Schema(description = "MinIO Region")
    private String region;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "状态")
    private Integer status;
}
