package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.RuleDTO;
import org.lyz.iot.entity.IotRule;
import org.lyz.iot.rule.engine.RuleEngineService;
import org.lyz.iot.service.RuleService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "规则引擎")
@RestController
@RequestMapping("/rule")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;
    private final RuleEngineService ruleEngineService;

    @Operation(summary = "规则列表")
    @GetMapping
    public Result<PageResult<IotRule>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name", required = false) String name) {
        return Result.success(ruleService.list(page, size, name));
    }

    @Operation(summary = "规则详情")
    @GetMapping("/{id}")
    public Result<IotRule> getById(@PathVariable(value = "id") String id) {
        return Result.success(ruleService.getById(id));
    }

    @Operation(summary = "创建规则")
    @PostMapping
    public Result<IotRule> create(@RequestBody RuleDTO dto) {
        return Result.success(ruleService.create(dto));
    }

    @Operation(summary = "更新规则")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable(value = "id") String id, @RequestBody RuleDTO dto) {
        ruleService.update(id, dto);
        return Result.success(null);
    }

    @Operation(summary = "删除规则")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable(value = "id") String id) {
        ruleService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "启动规则")
    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable(value = "id") String id) {
        ruleService.start(id);
        return Result.success(null);
    }

    @Operation(summary = "停止规则")
    @PostMapping("/{id}/stop")
    public Result<Void> stop(@PathVariable(value = "id") String id) {
        ruleService.stop(id);
        return Result.success(null);
    }

    @Operation(summary = "手动触发测试")
    @PostMapping("/{id}/test-trigger")
    public Result<Void> testTrigger(
            @PathVariable(value = "id") String id,
            @RequestParam(value = "productKey", defaultValue = "test-product") String productKey,
            @RequestParam(value = "deviceName", defaultValue = "test-device") String deviceName) {
        ruleEngineService.triggerTest(id, productKey, deviceName);
        return Result.success(null);
    }
}
