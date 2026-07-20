package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkMessage;

@Slf4j
public class KafkaDataSink implements DataSink {

    private DataSinkConfig config;
    private boolean alive = false;

    private String bootstrapServers;
    private String defaultTopic;
    private String groupId;

    @Override
    public String getType() {
        return "kafka";
    }

    @Override
    public void init(DataSinkConfig config) {
        this.config = config;
        this.bootstrapServers = config.getString("bootstrapServers", "localhost:9092");
        this.defaultTopic = config.getString("topic", "iot-data");
        this.groupId = config.getString("groupId", "iot-rule-engine");
        this.alive = true;
        log.info("Kafka DataSink初始化(桩模式): bootstrapServers={}, topic={}, groupId={}", bootstrapServers, defaultTopic, groupId);
    }

    @Override
    public void send(DataSinkMessage message) {
        if (!alive) {
            log.warn("Kafka DataSink未就绪, 消息丢弃: topic={}", message.getTopic());
            return;
        }
        String topic = message.getTopic() != null ? message.getTopic() : defaultTopic;
        String key = message.getKey();
        log.info("[Kafka-桩] 发送消息: server={}, topic={}, key={}, payload={}", bootstrapServers, topic, key, message.getPayload());
    }

    @Override
    public void close() {
        this.alive = false;
        log.info("Kafka DataSink已关闭(桩模式)");
    }

    @Override
    public boolean isAlive() {
        return alive;
    }
}
