package org.lyz.iot.rule.sink.impl;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.lyz.iot.rule.sink.DataSink;
import org.lyz.iot.rule.sink.DataSinkConfig;
import org.lyz.iot.rule.sink.DataSinkMessage;

import java.util.UUID;

@Slf4j
public class MqttDataSink implements DataSink {

    private DataSinkConfig config;
    private MqttClient client;
    private boolean alive = false;

    private String broker;
    private int port;
    private String defaultTopic;
    private String username;
    private String password;
    private int qos;
    private int keepAlive;

    @Override
    public String getType() {
        return "mqtt";
    }

    @Override
    public void init(DataSinkConfig config) {
        this.config = config;
        this.broker = config.getString("broker", "localhost");
        this.port = config.getInt("port", 1883);
        this.defaultTopic = config.getString("topic", "iot/output");
        this.username = config.getString("username", "");
        this.password = config.getString("password", "");
        this.qos = config.getInt("qos", 1);
        this.keepAlive = config.getInt("keepAlive", 60);

        try {
            String clientId = "rule-sink-" + UUID.randomUUID().toString().substring(0, 8);
            String brokerUrl = "tcp://" + broker + ":" + port;

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setKeepAliveInterval(keepAlive);
            if (!username.isEmpty()) {
                options.setUserName(username);
            }
            if (!password.isEmpty()) {
                options.setPassword(password.toCharArray());
            }

            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT Sink连接断开: broker={}, reason={}", brokerUrl, cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {}

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            client.connect(options);
            this.alive = true;
            log.info("MQTT DataSink连接成功: broker={}:{}, clientId={}", broker, port, clientId);
        } catch (Exception e) {
            log.error("MQTT DataSink连接失败: broker={}:{}", broker, port, e);
            this.alive = false;
        }
    }

    @Override
    public void send(DataSinkMessage message) {
        if (!alive || client == null || !client.isConnected()) {
            log.warn("MQTT DataSink未就绪, 消息丢弃: topic={}", message.getTopic());
            return;
        }
        String topic = message.getTopic() != null && !message.getTopic().isEmpty()
                ? message.getTopic() : defaultTopic;
        try {
            MqttMessage mqttMessage = new MqttMessage(
                    message.getPayload() != null ? message.getPayload().getBytes() : new byte[0]);
            mqttMessage.setQos(qos);
            client.publish(topic, mqttMessage);
            log.debug("MQTT DataSink发送成功: topic={}, qos={}, payloadLength={}", topic, qos,
                    message.getPayload() != null ? message.getPayload().length() : 0);
        } catch (Exception e) {
            log.error("MQTT DataSink发送失败: topic={}", topic, e);
        }
    }

    @Override
    public void close() {
        this.alive = false;
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
                log.info("MQTT DataSink连接已关闭: broker={}:{}", broker, port);
            } catch (Exception e) {
                log.error("MQTT DataSink关闭失败: broker={}:{}", broker, port, e);
            }
        }
        this.client = null;
    }

    @Override
    public boolean isAlive() {
        return alive && client != null && client.isConnected();
    }
}
