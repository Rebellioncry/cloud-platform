package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkMessage;

@Slf4j
public class PulsarDataSink implements DataSink {

    private DataSinkConfig config;
    private boolean alive = false;

    private String serviceUrl;
    private String defaultTopic;
    private String tenant;
    private String namespace;

    @Override
    public String getType() {
        return "pulsar";
    }

    @Override
    public void init(DataSinkConfig config) {
        this.config = config;
        this.serviceUrl = config.getString("serviceUrl", "pulsar://localhost:6650");
        this.defaultTopic = config.getString("topic", "persistent://public/default/iot");
        this.tenant = config.getString("tenant", "public");
        this.namespace = config.getString("namespace", "default");
        this.alive = true;
        log.info("Pulsar DataSink初始化(桩模式): serviceUrl={}, topic={}, tenant={}, namespace={}", serviceUrl, defaultTopic, tenant, namespace);
    }

    @Override
    public void send(DataSinkMessage message) {
        if (!alive) {
            log.warn("Pulsar DataSink未就绪, 消息丢弃: topic={}", message.getTopic());
            return;
        }
        String topic = message.getTopic() != null ? message.getTopic() : defaultTopic;
        log.info("[Pulsar-桩] 发送消息: serviceUrl={}, topic={}, payload={}", serviceUrl, topic, message.getPayload());
    }

    @Override
    public void close() {
        this.alive = false;
        log.info("Pulsar DataSink已关闭(桩模式)");
    }

    @Override
    public boolean isAlive() {
        return alive;
    }
}
