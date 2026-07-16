package org.lyz.iot.mqtt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.entity.IotMqttConfig;
import org.lyz.iot.mapper.mysql.IotMqttConfigMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqttAutoReconnect implements ApplicationRunner {

    private final IotMqttConfigMapper mqttConfigMapper;
    private final MqttClientManager mqttClientManager;

    @Override
    public void run(ApplicationArguments args) {
        LambdaQueryWrapper<IotMqttConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotMqttConfig::getStatus, 1);
        List<IotMqttConfig> configs = mqttConfigMapper.selectList(wrapper);

        if (configs.isEmpty()) {
            return;
        }

        log.info("发现{}个运行中的MQTT配置，开始重连...", configs.size());
        for (IotMqttConfig config : configs) {
            if (mqttClientManager.isRunning(config.getId())) {
                log.info("MQTT客户端已在运行，跳过: id={}", config.getId());
                continue;
            }
            try {
                mqttClientManager.start(config);
                log.info("MQTT客户端重连成功: id={}, name={}", config.getId(), config.getName());
            } catch (Exception e) {
                // 不修改 DB status，其他实例可能已经在运行
                log.error("MQTT客户端重连失败: id={}, name={}, error={}", config.getId(), config.getName(), e.getMessage());
            }
        }
    }
}
