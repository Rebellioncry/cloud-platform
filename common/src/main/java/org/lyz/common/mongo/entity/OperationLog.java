package org.lyz.common.mongo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "op_logs")
public class OperationLog {
    @Id
    private String id;

    private String tenantId;
    private String userId;
    private String username;

    private String module;
    private String operation;
    private String method;
    private String url;
    private String httpMethod;

    private Map<String, Object> params;
    private Object result;
    private Object errorMsg;

    private String ip;
    private String userAgent;
    private Long duration;
    private Integer status;

    private LocalDateTime createTime;
}
