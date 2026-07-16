package org.lyz.sms.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.sms.dto.SmsSendRequest;
import org.lyz.sms.service.MessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service("smsSender")
public class SmsSenderImpl implements MessageSender {

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.template-code:}")
    private String defaultTemplateCode;

    private Client client;

    @PostConstruct
    public void init() throws Exception {
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            log.warn("阿里云短信AccessKey未配置，短信发送功能不可用");
            return;
        }
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com");
        this.client = new Client(config);
    }

    @Override
    public void send(SmsSendRequest request) {
        if (client == null) {
            throw new BusinessException("短信服务未配置，请联系管理员");
        }

        String target = request.getTarget();
        String content = request.getContent();
        String templateCode = request.getTemplateCode() != null ? request.getTemplateCode() : defaultTemplateCode;
        String templateParam = request.getTemplateParams() != null
                ? request.getTemplateParams().toString()
                : "{\"code\":\"" + content + "\"}";

        try {
            SendSmsRequest smsRequest = new SendSmsRequest()
                    .setPhoneNumbers(target)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);

            SendSmsResponse response = client.sendSms(smsRequest);

            if (!"OK".equals(response.getBody().getCode())) {
                log.error("短信发送失败: {} - {}", response.getBody().getCode(), response.getBody().getMessage());
                throw new BusinessException("短信发送失败: " + response.getBody().getMessage());
            }
            log.info("短信发送成功: {}", target);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("短信发送异常", e);
            throw new BusinessException("短信发送异常，请稍后重试");
        }
    }
}
