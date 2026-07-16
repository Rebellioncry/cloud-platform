package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.MqttConfigDTO;
import org.lyz.iot.entity.IotMqttConfig;

public interface MqttConfigService {
    PageResult<IotMqttConfig> list(int page, int size, String name);
    IotMqttConfig getById(String id);
    IotMqttConfig create(MqttConfigDTO dto);
    void update(String id, MqttConfigDTO dto);
    void delete(String id);
    void startClient(String id);
    void stopClient(String id);
}
