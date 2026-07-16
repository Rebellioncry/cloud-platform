package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.lyz.common.core.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_product")
@Schema(description = "IoT产品")
public class IotProduct extends BaseEntity {

    @Schema(description = "租户ID")
    private String tenantId;

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

    @Schema(description = "数据格式(0ALink JSON)")
    private Integer dataFormat;

    @TableField(updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.ALWAYS)
    @Schema(description = "物模型(TSL JSON)")
    private String thingModel;

    @Schema(description = "物模型状态(0草稿1发布2停用)")
    private Integer modelStatus;

    @Schema(description = "状态(0禁用1正常)")
    private Integer status;
}
