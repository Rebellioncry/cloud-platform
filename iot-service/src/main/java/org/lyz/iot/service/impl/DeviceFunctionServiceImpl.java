package org.lyz.iot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lyz.common.core.exception.BusinessException;
import org.lyz.iot.dto.DeviceFunctionDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.entity.IotDeviceCommand;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.dao.IotDeviceCommandDao;
import org.lyz.iot.dao.IotDeviceDao;
import org.lyz.iot.dao.IotProductDao;
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

    private final IotDeviceDao deviceDao;
    private final IotProductDao productDao;
    private final IotDeviceCommandDao commandDao;
    private final ObjectMapper objectMapper;
    private final CommandEventPublisher commandEventPublisher;

    @Override
    public List<DeviceFunctionDTO> listFunctions(String deviceId) {
        IotDevice device = deviceDao.getById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        IotProduct product = productDao.getById(device.getProductId());
        List<DeviceFunctionDTO> functions = new ArrayList<>();
        if (product == null || product.getThingModel() == null) {
            return functions;
        }
        try {
            JsonNode root = objectMapper.readTree(product.getThingModel());

            JsonNode servicesNode = root.get("services");
            if (servicesNode != null && servicesNode.isArray()) {
                for (JsonNode node : servicesNode) {
                    DeviceFunctionDTO dto = new DeviceFunctionDTO();
                    dto.setIdentifier(node.get("identifier").asText());
                    dto.setName(node.get("name").asText());
                    dto.setType("service");
                    dto.setCallType(node.has("callType") ? node.get("callType").asText() : "async");
                    dto.setSpecs(node.has("inputData") ? node.get("inputData").toString() : "[]");
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
        IotDevice device = deviceDao.getById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (device.getStatus() != null && device.getStatus() == 3) {
            throw new BusinessException("设备已禁用，无法调用功能");
        }
        IotDeviceCommand command = new IotDeviceCommand();
        command.setDeviceId(deviceId);
        command.setCommandType("function_invoke");
        command.setIdentifier(identifier);
        command.setInputData(input);
        command.setStatus(0);
        command.setRequestId(UUID.randomUUID().toString());
        command.setCreateTime(LocalDateTime.now());
        command.setUpdateTime(LocalDateTime.now());
        commandDao.save(command);

        commandEventPublisher.publishCommand(command);

        log.info("设备功能调用指令已创建: deviceId={}, identifier={}, requestId={}",
                deviceId, identifier, command.getRequestId());
    }
}
