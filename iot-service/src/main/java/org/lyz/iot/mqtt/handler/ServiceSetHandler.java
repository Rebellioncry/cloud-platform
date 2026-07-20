package org.lyz.iot.mqtt.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceLog;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.service.DeviceLogService;
import org.lyz.iot.mqtt.MqttTopicUtils;
import org.lyz.iot.service.DeviceShadowService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceSetHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IotDeviceDao deviceDao;
    private final DeviceLogService deviceLogService;
    private final DeviceShadowService shadowService;

    public void handle(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceDao.getOneIgnoreTenant(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) return;
            if (device.getStatus() != null && device.getStatus() == 3) {
                log.info("服务设置: 设备已禁用, 跳过处理. device={}", deviceName);
                return;
            }

            JsonNode params = root.get("params");
            if (params == null) return;

            params.fields().forEachRemaining(entry -> {
                String identifier = entry.getKey();
                String value = entry.getValue().isObject() ? entry.getValue().toString() : entry.getValue().asText();
                shadowService.updateDesired(device.getId(), identifier, value);
            });

            String tableDate = LocalDate.now().format(DATE_FMT);
            String messageId = root.has("id") ? root.get("id").asText() : null;

            IotDeviceLog deviceLog = new IotDeviceLog();
            deviceLog.setTs(LocalDateTime.now());
            deviceLog.setLogType("service_set");
            deviceLog.setContent("服务设置: " + params);
            deviceLog.setMessageId(messageId);
            deviceLog.setDeviceId(device.getId());
            deviceLog.setProductKey(productKey);
            deviceLog.setDeviceName(deviceName);
            deviceLog.setTableDate(tableDate);
            deviceLogService.save(deviceLog);

            log.info("服务设置处理完成: device={}, params={}", deviceName, params);
        } catch (Exception e) {
            log.error("服务设置处理失败", e);
        }
    }
}
