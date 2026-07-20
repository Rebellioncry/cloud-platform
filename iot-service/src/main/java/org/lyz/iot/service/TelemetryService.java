package org.lyz.iot.service;

import org.lyz.iot.entity.IotDeviceTelemetry;

public interface TelemetryService {

    void save(IotDeviceTelemetry telemetry);
}
