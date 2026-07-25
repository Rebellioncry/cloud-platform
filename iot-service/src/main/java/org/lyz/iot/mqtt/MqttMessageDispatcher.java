package org.lyz.iot.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.mqtt.handler.PropertyPostHandler;
import org.lyz.iot.mqtt.handler.EventPostHandler;
import org.lyz.iot.mqtt.handler.ServiceSetHandler;
import org.lyz.iot.mqtt.handler.ServiceInvokeHandler;
import org.lyz.iot.mqtt.handler.OtaHandler;
import org.lyz.iot.rule.engine.RuleDataBus;
import org.lyz.iot.rule.model.RuleData;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttMessageDispatcher {

    private final ObjectMapper objectMapper;
    private final PropertyPostHandler propertyPostHandler;
    private final EventPostHandler eventPostHandler;
    private final ServiceSetHandler serviceSetHandler;
    private final ServiceInvokeHandler serviceInvokeHandler;
    private final OtaHandler otaHandler;
    private final RuleDataBus ruleDataBus;

    public void dispatch(String topic, String payload) {
        try {
            String normalized = MqttTopicUtils.normalizeTopic(topic);
            log.debug("MQTT消息: topic={}, normalized={}", topic, normalized);
            JsonNode root = objectMapper.readTree(payload);
            String method = root.has("method") ? root.get("method").asText() : "";

            publishToRuleEngine(topic, normalized, root, method);

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
            } else if (topic.contains("ota/device/inform/")) {
                otaHandler.handleDeviceInform(normalized, root);
            } else if (topic.contains("ota/device/progress/")) {
                otaHandler.handleDeviceProgress(normalized, root);
            } else if (topic.contains("ota/device/download/")) {
                otaHandler.handleDeviceDownload(normalized, root);
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

    private void publishToRuleEngine(String rawTopic, String normalizedTopic, JsonNode root, String method) {
        try {
            String productKey = extractProductKey(normalizedTopic);
            String deviceName = extractDeviceName(normalizedTopic);

            RuleData ruleData = new RuleData();
            ruleData.put("topic", rawTopic);
            ruleData.put("normalizedTopic", normalizedTopic);
            ruleData.put("productKey", productKey);
            ruleData.put("deviceName", deviceName);
            ruleData.put("method", method);
            ruleData.put("payload", objectMapper.convertValue(root, java.util.Map.class));

            if ("thing.property.post".equals(method)) {
                ruleData.put("eventType", "property");
            } else if (method.startsWith("thing.event.") && method.endsWith(".post")) {
                String eventName = method.replace("thing.event.", "").replace(".post", "");
                ruleData.put("eventType", eventName);
            } else {
                ruleData.put("eventType", method);
            }

            ruleDataBus.publish(ruleData);
        } catch (Exception e) {
            log.error("发布规则引擎事件失败: topic={}", rawTopic, e);
        }
    }
}
