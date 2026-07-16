package org.lyz.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.lyz.auth.client.SmsClient;
import org.lyz.common.core.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationService {

    private final StringRedisTemplate redisTemplate;
    private final SmsClient smsClient;

    public VerificationService(StringRedisTemplate redisTemplate, SmsClient smsClient) {
        this.redisTemplate = redisTemplate;
        this.smsClient = smsClient;
    }

    private static final int CODE_LENGTH = 6;
    private static final int CODE_TTL_MINUTES = 5;
    private static final int LIMIT_TTL_SECONDS = 60;

    private static final String CODE_PREFIX = "auth:code:";
    private static final String LIMIT_PREFIX = "auth:limit:";

    public void sendCode(String type, String target) {
        checkFrequencyLimit(type, target);

        String code = generateCode();

        String codeKey = CODE_PREFIX + type + ":" + target;
        redisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        String limitKey = LIMIT_PREFIX + type + ":" + target;
        redisTemplate.opsForValue().set(limitKey, "1", LIMIT_TTL_SECONDS, TimeUnit.SECONDS);

        sendViaSmsService(type, target, code);
    }

    public boolean verifyCode(String type, String target, String code) {
        String codeKey = CODE_PREFIX + type + ":" + target;
        String cachedCode = redisTemplate.opsForValue().get(codeKey);

        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        redisTemplate.delete(codeKey);
        return true;
    }

    public String resolveTarget(String type, String mobile, String email) {
        if ("sms".equalsIgnoreCase(type)) {
            if (mobile == null || mobile.isEmpty()) {
                throw new BusinessException("手机号不能为空");
            }
            return mobile;
        } else if ("email".equalsIgnoreCase(type)) {
            if (email == null || email.isEmpty()) {
                throw new BusinessException("邮箱不能为空");
            }
            return email;
        }
        throw new BusinessException("不支持的验证码类型: " + type);
    }

    private void sendViaSmsService(String type, String target, String code) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("type", type);
            request.put("target", target);

            if ("sms".equalsIgnoreCase(type)) {
                request.put("content", code);
            } else {
                request.put("subject", "【Cloud Platform】登录验证码");
                request.put("content", buildEmailContent(code));
            }

            var result = smsClient.send(request);
            if (result == null || result.getCode() != 200) {
                throw new BusinessException("发送失败: " + (result != null ? result.getMessage() : "未知错误"));
            }

            log.info("验证码已发送 [{}]: {}", type, target);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用sms-service失败", e);
            throw new BusinessException("验证码发送失败，请稍后重试");
        }
    }

    private void checkFrequencyLimit(String type, String target) {
        String limitKey = LIMIT_PREFIX + type + ":" + target;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new BusinessException("发送过于频繁，请60秒后重试");
        }
    }

    private String generateCode() {
        int random = ThreadLocalRandom.current().nextInt(0, 1000000);
        return String.format("%0" + CODE_LENGTH + "d", random);
    }

    private String buildEmailContent(String code) {
        return "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h3 style='color: #333;'>Cloud Platform 登录验证码</h3>"
                + "<p>您的验证码是：</p>"
                + "<p style='font-size: 28px; font-weight: bold; color: #409EFF; letter-spacing: 5px;'>"
                + code + "</p>"
                + "<p style='color: #999; font-size: 12px;'>验证码 " + CODE_TTL_MINUTES + " 分钟内有效，请勿泄露给他人。</p>"
                + "</div>";
    }
}
