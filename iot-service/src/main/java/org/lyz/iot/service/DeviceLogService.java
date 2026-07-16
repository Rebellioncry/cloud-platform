package org.lyz.iot.service;

import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.DeviceLogDTO;

import java.time.LocalDateTime;

public interface DeviceLogService {
    PageResult<DeviceLogDTO> listLogs(String deviceId, String logType,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       int page, int size);
}
