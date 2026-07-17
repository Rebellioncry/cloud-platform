package org.lyz.iot.mqtt.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceTelemetry;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mapper.mysql.IotDeviceShadowMapper;
import org.lyz.iot.mapper.tdengine.DeviceTelemetryMapper;
import org.lyz.iot.entity.IotDeviceShadow;
import org.lyz.iot.mqtt.MqttTopicUtils;
import org.lyz.iot.service.DeviceShadowService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyPostHandler {

    private final IotDeviceMapper deviceMapper;
    private final DeviceTelemetryMapper telemetryMapper;
    private final DeviceShadowService shadowService;
    private final ObjectMapper objectMapper;

    public void handle(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("属性上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }

            JsonNode params = root.get("params");
            if (params == null || !params.isObject()) return;

            params.fields().forEachRemaining(entry -> {
                String identifier = entry.getKey();
                JsonNode valueNode = entry.getValue();
                String value = valueNode.isObject() ? valueNode.toString() : valueNode.asText();

                IotDeviceTelemetry telemetry = new IotDeviceTelemetry();
                telemetry.setTs(LocalDateTime.now());
                telemetry.setValue(value);
                telemetry.setValueType(valueNode.isObject() ? "object" : valueNode.getNodeType().name().toLowerCase());
                telemetry.setQuality("GOOD");
                telemetry.setDeviceId(device.getId());
                telemetry.setProductKey(productKey);
                telemetry.setPropertyId(identifier);
                telemetry.setDeviceName(deviceName);
                telemetryMapper.insert(telemetry);

                shadowService.updateReported(device.getId(), identifier, value);
            });

            device.setStatus(1);
            device.setLastOnlineTime(LocalDateTime.now());
            deviceMapper.updateById(device);

            log.info("属性上报处理完成: device={}, properties={}", device.getDeviceName(), params);
        } catch (Exception e) {
            log.error("属性上报处理失败", e);
        }
    }

    public void handleHistoryPost(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("历史属性上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }

            JsonNode params = root.get("params");
            if (params == null || !params.isObject()) return;

            params.fields().forEachRemaining(entry -> {
                String identifier = entry.getKey();
                JsonNode valueNode = entry.getValue();
                String value = valueNode.isObject() ? valueNode.toString() : valueNode.asText();

                IotDeviceTelemetry telemetry = new IotDeviceTelemetry();
                telemetry.setTs(LocalDateTime.now());
                telemetry.setValue(value);
                telemetry.setValueType(valueNode.isObject() ? "object" : valueNode.getNodeType().name().toLowerCase());
                telemetry.setQuality("HISTORY");
                telemetry.setDeviceId(device.getId());
                telemetry.setProductKey(productKey);
                telemetry.setPropertyId(identifier);
                telemetry.setDeviceName(deviceName);
                telemetryMapper.insert(telemetry);

                shadowService.updateReported(device.getId(), identifier, value);
            });

            log.info("历史属性上报处理完成: device={}, properties={}", device.getDeviceName(), params);
        } catch (Exception e) {
            log.error("历史属性上报处理失败", e);
        }
    }

    public void handleBatchPost(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("批量属性上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }

            JsonNode params = root.get("params");
            if (params == null || !params.isObject()) return;

            params.fields().forEachRemaining(entry -> {
                String identifier = entry.getKey();
                JsonNode valueNode = entry.getValue();
                String value = valueNode.isObject() ? valueNode.toString() : valueNode.asText();

                IotDeviceTelemetry telemetry = new IotDeviceTelemetry();
                telemetry.setTs(LocalDateTime.now());
                telemetry.setValue(value);
                telemetry.setValueType(valueNode.isObject() ? "object" : valueNode.getNodeType().name().toLowerCase());
                telemetry.setQuality("BATCH");
                telemetry.setDeviceId(device.getId());
                telemetry.setProductKey(productKey);
                telemetry.setPropertyId(identifier);
                telemetry.setDeviceName(deviceName);
                telemetryMapper.insert(telemetry);

                shadowService.updateReported(device.getId(), identifier, value);
            });

            log.info("批量属性上报处理完成: device={}, properties={}", device.getDeviceName(), params);
        } catch (Exception e) {
            log.error("批量属性上报处理失败", e);
        }
    }

    public void handlePackPost(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("网关批量上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }

            JsonNode params = root.get("params");
            if (params == null) return;

            JsonNode subDevices = params.get("subDevices");
            if (subDevices != null && subDevices.isArray()) {
                for (JsonNode sub : subDevices) {
                    String subProductKey = sub.has("productKey") ? sub.get("productKey").asText() : productKey;
                    String subDeviceName = sub.has("deviceName") ? sub.get("deviceName").asText() : null;

                    JsonNode properties = sub.get("properties");
                    if (properties != null && properties.isObject()) {
                        properties.fields().forEachRemaining(entry -> {
                            String identifier = entry.getKey();
                            JsonNode valueNode = entry.getValue();
                            String value = valueNode.isObject() ? valueNode.toString() : valueNode.asText();

                            IotDeviceTelemetry telemetry = new IotDeviceTelemetry();
                            telemetry.setTs(LocalDateTime.now());
                            telemetry.setValue(value);
                            telemetry.setValueType(valueNode.isObject() ? "object" : valueNode.getNodeType().name().toLowerCase());
                            telemetry.setQuality("PACK");
                            telemetry.setDeviceId(device.getId());
                            telemetry.setProductKey(subProductKey);
                            telemetry.setPropertyId(identifier);
                            telemetry.setDeviceName(subDeviceName);
                            telemetryMapper.insert(telemetry);
                        });
                    }
                }
            }

            JsonNode properties = params.get("properties");
            if (properties != null && properties.isObject()) {
                properties.fields().forEachRemaining(entry -> {
                    String identifier = entry.getKey();
                    JsonNode valueNode = entry.getValue();
                    String value = valueNode.isObject() ? valueNode.toString() : valueNode.asText();

                    IotDeviceTelemetry telemetry = new IotDeviceTelemetry();
                    telemetry.setTs(LocalDateTime.now());
                    telemetry.setValue(value);
                    telemetry.setValueType(valueNode.isObject() ? "object" : valueNode.getNodeType().name().toLowerCase());
                    telemetry.setQuality("PACK");
                    telemetry.setDeviceId(device.getId());
                    telemetry.setProductKey(productKey);
                    telemetry.setPropertyId(identifier);
                    telemetry.setDeviceName(deviceName);
                    telemetryMapper.insert(telemetry);

                    shadowService.updateReported(device.getId(), identifier, value);
                });
            }

            log.info("网关批量上报处理完成: device={}, subDevices={}", deviceName,
                    subDevices != null ? subDevices.size() : 0);
        } catch (Exception e) {
            log.error("网关批量上报处理失败", e);
        }
    }
}
