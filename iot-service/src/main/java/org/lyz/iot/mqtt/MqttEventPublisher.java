package org.lyz.iot.mqtt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttEventPublisher {

    public static final String TOPIC_MQTT_EVENT = "iot:mqtt:event";

    private final StringRedisTemplate redisTemplate;

    public void publishStart(String configId) {
        redisTemplate.convertAndSend(TOPIC_MQTT_EVENT, "start:" + configId);
        log.info("MQTT启动事件广播: configId={}", configId);
    }

    public void publishStop(String configId) {
        redisTemplate.convertAndSend(TOPIC_MQTT_EVENT, "stop:" + configId);
        log.info("MQTT停止事件广播: configId={}", configId);
    }
}
