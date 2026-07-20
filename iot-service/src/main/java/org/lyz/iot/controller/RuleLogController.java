package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.entity.IotRuleExecLog;
import org.lyz.iot.service.RuleLogService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "规则执行日志")
@RestController
@RequestMapping("/rule/log")
@RequiredArgsConstructor
public class RuleLogController {

    private final RuleLogService ruleLogService;

    @Operation(summary = "日志列表")
    @GetMapping("/list")
    public Result<PageResult<IotRuleExecLog>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "ruleId", required = false) String ruleId,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(ruleLogService.list(page, size, ruleId, status));
    }
}
