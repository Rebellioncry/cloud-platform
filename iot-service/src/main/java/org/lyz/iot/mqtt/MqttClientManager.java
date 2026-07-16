package org.lyz.iot.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.lyz.iot.entity.IotMqttConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MqttClientManager {

    private final ConcurrentHashMap<String, MqttClient> clientMap = new ConcurrentHashMap<>();
    private final MqttMessageDispatcher dispatcher;

    @Value("${iot.platform.username:iot-platform}")
    private String platformUsername;

    @Value("${iot.platform.password:iot-platform-key}")
    private String platformPassword;

    public MqttClientManager(MqttMessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void start(IotMqttConfig config) {
        stop(config.getId());
        try {
            String clientId = config.getClientIdPrefix() + "-" + UUID.randomUUID().toString().substring(0, 8);
            String brokerUrl = "tcp://" + config.getBroker() + ":" + config.getPort();

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(config.getAutoReconnect() == 1);
            options.setKeepAliveInterval(config.getKeepAlive());
            if (platformUsername != null && !platformUsername.isEmpty()) {
                options.setUserName(platformUsername);
            }
            if (platformPassword != null && !platformPassword.isEmpty()) {
                options.setPassword(platformPassword.toCharArray());
            }

            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT连接断开: configId={}, reason={}", config.getId(), cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    dispatcher.dispatch(topic, new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            client.connect(options);

            String group = config.getSharedGroup() != null && !config.getSharedGroup().isEmpty()
                    ? config.getSharedGroup() : MqttTopicConstants.SHARE_GROUP_DEFAULT;
            String[] topics = MqttTopicConstants.getSubscribeTopics(group);
            int[] qos = new int[topics.length];
            Arrays.fill(qos, config.getQos());
            client.subscribe(topics, qos);
            log.info("MQTT共享订阅: configId={}, group={}, topics={}", config.getId(), group, Arrays.toString(topics));

            clientMap.put(config.getId(), client);
            log.info("MQTT客户端启动成功: configId={}, clientId={}, broker={}", config.getId(), clientId, brokerUrl);
        } catch (Exception e) {
            log.error("MQTT客户端启动失败: configId={}", config.getId(), e);
            throw new RuntimeException("MQTT客户端启动失败: " + e.getMessage());
        }
    }

    public void stop(String configId) {
        MqttClient client = clientMap.remove(configId);
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
                log.info("MQTT客户端已停止: configId={}", configId);
            } catch (Exception e) {
                log.error("MQTT客户端停止失败: configId={}", configId, e);
            }
        }
    }

    public void publish(String configId, String topic, String payload, int qos) {
        MqttClient client = clientMap.get(configId);
        if (client != null && client.isConnected()) {
            try {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                client.publish(topic, message);
            } catch (Exception e) {
                log.error("MQTT发布失败: configId={}, topic={}", configId, topic, e);
            }
        }
    }

    public boolean isRunning(String configId) {
        MqttClient client = clientMap.get(configId);
        return client != null && client.isConnected();
    }
}
