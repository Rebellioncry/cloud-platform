package org.lyz.sms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "短信/邮件发送请求")
public class SmsSendRequest {

    @NotBlank(message = "发送类型不能为空")
    @Schema(description = "发送类型: sms(短信) / email(邮件)")
    private String type;

    @NotBlank(message = "发送目标不能为空")
    @Schema(description = "发送目标: 手机号或邮箱")
    private String target;

    @Schema(description = "邮件主题（email类型时使用）")
    private String subject;

    @NotBlank(message = "发送内容不能为空")
    @Schema(description = "发送内容: 短信为纯文本, 邮件为HTML")
    private String content;

    @Schema(description = "短信模板Code（sms类型时使用）")
    private String templateCode;

    @Schema(description = "短信模板参数（sms类型时使用）")
    private Map<String, String> templateParams;
}
