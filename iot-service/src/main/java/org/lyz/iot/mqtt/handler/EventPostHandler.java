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
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPostHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IotDeviceDao deviceDao;
    private final DeviceLogService deviceLogService;

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
                log.info("事件上报: 设备已禁用, 跳过处理. device={}", deviceName);
                return;
            }

            String identifier = root.has("params") && root.get("params").has("identifier")
                    ? root.get("params").get("identifier").asText() : "unknown";
            String messageId = root.has("id") ? root.get("id").asText() : null;

            IotDeviceLog deviceLog = new IotDeviceLog();
            deviceLog.setTs(LocalDateTime.now());
            deviceLog.setLogType("event");
            deviceLog.setContent("{\"identifier\":\"" + identifier + "\",\"data\":" + root.get("params") + "}");
            deviceLog.setMessageId(messageId);
            deviceLog.setDeviceId(device.getId());
            deviceLog.setProductKey(productKey);
            deviceLog.setDeviceName(deviceName);
            deviceLog.setTableDate(LocalDate.now().format(DATE_FMT));
            deviceLogService.save(deviceLog);

            log.info("事件处理完成: device={}, event={}", deviceName, identifier);
        } catch (Exception e) {
            log.error("事件处理失败", e);
        }
    }
}
