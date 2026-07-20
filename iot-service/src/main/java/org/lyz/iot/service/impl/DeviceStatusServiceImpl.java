package org.lyz.iot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.iot.dto.DeviceStatusDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceLog;
import org.lyz.iot.entity.IotDeviceShadow;
import org.lyz.iot.entity.IotDeviceTelemetry;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.dao.IotDeviceShadowDao;
import org.lyz.iot.dao.IotProductDao;
import org.lyz.iot.mapper.tdengine.DeviceLogMapper;
import org.lyz.iot.mapper.tdengine.DeviceTelemetryMapper;
import org.lyz.iot.service.DeviceStatusService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceStatusServiceImpl implements DeviceStatusService {

    private final IotDeviceDao deviceDao;
    private final IotProductDao productDao;
    private final DeviceTelemetryMapper telemetryMapper;
    private final DeviceLogMapper deviceLogMapper;
    private final IotDeviceShadowDao shadowDao;
    private final ObjectMapper objectMapper;

    @Override
    public DeviceStatusDTO getLatestStatus(String deviceId) {
        IotDevice device = deviceDao.getById(deviceId);
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

        Map<String, DeviceStatusDTO.PropertyLatest> properties = new LinkedHashMap<>();
        IotProduct product = productDao.getById(device.getProductId());
        if (product != null && product.getThingModel() != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(product.getThingModel());
                JsonNode propertiesNode = rootNode.get("properties");
                if (propertiesNode != null && propertiesNode.isArray()) {
                    for (JsonNode propNode : propertiesNode) {
                        String identifier = propNode.get("identifier").asText();
                        DeviceStatusDTO.PropertyLatest pl = new DeviceStatusDTO.PropertyLatest();
                        pl.setIdentifier(identifier);
                        pl.setName(propNode.has("name") ? propNode.get("name").asText() : identifier);
                        pl.setAccessMode(propNode.has("accessMode") ? propNode.get("accessMode").asText() : "r");
                        JsonNode dt = propNode.get("dataType");
                        if (dt != null) {
                            pl.setDataType(dt.has("type") ? dt.get("type").asText() : "");
                        }
                        try {
                            IotDeviceTelemetry latest = telemetryMapper.selectLatest(deviceId, identifier);
                            if (latest != null) {
                                pl.setValue(latest.getValue());
                                pl.setValueType(latest.getValueType());
                                pl.setTimestamp(latest.getTs());
                            }
                        } catch (Exception e) {
                            log.warn("查询属性最新值失败: identifier={}, error={}", identifier, e.getMessage());
                        }
                        properties.put(identifier, pl);
                    }
                }
            } catch (Exception e) {
                log.warn("解析物模型失败: {}", e.getMessage());
            }
        }
        dto.setProperties(properties);

        Map<String, DeviceStatusDTO.EventLatest> events = new LinkedHashMap<>();
        if (product != null && product.getThingModel() != null) {
            try {
                JsonNode rootNode = objectMapper.readTree(product.getThingModel());
                JsonNode eventsNode = rootNode.get("events");
                if (eventsNode != null && eventsNode.isArray()) {
                    for (JsonNode evtNode : eventsNode) {
                        String identifier = evtNode.get("identifier").asText();
                        DeviceStatusDTO.EventLatest el = new DeviceStatusDTO.EventLatest();
                        el.setIdentifier(identifier);
                        el.setName(evtNode.has("name") ? evtNode.get("name").asText() : identifier);
                        el.setEventType(evtNode.has("type") ? evtNode.get("type").asText() : "info");
                        try {
                            List<IotDeviceLog> latestEvents = deviceLogMapper.selectLatestEvents(deviceId, 50);
                            for (IotDeviceLog logEntry : latestEvents) {
                                if (logEntry.getContent() != null && logEntry.getContent().contains("\"identifier\":\"" + identifier + "\"")) {
                                    el.setOutputValues(logEntry.getContent());
                                    el.setTimestamp(logEntry.getTs());
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            log.warn("查询事件最新值失败: identifier={}, error={}", identifier, e.getMessage());
                        }
                        events.put(identifier, el);
                    }
                }
            } catch (Exception e) {
                log.warn("解析物模型事件失败: {}", e.getMessage());
            }
        }
        dto.setEvents(events);
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
