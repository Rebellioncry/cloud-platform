package org.lyz.simulator.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class MqttSimulator {

    private MqttClient client;
    private final CopyOnWriteArrayList<MessageListener> listeners = new CopyOnWriteArrayList<>();

    @FunctionalInterface
    public interface MessageListener {
        void onMessage(String topic, String payload);
    }

    public void addListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MessageListener listener) {
        listeners.remove(listener);
    }

    public void connect(String broker, int port, String deviceName, String deviceKey) throws MqttException {
        disconnect();
        String clientId = "sim-" + deviceName + "-" + UUID.randomUUID().toString().substring(0, 8);
        String url = "tcp://" + broker + ":" + port;
        client = new MqttClient(url, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(30);
        options.setUserName(deviceName);
        options.setPassword(deviceKey.toCharArray());

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("MQTT连接断开: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                for (MessageListener l : listeners) {
                    try {
                        l.onMessage(topic, payload);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        client.connect(options);
        client.subscribe(new String[]{
                TopicUtils.SUB_SERVICE_PROPERTY_SET,
                TopicUtils.SUB_SERVICE_INVOKE,
                TopicUtils.SUB_OTA_UPGRADE
        }, new int[]{1, 1, 1});
        System.out.println("MQTT已连接: " + url + ", clientId=" + clientId);
    }

    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            client = null;
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public void publish(String topic, String payload, int qos) throws MqttException {
        if (client == null || !client.isConnected()) {
            throw new MqttException(new IllegalStateException("MQTT未连接"));
        }
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        client.publish(topic, message);
    }
}
