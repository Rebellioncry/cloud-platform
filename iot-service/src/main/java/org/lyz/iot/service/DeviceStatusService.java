package org.lyz.iot.service;

import org.lyz.iot.dto.DeviceStatusDTO;
import org.lyz.iot.entity.IotDeviceTelemetry;

import java.time.LocalDateTime;
import java.util.List;

public interface DeviceStatusService {
    DeviceStatusDTO getLatestStatus(String deviceId);
    IotDeviceTelemetry getPropertyLatest(String deviceId, String propertyId);
    List<IotDeviceTelemetry> getPropertyHistory(String deviceId, String propertyId,
                                                 LocalDateTime startTime, LocalDateTime endTime);
}
