package org.lyz.iot.mqtt.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mqtt.MqttTopicUtils;
import org.lyz.iot.service.DeviceShadowService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceSetHandler {

    private final IotDeviceMapper deviceMapper;
    private final DeviceShadowService shadowService;

    public void handle(String topic, JsonNode root) {
        try {
            String productKey = MqttTopicUtils.extractProductKey(topic);
            String deviceName = MqttTopicUtils.extractDeviceName(topic);

            IotDevice device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<IotDevice>()
                            .eq(IotDevice::getProductKey, productKey)
                            .eq(IotDevice::getDeviceName, deviceName));
            if (device == null) return;

            JsonNode params = root.get("params");
            if (params == null) return;

            params.fields().forEachRemaining(entry -> {
                String identifier = entry.getKey();
                String value = entry.getValue().isObject() ? entry.getValue().toString() : entry.getValue().asText();
                shadowService.updateDesired(device.getId(), identifier, value);
            });

            log.info("服务设置处理完成: device={}, params={}", deviceName, params);
        } catch (Exception e) {
            log.error("服务设置处理失败", e);
        }
    }
}
