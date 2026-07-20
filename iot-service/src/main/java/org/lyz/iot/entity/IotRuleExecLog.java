package org.lyz.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("iot_rule_exec_log")
@Schema(description = "规则执行日志")
public class IotRuleExecLog {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "主键")
    private String id;

    @Schema(description = "规则ID")
    private String ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "节点ID")
    private String nodeId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "执行状态(0成功1失败)")
    private Integer status;

    @Schema(description = "输入数据(JSON)")
    private String inputData;

    @Schema(description = "输出数据(JSON)")
    private String outputData;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "执行耗时(毫秒)")
    private Long duration;

    @Schema(description = "触发设备Key")
    private String deviceKey;

    @Schema(description = "产品Key")
    private String productKey;

    @Schema(description = "执行时间")
    private LocalDateTime executeTime;
}
