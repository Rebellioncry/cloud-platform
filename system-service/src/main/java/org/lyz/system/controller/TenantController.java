package org.lyz.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.TenantDTO;
import org.lyz.system.entity.SysTenant;
import org.lyz.system.service.TenantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/tenant")
@RequiredArgsConstructor
@Tag(name = "租户管理", description = "租户CRUD接口")
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "租户列表", description = "获取所有租户列表")
    @GetMapping("/list")
    public Result<List<SysTenant>> list() {
        return Result.success(tenantService.list());
    }

    @Operation(summary = "获取租户详情", description = "根据ID获取租户详细信息")
    @GetMapping("/{id}")
    public Result<TenantDTO> getById(
            @Parameter(description = "租户ID") @PathVariable Long id) {
        return Result.success(tenantService.getById(id));
    }

    @Operation(summary = "创建租户", description = "新增租户")
    @PostMapping
    public Result<Void> create(
            @Parameter(description = "租户信息") @Valid @RequestBody TenantDTO dto) {
        tenantService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新租户", description = "修改租户信息")
    @PutMapping
    public Result<Void> update(
            @Parameter(description = "租户信息") @Valid @RequestBody TenantDTO dto) {
        tenantService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除租户", description = "根据ID删除租户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "租户ID") @PathVariable Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}
