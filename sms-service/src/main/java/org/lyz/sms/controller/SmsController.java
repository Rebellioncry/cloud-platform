package org.lyz.sms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.lyz.common.core.result.Result;
import org.lyz.sms.dto.SmsSendRequest;
import org.lyz.sms.dto.SmsSendResponse;
import org.lyz.sms.service.MessageSender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Tag(name = "消息发送", description = "短信和邮件发送接口")
public class SmsController {

    private final MessageSender smsSender;
    private final MessageSender mailSender;

    public SmsController(@Qualifier("smsSender") MessageSender smsSender,
                         @Qualifier("emailSender") MessageSender mailSender) {
        this.smsSender = smsSender;
        this.mailSender = mailSender;
    }

    @Operation(summary = "发送消息", description = "统一发送接口，支持短信和邮件")
    @PostMapping("/send")
    public Result<SmsSendResponse> send(@Valid @RequestBody SmsSendRequest request) {
        MessageSender sender = resolveSender(request.getType());
        sender.send(request);
        return Result.success(SmsSendResponse.builder()
                .success(true)
                .message("发送成功")
                .build());
    }

    private MessageSender resolveSender(String type) {
        if ("sms".equalsIgnoreCase(type)) {
            return smsSender;
        } else if ("email".equalsIgnoreCase(type)) {
            return mailSender;
        }
        throw new org.lyz.common.core.exception.BusinessException("不支持的发送类型: " + type);
    }
}
