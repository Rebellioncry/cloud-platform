package org.lyz.iot.mqtt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceCommand;
import org.lyz.iot.entity.IotMqttConfig;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mapper.mysql.IotMqttConfigMapper;
import org.lyz.iot.mapper.mysql.IotProductMapper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandEventPublisher {

    private final IotDeviceMapper deviceMapper;
    private final IotProductMapper productMapper;
    private final IotMqttConfigMapper mqttConfigMapper;
    private final MqttClientManager mqttClientManager;

    public void publishCommand(IotDeviceCommand command) {
        IotDevice device = deviceMapper.selectById(command.getDeviceId());
        if (device == null) {
            log.warn("命令下发: 设备不存在, deviceId={}", command.getDeviceId());
            return;
        }

        IotProduct product = productMapper.selectById(device.getProductId());
        if (product == null) {
            log.warn("命令下发: 产品不存在, productId={}", device.getProductId());
            return;
        }

        // 找到第一个运行中的 MQTT config
        IotMqttConfig mqttConfig = findActiveMqttConfig();
        if (mqttConfig == null) {
            log.warn("命令下发: 无可用MQTT连接, commandId={}", command.getId());
            return;
        }

        String topic;
        String payload;

        if ("property_write".equals(command.getCommandType()) || "property_set".equals(command.getCommandType())) {
            topic = MqttTopicConstants.buildServicePropertySetTopic(product.getProductKey(), device.getDeviceName());
            payload = String.format("{\"id\":\"%s\",\"method\":\"thing.service.property.set\",\"params\":{\"%s\":%s}}",
                    command.getRequestId(), command.getIdentifier(), command.getInputData());
        } else {
            topic = MqttTopicConstants.buildServiceInvokeTopic(product.getProductKey(), device.getDeviceName(), command.getIdentifier());
            payload = String.format("{\"id\":\"%s\",\"method\":\"thing.service.invoke\",\"params\":{\"identifier\":\"%s\",\"input\":%s}}",
                    command.getRequestId(), command.getIdentifier(), command.getInputData());
        }

        mqttClientManager.publish(mqttConfig.getId(), topic, payload, mqttConfig.getQos());
        log.info("命令已下发: commandId={}, device={}, topic={}", command.getId(), device.getDeviceName(), topic);
    }

    private IotMqttConfig findActiveMqttConfig() {
        LambdaQueryWrapper<IotMqttConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotMqttConfig::getStatus, 1);
        wrapper.last("LIMIT 1");
        return mqttConfigMapper.selectOne(wrapper);
    }
}
