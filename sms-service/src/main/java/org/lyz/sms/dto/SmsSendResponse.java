package org.lyz.sms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "短信/邮件发送响应")
public class SmsSendResponse {

    @Schema(description = "是否成功")
    private boolean success;

    @Schema(description = "消息")
    private String message;
}
