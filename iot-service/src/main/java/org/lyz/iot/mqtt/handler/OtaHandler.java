package org.lyz.iot.mqtt.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.service.OtaTaskService;
import org.lyz.iot.mqtt.MqttTopicUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtaHandler {

    private final OtaTaskService otaTaskService;
    private final ObjectMapper objectMapper;

    public void handleDeviceInform(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);
            JsonNode params = root.get("params");
            if (params == null) return;

            String version = params.has("firmwareVersion") ? params.get("firmwareVersion").asText() : null;
            log.info("设备固件版本上报: productKey={}, deviceName={}, version={}", productKey, deviceName, version);
        } catch (Exception e) {
            log.error("设备固件版本上报处理失败", e);
        }
    }

    public void handleDeviceProgress(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            String taskId = root.has("id") ? root.get("id").asText() : null;
            Integer progress = root.has("progress") ? root.get("progress").asInt() : 0;
            String status = root.has("status") ? root.get("status").asText() : "";
            String errorMessage = root.has("message") ? root.get("message").asText() : null;

            if (taskId != null && !taskId.isEmpty()) {
                otaTaskService.updateDeviceProgress(taskId, deviceName, progress, status, errorMessage);
            }

            log.info("OTA进度上报: device={}/{}, progress={}, status={}", productKey, deviceName, progress, status);
        } catch (Exception e) {
            log.error("OTA进度上报处理失败", e);
        }
    }

    public void handleDeviceDownload(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            String taskId = root.has("id") ? root.get("id").asText() : null;
            String status = root.has("status") ? root.get("status").asText() : "";

            if (taskId != null && !taskId.isEmpty()) {
                otaTaskService.updateDeviceProgress(taskId, deviceName, 0, "downloading".equals(status) ? "downloading" : status, null);
            }

            log.info("OTA下载状态上报: device={}/{}, status={}", productKey, deviceName, status);
        } catch (Exception e) {
            log.error("OTA下载状态上报处理失败", e);
        }
    }
}
