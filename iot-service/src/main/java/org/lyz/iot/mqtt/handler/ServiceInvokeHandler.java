package org.lyz.iot.mqtt.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceCommand;
import org.lyz.iot.mapper.mysql.IotDeviceCommandMapper;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mqtt.MqttTopicUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceInvokeHandler {

    private final IotDeviceMapper deviceMapper;
    private final IotDeviceCommandMapper commandMapper;

    public void handle(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) return;

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
            commandMapper.insert(command);

            log.info("服务调用处理完成: device={}, messageId={}", deviceName, messageId);
        } catch (Exception e) {
            log.error("服务调用处理失败", e);
        }
    }
}
