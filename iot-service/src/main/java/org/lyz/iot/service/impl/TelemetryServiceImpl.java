package org.lyz.iot.service.impl;

import lombok.RequiredArgsConstructor;
import org.lyz.iot.entity.IotDeviceTelemetry;
import org.lyz.iot.mapper.tdengine.DeviceTelemetryMapper;
import org.lyz.iot.service.TelemetryService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final DeviceTelemetryMapper telemetryMapper;

    @Override
    public void save(IotDeviceTelemetry telemetry) {
        telemetryMapper.insert(telemetry);
    }
}
