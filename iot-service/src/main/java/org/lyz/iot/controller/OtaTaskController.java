package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.OtaTaskDTO;
import org.lyz.iot.entity.IotOtaTask;
import org.lyz.iot.entity.IotOtaTaskDevice;
import org.lyz.iot.service.OtaTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "OTA升级")
@RestController
@RequestMapping("/ota/task")
@RequiredArgsConstructor
public class OtaTaskController {

    private final OtaTaskService otaTaskService;

    @Operation(summary = "任务列表")
    @GetMapping
    public Result<PageResult<IotOtaTask>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "taskName", required = false) String taskName) {
        return Result.success(otaTaskService.list(page, size, taskName));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public Result<IotOtaTask> getById(@PathVariable(value = "id") String id) {
        return Result.success(otaTaskService.getById(id));
    }

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<IotOtaTask> create(@RequestBody OtaTaskDTO dto) {
        return Result.success(otaTaskService.create(dto));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable(value = "id") String id) {
        otaTaskService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "启动任务")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable(value = "id") String id) {
        otaTaskService.start(id);
        return Result.success(null);
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable(value = "id") String id) {
        otaTaskService.cancel(id);
        return Result.success(null);
    }

    @Operation(summary = "获取任务设备列表")
    @GetMapping("/{id}/devices")
    public Result<List<IotOtaTaskDevice>> listDevices(@PathVariable(value = "id") String id) {
        return Result.success(otaTaskService.listTaskDevices(id));
    }

    @Operation(summary = "重试失败设备")
    @PostMapping("/{id}/retry")
    public Result<Void> retry(
            @PathVariable(value = "id") String id,
            @RequestBody(required = false) List<String> deviceIds) {
        otaTaskService.retryDevices(id, deviceIds);
        return Result.success(null);
    }

    @Operation(summary = "设备上报升级进度")
    @PostMapping("/device/progress")
    public Result<Void> reportProgress(
            @RequestParam(value = "taskId") String taskId,
            @RequestParam(value = "deviceName") String deviceName,
            @RequestParam(value = "progress", defaultValue = "0") Integer progress,
            @RequestParam(value = "status") String status,
            @RequestParam(value = "errorMessage", required = false) String errorMessage) {
        otaTaskService.updateDeviceProgress(taskId, deviceName, progress, status, errorMessage);
        return Result.success(null);
    }
}
