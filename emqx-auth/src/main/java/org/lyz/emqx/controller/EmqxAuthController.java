package org.lyz.emqx.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.emqx.entity.EmqxAclDevice;
import org.lyz.emqx.mapper.EmqxAclDeviceMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/emqx")
@RequiredArgsConstructor
public class EmqxAuthController {

    private static final String CACHE_PREFIX = "emqx:auth:";
    private static final long CACHE_TTL_MINUTES = 5;

    private final EmqxAclDeviceMapper deviceMapper;
    private final StringRedisTemplate redisTemplate;

    @Value("${iot.platform.username:iot-platform}")
    private String platformUsername;

    @Value("${iot.platform.password:iot-platform-key}")
    private String platformPassword;

    private static final Map<String, String> RESULT_ALLOW = Map.of("result", "allow");
    private static final Map<String, String> RESULT_DENY = Map.of("result", "deny");

    @PostMapping("/auth")
    public ResponseEntity<Map<String, String>> auth(@RequestBody Map<String, Object> payload) {
        String clientid = (String) payload.get("clientid");
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");

        log.info("EMQX认证请求: clientid={}, username={}", clientid, username);

        if (username == null || password == null) {
            log.warn("认证参数缺失: clientid={}, username={}, password={}", clientid, username, password == null ? "null" : "***");
            return ResponseEntity.ok(RESULT_DENY);
        }

        if (platformUsername.equals(username)) {
            if (platformPassword.equals(password)) {
                log.info("平台客户端认证通过: clientid={}", clientid);
                return ResponseEntity.ok(RESULT_ALLOW);
            }
            log.warn("平台客户端认证失败(密码错误): clientid={}", clientid);
            return ResponseEntity.ok(RESULT_DENY);
        }

        String cacheKey = CACHE_PREFIX + username;
        String cachedKey = redisTemplate.opsForValue().get(cacheKey);
        if (cachedKey != null) {
            if (cachedKey.equals(password)) {
                log.info("设备认证通过(缓存): deviceName={}", username);
                return ResponseEntity.ok(RESULT_ALLOW);
            }
            log.warn("设备认证失败(缓存密码不匹配): deviceName={}", username);
            return ResponseEntity.ok(RESULT_DENY);
        }

        EmqxAclDevice device = deviceMapper.selectOne(
                new LambdaQueryWrapper<EmqxAclDevice>()
                        .eq(EmqxAclDevice::getDeviceName, username)
                        .last("LIMIT 1"));

        if (device == null || device.getDeviceKey() == null) {
            log.warn("设备不存在或无密钥: deviceName={}", username);
            return ResponseEntity.ok(RESULT_DENY);
        }

        if (!device.getDeviceKey().equals(password)) {
            log.warn("设备密钥不匹配: deviceName={}", username);
            return ResponseEntity.ok(RESULT_DENY);
        }

        if (device.getStatus() != null && device.getStatus() == 3) {
            log.warn("设备已禁用，拒绝连接: deviceName={}", username);
            return ResponseEntity.ok(RESULT_DENY);
        }

        redisTemplate.opsForValue().set(cacheKey, password, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("设备认证通过(DB): deviceName={}", username);
        return ResponseEntity.ok(RESULT_ALLOW);
    }
}
