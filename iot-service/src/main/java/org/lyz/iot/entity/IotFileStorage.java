package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_file_storage")
@Schema(description = "文件存储配置")
public class IotFileStorage extends BaseEntity {

    @Schema(description = "租户ID")
    private String tenantId;

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

    @Schema(description = "状态(0禁用 1启用)")
    private Integer status;
}
