package org.lyz.iot.mqtt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotMqttConfig;
import org.lyz.iot.mapper.mysql.IotMqttConfigMapper;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttEventListener implements MessageListener {

    private final RedisMessageListenerContainer redisContainer;
    private final IotMqttConfigMapper mqttConfigMapper;
    private final MqttClientManager mqttClientManager;

    @PostConstruct
    public void subscribe() {
        redisContainer.addMessageListener(this, new ChannelTopic(MqttEventPublisher.TOPIC_MQTT_EVENT));
        log.info("MQTT事件监听已启动，订阅频道: {}", MqttEventPublisher.TOPIC_MQTT_EVENT);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        log.info("收到MQTT事件: {}", body);

        if (body.startsWith("start:")) {
            String configId = body.substring(6);
            handleStart(configId);
        } else if (body.startsWith("stop:")) {
            String configId = body.substring(5);
            handleStop(configId);
        }
    }

    private void handleStart(String configId) {
        if (mqttClientManager.isRunning(configId)) {
            log.info("MQTT客户端已在运行，跳过: configId={}", configId);
            return;
        }
        try {
            IotMqttConfig config = mqttConfigMapper.selectById(configId);
            if (config == null) {
                log.warn("MQTT配置不存在: configId={}", configId);
                return;
            }
            mqttClientManager.start(config);
            log.info("MQTT客户端启动成功(事件触发): configId={}", configId);
        } catch (Exception e) {
            log.error("MQTT客户端启动失败(事件触发): configId={}", configId, e);
        }
    }

    private void handleStop(String configId) {
        if (!mqttClientManager.isRunning(configId)) {
            log.info("MQTT客户端未运行，跳过: configId={}", configId);
            return;
        }
        mqttClientManager.stop(configId);
        log.info("MQTT客户端已停止(事件触发): configId={}", configId);
    }
}
