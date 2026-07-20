package org.lyz.iot.mqtt.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceLog;
import org.lyz.iot.entity.IotDeviceTelemetry;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.service.DeviceLogService;
import org.lyz.iot.service.TelemetryService;
import org.lyz.iot.mqtt.MqttTopicUtils;
import org.lyz.iot.service.DeviceShadowService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyPostHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IotDeviceDao deviceDao;
    private final TelemetryService telemetryService;
    private final DeviceLogService deviceLogService;
    private final DeviceShadowService shadowService;
    private final ObjectMapper objectMapper;

    public void handle(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceDao.getOneIgnoreTenant(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("属性上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }
            if (device.getStatus() != null && device.getStatus() == 3) {
                log.info("属性上报: 设备已禁用, 跳过处理. device={}", device.getDeviceName());
                return;
            }

            JsonNode params = root.get("params");
            if (params == null || !params.isObject()) return;

            String tableDate = LocalDate.now().format(DATE_FMT);
            String messageId = root.has("id") ? root.get("id").asText() : null;

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
                telemetry.setTableDate(tableDate);
                telemetryService.save(telemetry);

                shadowService.updateReported(device.getId(), identifier, value);
            });

            device.setStatus(1);
            device.setLastOnlineTime(LocalDateTime.now());
            deviceDao.updateByIdIgnoreTenant(device);

            IotDeviceLog deviceLog = new IotDeviceLog();
            deviceLog.setTs(LocalDateTime.now());
            deviceLog.setLogType("property_post");
            deviceLog.setContent("属性上报: " + params);
            deviceLog.setMessageId(messageId);
            deviceLog.setDeviceId(device.getId());
            deviceLog.setProductKey(productKey);
            deviceLog.setDeviceName(deviceName);
            deviceLog.setTableDate(tableDate);
            deviceLogService.save(deviceLog);

            log.info("属性上报处理完成: device={}, properties={}", device.getDeviceName(), params);
        } catch (Exception e) {
            log.error("属性上报处理失败", e);
        }
    }

    public void handleHistoryPost(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceDao.getOneIgnoreTenant(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("历史属性上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }
            if (device.getStatus() != null && device.getStatus() == 3) {
                log.info("历史属性上报: 设备已禁用, 跳过处理. device={}", device.getDeviceName());
                return;
            }

            JsonNode params = root.get("params");
            if (params == null || !params.isObject()) return;

            String tableDate = LocalDate.now().format(DATE_FMT);

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
                telemetry.setTableDate(tableDate);
                telemetryService.save(telemetry);

                shadowService.updateReported(device.getId(), identifier, value);
            });

            IotDeviceLog deviceLog = new IotDeviceLog();
            deviceLog.setTs(LocalDateTime.now());
            deviceLog.setLogType("property_history");
            deviceLog.setContent("历史属性上报: " + params);
            deviceLog.setMessageId(root.has("id") ? root.get("id").asText() : null);
            deviceLog.setDeviceId(device.getId());
            deviceLog.setProductKey(productKey);
            deviceLog.setDeviceName(deviceName);
            deviceLog.setTableDate(tableDate);
            deviceLogService.save(deviceLog);

            log.info("历史属性上报处理完成: device={}, properties={}", device.getDeviceName(), params);
        } catch (Exception e) {
            log.error("历史属性上报处理失败", e);
        }
    }

    public void handleBatchPost(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceDao.getOneIgnoreTenant(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("批量属性上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }
            if (device.getStatus() != null && device.getStatus() == 3) {
                log.info("批量属性上报: 设备已禁用, 跳过处理. device={}", device.getDeviceName());
                return;
            }

            JsonNode params = root.get("params");
            if (params == null || !params.isObject()) return;

            String tableDate = LocalDate.now().format(DATE_FMT);

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
                telemetry.setTableDate(tableDate);
                telemetryService.save(telemetry);

                shadowService.updateReported(device.getId(), identifier, value);
            });

            IotDeviceLog deviceLog = new IotDeviceLog();
            deviceLog.setTs(LocalDateTime.now());
            deviceLog.setLogType("property_batch");
            deviceLog.setContent("批量属性上报: " + params);
            deviceLog.setMessageId(root.has("id") ? root.get("id").asText() : null);
            deviceLog.setDeviceId(device.getId());
            deviceLog.setProductKey(productKey);
            deviceLog.setDeviceName(deviceName);
            deviceLog.setTableDate(tableDate);
            deviceLogService.save(deviceLog);

            log.info("批量属性上报处理完成: device={}, properties={}", device.getDeviceName(), params);
        } catch (Exception e) {
            log.error("批量属性上报处理失败", e);
        }
    }

    public void handlePackPost(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceDao.getOneIgnoreTenant(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) {
                log.warn("网关批量上报: 未找到设备. productKey={}, deviceName={}", productKey, deviceName);
                return;
            }
            if (device.getStatus() != null && device.getStatus() == 3) {
                log.info("网关批量上报: 设备已禁用, 跳过处理. device={}", device.getDeviceName());
                return;
            }

            JsonNode params = root.get("params");
            if (params == null) return;

            String tableDate = LocalDate.now().format(DATE_FMT);

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
                            telemetry.setTableDate(tableDate);
                            telemetryService.save(telemetry);
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
                    telemetry.setTableDate(tableDate);
                    telemetryService.save(telemetry);

                    shadowService.updateReported(device.getId(), identifier, value);
                });
            }

            IotDeviceLog deviceLog = new IotDeviceLog();
            deviceLog.setTs(LocalDateTime.now());
            deviceLog.setLogType("property_pack");
            deviceLog.setContent("网关批量上报: " + root.get("params"));
            deviceLog.setMessageId(root.has("id") ? root.get("id").asText() : null);
            deviceLog.setDeviceId(device.getId());
            deviceLog.setProductKey(productKey);
            deviceLog.setDeviceName(deviceName);
            deviceLog.setTableDate(tableDate);
            deviceLogService.save(deviceLog);

            log.info("网关批量上报处理完成: device={}, subDevices={}", deviceName,
                    subDevices != null ? subDevices.size() : 0);
        } catch (Exception e) {
            log.error("网关批量上报处理失败", e);
        }
    }
}
