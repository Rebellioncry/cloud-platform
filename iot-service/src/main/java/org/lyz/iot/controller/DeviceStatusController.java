package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.DeviceStatusDTO;
import org.lyz.iot.entity.IotDeviceTelemetry;
import org.lyz.iot.service.DeviceStatusService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "设备运行状态")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceStatusController {

    private final DeviceStatusService statusService;

    @Operation(summary = "设备全部最新状态")
    @GetMapping("/{id}/status")
    public Result<DeviceStatusDTO> getLatestStatus(@PathVariable(value = "id") String deviceId) {
        return Result.success(statusService.getLatestStatus(deviceId));
    }

    @Operation(summary = "属性最新值")
    @GetMapping("/{id}/status/property/{propertyId}")
    public Result<IotDeviceTelemetry> getPropertyLatest(@PathVariable(value = "id") String deviceId,
                                                         @PathVariable(value = "propertyId") String propertyId) {
        return Result.success(statusService.getPropertyLatest(deviceId, propertyId));
    }

    @Operation(summary = "属性历史值")
    @GetMapping("/{id}/status/property/{propertyId}/history")
    public Result<List<IotDeviceTelemetry>> getPropertyHistory(
            @PathVariable(value = "id") String deviceId,
            @PathVariable String propertyId,
            @RequestParam(value = "startTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(value = "endTime") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(statusService.getPropertyHistory(deviceId, propertyId, startTime, endTime));
    }
}
