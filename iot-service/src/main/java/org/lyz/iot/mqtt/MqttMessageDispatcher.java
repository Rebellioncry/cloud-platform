package org.lyz.iot.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.config.TenantIgnore;
import org.lyz.iot.mqtt.handler.PropertyPostHandler;
import org.lyz.iot.mqtt.handler.EventPostHandler;
import org.lyz.iot.mqtt.handler.ServiceSetHandler;
import org.lyz.iot.mqtt.handler.ServiceInvokeHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@TenantIgnore
public class MqttMessageDispatcher {

    private final ObjectMapper objectMapper;
    private final PropertyPostHandler propertyPostHandler;
    private final EventPostHandler eventPostHandler;
    private final ServiceSetHandler serviceSetHandler;
    private final ServiceInvokeHandler serviceInvokeHandler;

    public void dispatch(String topic, String payload) {
        try {
            String normalized = MqttTopicUtils.normalizeTopic(topic);
            log.debug("MQTT消息: topic={}, normalized={}", topic, normalized);
            JsonNode root = objectMapper.readTree(payload);
            String method = root.has("method") ? root.get("method").asText() : "";

            if ("thing.property.post".equals(method)) {
                propertyPostHandler.handle(normalized, root);
            } else if ("thing.event.property.pack.post".equals(method)) {
                propertyPostHandler.handlePackPost(normalized, root);
            } else if ("thing.event.property.history.post".equals(method)) {
                propertyPostHandler.handleHistoryPost(normalized, root);
            } else if ("thing.event.property.batch.post".equals(method)) {
                propertyPostHandler.handleBatchPost(normalized, root);
            } else if (method.startsWith("thing.event.") && method.endsWith(".post")) {
                eventPostHandler.handle(normalized, root);
            } else if ("thing.service.property.set".equals(method)) {
                serviceSetHandler.handle(normalized, root);
            } else if (method.startsWith("thing.service.") && method.endsWith("_reply")) {
                serviceInvokeHandler.handle(normalized, root);
            } else {
                log.warn("未知MQTT方法: method={}, topic={}", method, normalized);
            }
        } catch (Exception e) {
            log.error("MQTT消息处理失败: topic={}", topic, e);
        }
    }

    public String extractProductKey(String topic) {
        return MqttTopicUtils.extractProductKey(topic);
    }

    public String extractDeviceName(String topic) {
        return MqttTopicUtils.extractDeviceName(topic);
    }
}
