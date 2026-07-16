package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.DeviceLogDTO;
import org.lyz.iot.service.DeviceLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "设备日志")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceLogController {

    private final DeviceLogService logService;

    @Operation(summary = "设备日志列表")
    @GetMapping("/{id}/log")
    public Result<PageResult<DeviceLogDTO>> listLogs(
            @PathVariable(value = "id") String deviceId,
            @RequestParam(value = "logType", required = false) String logType,
            @RequestParam(value = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        if (startTime == null) startTime = LocalDateTime.now().minusDays(7);
        if (endTime == null) endTime = LocalDateTime.now();
        return Result.success(logService.listLogs(deviceId, logType, startTime, endTime, page, size));
    }
}
