package org.lyz.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.context.TenantHelper;
import org.lyz.common.core.context.UserContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.MqttConfigDTO;
import org.lyz.iot.entity.IotMqttConfig;
import org.lyz.iot.dao.IotMqttConfigDao;
import org.lyz.iot.mqtt.MqttClientManager;
import org.lyz.iot.mqtt.MqttEventPublisher;
import org.lyz.iot.mqtt.MqttTopicConstants;
import org.lyz.iot.service.MqttConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttConfigServiceImpl implements MqttConfigService {

    private final IotMqttConfigDao mqttConfigDao;
    private final MqttClientManager mqttClientManager;
    private final MqttEventPublisher mqttEventPublisher;

    @Override
    public PageResult<IotMqttConfig> list(int page, int size, String name) {
        Page<IotMqttConfig> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IotMqttConfig> wrapper = new LambdaQueryWrapper<>();
        if (!UserContext.isPlatformAdmin()) {
            String tenantId = UserContext.getTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                wrapper.eq(IotMqttConfig::getTenantId, tenantId);
            }
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like(IotMqttConfig::getName, name);
        }
        wrapper.orderByDesc(IotMqttConfig::getCreateTime);
        IPage<IotMqttConfig>[] resultRef = new IPage[1];
        TenantHelper.ignore(() -> resultRef[0] = mqttConfigDao.page(pageParam, wrapper));
        return PageResult.of(resultRef[0].getTotal(), page, size, resultRef[0].getRecords());
    }

    @Override
    public IotMqttConfig getById(String id) {
        IotMqttConfig[] ref = new IotMqttConfig[1];
        TenantHelper.ignore(() -> ref[0] = mqttConfigDao.getById(id));
        if (ref[0] == null) {
            throw new BusinessException("MQTT配置不存在");
        }
        return ref[0];
    }

    @Override
    @Transactional
    public IotMqttConfig create(MqttConfigDTO dto) {
        IotMqttConfig config = new IotMqttConfig();
        String tenantId = UserContext.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            config.setTenantId(tenantId);
        }
        config.setName(dto.getName());
        config.setDescription(dto.getDescription());
        config.setBroker(dto.getBroker());
        config.setPort(dto.getPort() != null ? dto.getPort() : 1883);
        config.setUsername(dto.getUsername());
        config.setPassword(dto.getPassword());
        config.setClientIdPrefix(dto.getClientIdPrefix() != null ? dto.getClientIdPrefix() : "iot-service");
        config.setSharedGroup(dto.getSharedGroup() != null ? dto.getSharedGroup() : MqttTopicConstants.SHARE_GROUP_DEFAULT);
        config.setQos(dto.getQos() != null ? dto.getQos() : 1);
        config.setKeepAlive(dto.getKeepAlive() != null ? dto.getKeepAlive() : 60);
        config.setAutoReconnect(dto.getAutoReconnect() != null ? dto.getAutoReconnect() : 1);
        config.setUseSsl(dto.getUseSsl() != null ? dto.getUseSsl() : 0);
        config.setStatus(0);
        mqttConfigDao.save(config);
        return config;
    }

    @Override
    @Transactional
    public void update(String id, MqttConfigDTO dto) {
        IotMqttConfig config = getById(id);
        if (config.getStatus() == 1) {
            throw new BusinessException("运行中的配置不能修改，请先停止");
        }
        if (dto.getName() != null) config.setName(dto.getName());
        if (dto.getDescription() != null) config.setDescription(dto.getDescription());
        if (dto.getBroker() != null) config.setBroker(dto.getBroker());
        if (dto.getPort() != null) config.setPort(dto.getPort());
        if (dto.getUsername() != null) config.setUsername(dto.getUsername());
        if (dto.getPassword() != null) config.setPassword(dto.getPassword());
        if (dto.getClientIdPrefix() != null) config.setClientIdPrefix(dto.getClientIdPrefix());
        if (dto.getSharedGroup() != null) config.setSharedGroup(dto.getSharedGroup());
        if (dto.getQos() != null) config.setQos(dto.getQos());
        if (dto.getKeepAlive() != null) config.setKeepAlive(dto.getKeepAlive());
        if (dto.getAutoReconnect() != null) config.setAutoReconnect(dto.getAutoReconnect());
        if (dto.getUseSsl() != null) config.setUseSsl(dto.getUseSsl());
        mqttConfigDao.updateById(config);
    }

    @Override
    @Transactional
    public void delete(String id) {
        IotMqttConfig config = getById(id);
        if (config.getStatus() == 1) {
            mqttEventPublisher.publishStop(id);
        }
        mqttConfigDao.removeById(id);
    }

    @Override
    public void startClient(String id) {
        IotMqttConfig config = getById(id);
        config.setStatus(1);
        mqttConfigDao.updateByIdIgnoreTenant(config);
        mqttEventPublisher.publishStart(id);
        log.info("MQTT客户端启动指令已发送: id={}, name={}", id, config.getName());
    }

    @Override
    public void stopClient(String id) {
        IotMqttConfig config = getById(id);
        config.setStatus(0);
        mqttConfigDao.updateByIdIgnoreTenant(config);
        mqttEventPublisher.publishStop(id);
        log.info("MQTT客户端停止指令已发送: id={}, name={}", id, config.getName());
    }
}
