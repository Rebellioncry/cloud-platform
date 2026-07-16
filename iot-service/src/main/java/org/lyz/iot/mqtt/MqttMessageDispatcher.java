package org.lyz.iot.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.mqtt.handler.PropertyPostHandler;
import org.lyz.iot.mqtt.handler.EventPostHandler;
import org.lyz.iot.mqtt.handler.ServiceSetHandler;
import org.lyz.iot.mqtt.handler.ServiceInvokeHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
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

            switch (method) {
                case "thing.property.post":
                    propertyPostHandler.handle(normalized, root);
                    break;
                case "thing.event.post":
                    eventPostHandler.handle(normalized, root);
                    break;
                case "thing.service.property.set":
                    serviceSetHandler.handle(normalized, root);
                    break;
                case "thing.service.invoke":
                    serviceInvokeHandler.handle(normalized, root);
                    break;
                default:
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
