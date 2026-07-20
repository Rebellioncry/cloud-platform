package org.lyz.iot.mqtt.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceCommand;
import org.lyz.iot.entity.IotDeviceLog;
import org.lyz.iot.dao.IotDeviceCommandDao;
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
public class ServiceInvokeHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IotDeviceDao deviceDao;
    private final IotDeviceCommandDao commandDao;
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
                log.info("服务调用: 设备已禁用, 跳过处理. device={}", deviceName);
                return;
            }

            String messageId = root.has("id") ? root.get("id").asText() : null;
            JsonNode params = root.get("params");

            IotDeviceCommand command = new IotDeviceCommand();
            command.setDeviceId(device.getId());
            command.setCommandType("function_invoke");
            command.setIdentifier(params != null && params.has("identifier") ? params.get("identifier").asText() : "");
            command.setInputData(params != null ? params.toString() : "");
            command.setStatus(0);
            command.setRequestId(messageId);
            command.setCreateTime(LocalDateTime.now());
            command.setUpdateTime(LocalDateTime.now());
            commandDao.saveIgnoreTenant(command);

            String tableDate = LocalDate.now().format(DATE_FMT);

            IotDeviceLog deviceLog = new IotDeviceLog();
            deviceLog.setTs(LocalDateTime.now());
            deviceLog.setLogType("service_invoke");
            deviceLog.setContent("服务调用: identifier=" + command.getIdentifier() + ", input=" + command.getInputData());
            deviceLog.setMessageId(messageId);
            deviceLog.setDeviceId(device.getId());
            deviceLog.setProductKey(productKey);
            deviceLog.setDeviceName(deviceName);
            deviceLog.setTableDate(tableDate);
            deviceLogService.save(deviceLog);

            log.info("服务调用处理完成: device={}, messageId={}", deviceName, messageId);
        } catch (Exception e) {
            log.error("服务调用处理失败", e);
        }
    }
}
