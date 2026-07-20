package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkMessage;

@Slf4j
public class RocketMQDataSink implements DataSink {

    private DataSinkConfig config;
    private boolean alive = false;

    private String nameServer;
    private String defaultTopic;
    private String producerGroup;

    @Override
    public String getType() {
        return "rocketmq";
    }

    @Override
    public void init(DataSinkConfig config) {
        this.config = config;
        this.nameServer = config.getString("nameServer", "localhost:9876");
        this.defaultTopic = config.getString("topic", "iot-data");
        this.producerGroup = config.getString("producerGroup", "iot-rule-producer");
        this.alive = true;
        log.info("RocketMQ DataSink初始化(桩模式): nameServer={}, topic={}, group={}", nameServer, defaultTopic, producerGroup);
    }

    @Override
    public void send(DataSinkMessage message) {
        if (!alive) {
            log.warn("RocketMQ DataSink未就绪, 消息丢弃: topic={}", message.getTopic());
            return;
        }
        String topic = message.getTopic() != null ? message.getTopic() : defaultTopic;
        String key = message.getKey();
        log.info("[RocketMQ-桩] 发送消息: nameServer={}, topic={}, group={}, key={}, payload={}", nameServer, topic, producerGroup, key, message.getPayload());
    }

    @Override
    public void close() {
        this.alive = false;
        log.info("RocketMQ DataSink已关闭(桩模式)");
    }

    @Override
    public boolean isAlive() {
        return alive;
    }
}
