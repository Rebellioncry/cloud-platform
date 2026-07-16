package org.lyz.iot.service;

import org.lyz.iot.dto.DeviceShadowDTO;

import java.util.List;

public interface DeviceShadowService {
    List<DeviceShadowDTO> getShadow(String deviceId);
    void updateDesired(String deviceId, String identifier, String value);
    void updateReported(String deviceId, String identifier, String value);
    List<DeviceShadowDTO> getDiff(String deviceId);
}
