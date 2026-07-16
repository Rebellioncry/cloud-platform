package org.lyz.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "产品树形结构DTO")
public class ProductTreeDTO {

    @Schema(description = "产品ID")
    private String id;

    @Schema(description = "父产品ID")
    private String parentId;

    @Schema(description = "产品密钥")
    private String productKey;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "节点类型(0直连1网关2子设备)")
    private Integer nodeType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子产品列表")
    private List<ProductTreeDTO> children;
}
