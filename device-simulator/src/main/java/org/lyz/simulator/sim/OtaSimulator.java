package org.lyz.simulator.sim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lyz.simulator.config.LiveConfig;
import org.lyz.simulator.model.DeviceInfo;
import org.lyz.simulator.mqtt.MqttSimulator;
import org.lyz.simulator.mqtt.TopicUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class OtaSimulator {

    private final MqttSimulator mqtt;
    private final LiveConfig config;
    private final DeviceInfo device;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private volatile boolean upgrading = false;
    private volatile String currentTaskId = null;

    public OtaSimulator(MqttSimulator mqtt, LiveConfig config, DeviceInfo device) {
        this.mqtt = mqtt;
        this.config = config;
        this.device = device;
    }

    public void informVersion() {
        if (!mqtt.isConnected() || upgrading) return;
        try {
            String topic = TopicUtils.buildOtaInformTopic(device.getProductKey(), device.getDeviceName());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", "inform-" + System.currentTimeMillis());
            payload.put("params", Map.of("firmwareVersion", config.getFirmwareVersion()));
            mqtt.publish(topic, objectMapper.writeValueAsString(payload), 1);
        } catch (Exception e) {
            System.err.println("固件版本上报失败: " + e.getMessage());
        }
    }

    public void handleUpgrade(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String taskId = root.has("taskId") ? root.get("taskId").asText() : null;
            String version = root.has("firmwareVersion") ? root.get("firmwareVersion").asText() : null;

            if (taskId == null || taskId.isEmpty()) return;
            if (upgrading) {
                System.out.println("OTA: 正在升级中，忽略新指令. taskId=" + taskId);
                return;
            }

            System.out.println("OTA: 收到升级指令. taskId=" + taskId + ", targetVersion=" + version);
            this.currentTaskId = taskId;
            this.upgrading = true;
            new Thread(() -> simulateOtaProgress(taskId), "ota-progress").start();
        } catch (Exception e) {
            System.err.println("OTA升级指令解析失败: " + e.getMessage());
        }
    }

    private void simulateOtaProgress(String taskId) {
        try {
            sendDownloadStatus(taskId, "downloading");
            int[] progressSteps = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
            for (int progress : progressSteps) {
                Thread.sleep(config.getOtaProgressInterval());
                if (!upgrading) break;

                if (progress < 100) {
                    sendProgress(taskId, progress, "downloading");
                } else {
                    sendProgress(taskId, 100, "success");
                    System.out.println("OTA: 升级完成! taskId=" + taskId);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            this.upgrading = false;
            this.currentTaskId = null;
        }
    }

    private void sendDownloadStatus(String taskId, String status) {
        try {
            String topic = TopicUtils.buildOtaDownloadTopic(device.getProductKey(), device.getDeviceName());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", taskId);
            payload.put("status", status);
            mqtt.publish(topic, objectMapper.writeValueAsString(payload), 1);
        } catch (Exception e) {
            System.err.println("OTA下载状态上报失败: " + e.getMessage());
        }
    }

    private void sendProgress(String taskId, int progress, String status) {
        try {
            String topic = TopicUtils.buildOtaProgressTopic(device.getProductKey(), device.getDeviceName());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", taskId);
            payload.put("progress", progress);
            payload.put("status", status);
            mqtt.publish(topic, objectMapper.writeValueAsString(payload), 1);
            System.out.println("OTA进度: " + progress + "% (" + status + ")");
        } catch (Exception e) {
            System.err.println("OTA进度上报失败: " + e.getMessage());
        }
    }

    public boolean isUpgrading() { return upgrading; }
}
