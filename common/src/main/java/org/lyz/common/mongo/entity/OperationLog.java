package org.lyz.common.mongo.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "op_logs")
@Schema(description = "操作日志")
public class OperationLog {
    @Id
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "租户ID")
    private String tenantId;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "模块")
    private String module;
    @Schema(description = "操作")
    private String operation;
    @Schema(description = "方法名")
    private String method;
    @Schema(description = "请求URL")
    private String url;
    @Schema(description = "HTTP方法")
    private String httpMethod;

    @Schema(description = "请求参数")
    private Map<String, Object> params;
    @Schema(description = "返回结果")
    private Object result;
    @Schema(description = "错误信息")
    private Object errorMsg;

    @Schema(description = "IP地址")
    private String ip;
    @Schema(description = "用户代理")
    private String userAgent;
    @Schema(description = "耗时(ms)")
    private Long duration;
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
