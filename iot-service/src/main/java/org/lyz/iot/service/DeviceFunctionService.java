package org.lyz.iot.service;

import org.lyz.iot.dto.DeviceFunctionDTO;

import java.util.List;

public interface DeviceFunctionService {
    List<DeviceFunctionDTO> listFunctions(String deviceId);
    void invokeFunction(String deviceId, String identifier, String input);
}
