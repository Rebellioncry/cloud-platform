package org.lyz.iot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.context.SecurityUtils;
import org.lyz.common.core.context.TenantContext;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.iot.dto.DeviceFunctionDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceCommand;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.mapper.mysql.IotDeviceCommandMapper;
import org.lyz.iot.mapper.mysql.IotDeviceMapper;
import org.lyz.iot.mapper.mysql.IotProductMapper;
import org.lyz.iot.mqtt.CommandEventPublisher;
import org.lyz.iot.service.DeviceFunctionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceFunctionServiceImpl implements DeviceFunctionService {

    private final IotDeviceMapper deviceMapper;
    private final IotProductMapper productMapper;
    private final IotDeviceCommandMapper commandMapper;
    private final ObjectMapper objectMapper;
    private final CommandEventPublisher commandEventPublisher;

    @Override
    public List<DeviceFunctionDTO> listFunctions(String deviceId) {
        IotDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (!SecurityUtils.isSuperAdmin()) {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(device.getTenantId())) {
                throw new BusinessException("无权访问该设备");
            }
        }
        IotProduct product = productMapper.selectById(device.getProductId());
        List<DeviceFunctionDTO> functions = new ArrayList<>();
        if (product == null || product.getThingModel() == null) {
            return functions;
        }
        try {
            JsonNode root = objectMapper.readTree(product.getThingModel());

            JsonNode propertiesNode = root.get("properties");
            if (propertiesNode != null && propertiesNode.isArray()) {
                for (JsonNode node : propertiesNode) {
                    DeviceFunctionDTO dto = new DeviceFunctionDTO();
                    dto.setIdentifier(node.get("identifier").asText());
                    dto.setName(node.get("name").asText());
                    dto.setType("property");
                    dto.setAccessMode(node.has("accessMode") ? node.get("accessMode").asText() : "r");
                    JsonNode dt = node.get("dataType");
                    if (dt != null) {
                        dto.setDataType(dt.has("type") ? dt.get("type").asText() : "");
                        dto.setSpecs(dt.has("specs") ? dt.get("specs").toString() : "");
                    }
                    functions.add(dto);
                }
            }

            JsonNode servicesNode = root.get("services");
            if (servicesNode != null && servicesNode.isArray()) {
                for (JsonNode node : servicesNode) {
                    DeviceFunctionDTO dto = new DeviceFunctionDTO();
                    dto.setIdentifier(node.get("identifier").asText());
                    dto.setName(node.get("name").asText());
                    dto.setType("service");
                    dto.setCallType(node.has("callType") ? node.get("callType").asText() : "async");
                    JsonNode input = node.get("inputData");
                    dto.setSpecs(input != null ? input.toString() : "");
                    functions.add(dto);
                }
            }

            JsonNode eventsNode = root.get("events");
            if (eventsNode != null && eventsNode.isArray()) {
                for (JsonNode node : eventsNode) {
                    DeviceFunctionDTO dto = new DeviceFunctionDTO();
                    dto.setIdentifier(node.get("identifier").asText());
                    dto.setName(node.get("name").asText());
                    dto.setType("event");
                    JsonNode output = node.get("outputData");
                    dto.setSpecs(output != null ? output.toString() : "");
                    functions.add(dto);
                }
            }
        } catch (Exception e) {
            log.warn("解析物模型失败: {}", e.getMessage());
        }
        return functions;
    }

    @Override
    public void invokeFunction(String deviceId, String identifier, String input) {
        IotDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (!SecurityUtils.isSuperAdmin()) {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(device.getTenantId())) {
                throw new BusinessException("无权访问该设备");
            }
        }
        IotDeviceCommand command = new IotDeviceCommand();
        command.setTenantId(TenantContext.getTenantId());
        command.setDeviceId(deviceId);
        command.setCommandType("function_invoke");
        command.setIdentifier(identifier);
        command.setInputData(input);
        command.setStatus(0);
        command.setRequestId(UUID.randomUUID().toString());
        command.setCreateTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());
        commandMapper.insert(command);

        commandEventPublisher.publishCommand(command);

        log.info("设备功能调用指令已创建: deviceId={}, identifier={}, requestId={}",
                deviceId, identifier, command.getRequestId());
    }
}
