package org.lyz.iot.service.impl;

import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.iot.dto.DeviceLogDTO;
import org.lyz.iot.entity.IotDeviceLog;
import org.lyz.iot.mapper.tdengine.DeviceLogMapper;
import org.lyz.iot.service.DeviceLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceLogServiceImpl implements DeviceLogService {

    private final DeviceLogMapper deviceLogMapper;

    @Override
    public PageResult<DeviceLogDTO> listLogs(String deviceId, String logType,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               int page, int size) {
        List<IotDeviceLog> allLogs = deviceLogMapper.selectByDevice(
                deviceId, logType, startTime, endTime);

        long total = allLogs.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allLogs.size());
        List<IotDeviceLog> pageData = fromIndex < allLogs.size()
                ? allLogs.subList(fromIndex, toIndex)
                : List.of();

        List<DeviceLogDTO> dtoList = pageData.stream().map(l -> {
            DeviceLogDTO dto = new DeviceLogDTO();
            dto.setLogType(l.getLogType());
            dto.setContent(l.getContent());
            dto.setMessageId(l.getMessageId());
            dto.setTimestamp(l.getTs());
            return dto;
        }).collect(Collectors.toList());

        return PageResult.of(total, page, size, dtoList);
    }

    @Override
    public void save(IotDeviceLog deviceLog) {
        deviceLogMapper.insert(deviceLog);
    }
}
