package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "产品DTO")
public class ProductDTO {

    @Schema(description = "产品ID")
    private String id;

    @Schema(description = "父产品ID")
    private String parentId;

    @Schema(description = "产品密钥")
    private String productKey;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "节点类型(0直连1网关2子设备)")
    private Integer nodeType;

    @Schema(description = "协议")
    private String protocol;

    @Schema(description = "数据格式")
    private Integer dataFormat;

    @Schema(description = "物模型(TSL JSON)")
    private String thingModel;

    @Schema(description = "物模型状态(0正常 1禁用)")
    private Integer modelStatus;

    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
