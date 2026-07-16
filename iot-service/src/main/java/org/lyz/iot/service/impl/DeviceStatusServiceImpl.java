package org.lyz.iot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.iot.dto.DeviceStatusDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceShadow;
import org.lyz.iot.entity.IotDeviceTelemetry;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mapper.mysql.IotDeviceShadowMapper;
import org.lyz.iot.mapper.mysql.IotProductMapper;
import org.lyz.iot.mapper.tdengine.DeviceTelemetryMapper;
import org.lyz.iot.service.DeviceStatusService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceStatusServiceImpl implements DeviceStatusService {

    private final IotDeviceMapper deviceMapper;
    private final IotProductMapper productMapper;
    private final DeviceTelemetryMapper telemetryMapper;
    private final IotDeviceShadowMapper shadowMapper;
    private final ObjectMapper objectMapper;

    @Override
    public DeviceStatusDTO getLatestStatus(String deviceId) {
        IotDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        DeviceStatusDTO dto = new DeviceStatusDTO();
        dto.setDeviceId(device.getId());
        dto.setDeviceName(device.getDeviceName());
        dto.setProductKey(device.getProductKey());
        dto.setStatus(device.getStatus());
        dto.setLastOnlineTime(device.getLastOnlineTime());
        dto.setIpAddress(device.getIpAddress());

        Map<String, DeviceStatusDTO.PropertyLatest> properties = new HashMap<>();
        IotProduct product = productMapper.selectById(device.getProductId());
        if (product != null && product.getThingModel() != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(product.getThingModel());
                JsonNode propertiesNode = rootNode.get("properties");
                if (propertiesNode != null && propertiesNode.isArray()) {
                    for (JsonNode propNode : propertiesNode) {
                        String identifier = propNode.get("identifier").asText();
                        IotDeviceTelemetry latest = telemetryMapper.selectLatest(
                                deviceId, identifier);
                        if (latest != null) {
                            DeviceStatusDTO.PropertyLatest pl = new DeviceStatusDTO.PropertyLatest();
                            pl.setIdentifier(identifier);
                            pl.setValue(latest.getValue());
                            pl.setValueType(latest.getValueType());
                            pl.setTimestamp(latest.getTs());
                            properties.put(identifier, pl);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析物模型失败: {}", e.getMessage());
            }
        }
        dto.setProperties(properties);
        return dto;
    }

    @Override
    public IotDeviceTelemetry getPropertyLatest(String deviceId, String propertyId) {
        return telemetryMapper.selectLatest(deviceId, propertyId);
    }

    @Override
    public List<IotDeviceTelemetry> getPropertyHistory(String deviceId, String propertyId,
                                                         LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryMapper.selectHistory(deviceId, propertyId, startTime, endTime);
    }
}
