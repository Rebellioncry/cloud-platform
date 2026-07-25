package org.lyz.simulator.sim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lyz.simulator.model.DeviceInfo;
import org.lyz.simulator.mqtt.MqttSimulator;
import org.lyz.simulator.mqtt.TopicUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceCallHandler {

    private final MqttSimulator mqtt;
    private final DeviceInfo device;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceCallHandler(MqttSimulator mqtt, DeviceInfo device) {
        this.mqtt = mqtt;
        this.device = device;
    }

    public void handleInvoke(String topic, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String messageId = root.has("id") ? root.get("id").asText() : null;
            JsonNode params = root.get("params");
            String identifier = params != null && params.has("identifier") ? params.get("identifier").asText() : "";

            System.out.println("服务调用: identifier=" + identifier + ", messageId=" + messageId);

            String replyTopic = TopicUtils.buildServiceReplyTopic(device.getProductKey(), device.getDeviceName(), identifier);
            Map<String, Object> reply = new LinkedHashMap<>();
            if (messageId != null) {
                reply.put("id", messageId);
            }
            reply.put("code", 200);
            reply.put("data", Map.of("result", "ok"));

            mqtt.publish(replyTopic, objectMapper.writeValueAsString(reply), 1);
            System.out.println("服务调用回复: identifier=" + identifier + ", code=200");
        } catch (Exception e) {
            System.err.println("服务调用处理失败: " + e.getMessage());
        }
    }

    public void handlePropertySet(String topic, String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode params = root.get("params");
            if (params != null) {
                System.out.println("属性下发: " + params);
            }
        } catch (Exception e) {
            System.err.println("属性下发处理失败: " + e.getMessage());
        }
    }
}
