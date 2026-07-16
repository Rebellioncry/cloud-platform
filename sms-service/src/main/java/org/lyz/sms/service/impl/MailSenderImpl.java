package org.lyz.sms.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.sms.dto.SmsSendRequest;
import org.lyz.sms.service.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service("emailSender")
public class MailSenderImpl implements MessageSender {

    private boolean available = false;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${mail.sender-address:noreply@cloud-platform.com}")
    private String senderAddress;

    @PostConstruct
    public void init() {
        if (mailSender == null || mailUsername == null || mailUsername.isEmpty()) {
            log.warn("邮件服务未配置(spring.mail.username)，邮件发送功能不可用");
            this.available = false;
        } else {
            this.available = true;
            log.info("邮件服务已配置，发送地址: {}", senderAddress);
        }
    }

    @Override
    public void send(SmsSendRequest request) {
        if (!available) {
            throw new BusinessException("邮件服务未配置，请联系管理员");
        }
        String target = request.getTarget();
        String content = request.getContent();
        String subject = request.getSubject() != null ? request.getSubject() : "【Cloud Platform】系统通知";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderAddress);
            helper.setTo(target);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("邮件发送成功: {}", target);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", target, e);
            throw new BusinessException("邮件发送失败，请稍后重试");
        }
    }
}
