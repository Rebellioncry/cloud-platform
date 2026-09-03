package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.DeviceDTO;
import org.lyz.iot.entity.IotDevice;
import org.lyz.iot.service.DeviceService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "设备管理")
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "设备列表")
    @GetMapping
    public Result<PageResult<IotDevice>> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                               @RequestParam(value = "size", defaultValue = "10") int size,
                                               @RequestParam(value = "productId", required = false) String productId,
                                               @RequestParam(value = "name", required = false) String name,
                                               @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(deviceService.list(page, size, productId, name, status));
    }

    @Operation(summary = "设备详情")
    @GetMapping("/{id}")
    public Result<IotDevice> getById(@PathVariable(value = "id") String id) {
        return Result.success(deviceService.getById(id));
    }

    @Operation(summary = "创建设备")
    @PostMapping
    public Result<IotDevice> create(@RequestBody DeviceDTO dto) {
        return Result.success(deviceService.create(dto));
    }

    @Operation(summary = "更新设备")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable(value = "id") String id, @RequestBody DeviceDTO dto) {
        deviceService.update(id, dto);
        return Result.success(null);
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable(value = "id") String id) {
        deviceService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "激活设备")
    @PostMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable(value = "id") String id) {
        deviceService.activate(id);
        return Result.success(null);
    }

    @Operation(summary = "禁用设备")
    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable(value = "id") String id) {
        deviceService.disable(id);
        return Result.success(null);
    }
}
