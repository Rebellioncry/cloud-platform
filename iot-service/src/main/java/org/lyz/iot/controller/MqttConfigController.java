package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.MqttConfigDTO;
import org.lyz.iot.entity.IotMqttConfig;
import org.lyz.iot.service.MqttConfigService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MQTT客户端配置")
@RestController
@RequestMapping("/mqtt/config")
@RequiredArgsConstructor
public class MqttConfigController {

    private final MqttConfigService mqttConfigService;

    @Operation(summary = "配置列表")
    @GetMapping
    public Result<PageResult<IotMqttConfig>> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                                   @RequestParam(value = "size", defaultValue = "10") int size,
                                                   @RequestParam(value = "name", required = false) String name) {
        return Result.success(mqttConfigService.list(page, size, name));
    }

    @Operation(summary = "配置详情")
    @GetMapping("/{id}")
    public Result<IotMqttConfig> getById(@PathVariable(value = "id") String id) {
        return Result.success(mqttConfigService.getById(id));
    }

    @Operation(summary = "创建配置")
    @PostMapping
    public Result<IotMqttConfig> create(@RequestBody MqttConfigDTO dto) {
        return Result.success(mqttConfigService.create(dto));
    }

    @Operation(summary = "更新配置")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable(value = "id") String id, @RequestBody MqttConfigDTO dto) {
        mqttConfigService.update(id, dto);
        return Result.success(null);
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable(value = "id") String id) {
        mqttConfigService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "启动MQTT客户端")
    @PostMapping("/{id}/start")
    public Result<Void> startClient(@PathVariable(value = "id") String id) {
        mqttConfigService.startClient(id);
        return Result.success(null);
    }

    @Operation(summary = "停止MQTT客户端")
    @PostMapping("/{id}/stop")
    public Result<Void> stopClient(@PathVariable(value = "id") String id) {
        mqttConfigService.stopClient(id);
        return Result.success(null);
    }
}
