package org.lyz.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.TenantDTO;
import org.lyz.system.dto.UserDTO;
import org.lyz.system.service.PlatformService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/platform")
@RequiredArgsConstructor
@Tag(name = "平台管理", description = "平台管理员专用：租户管理、创建租户管理员")
public class PlatformController {

    private final PlatformService platformService;

    @Operation(summary = "租户列表", description = "分页获取所有租户（含用户数统计）")
    @GetMapping("/tenant")
    public Result<PageResult<Map<String, Object>>> listTenants(
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(platformService.listTenants(page, size));
    }

    @Operation(summary = "租户详情", description = "获取指定租户的详细信息")
    @GetMapping("/tenant/{id}")
    public Result<Map<String, Object>> getTenantDetail(
            @Parameter(description = "租户ID") @PathVariable(value = "id") String id) {
        return Result.success(platformService.getTenantDetail(id));
    }

    @Operation(summary = "创建租户", description = "创建新租户及其管理员账号")
    @PostMapping("/tenant")
    public Result<Void> createTenant(
            @Parameter(description = "租户信息") @Valid @RequestBody TenantWithAdminRequest request) {
        platformService.createTenantWithAdmin(request.getTenant(), request.getAdmin());
        return Result.success();
    }

    @Operation(summary = "为租户创建管理员", description = "为已有租户创建管理员账号")
    @PostMapping("/tenant/{id}/admin")
    public Result<Void> createTenantAdmin(
            @Parameter(description = "租户ID") @PathVariable(value = "id") String id,
            @Parameter(description = "管理员信息") @Valid @RequestBody UserDTO adminDto) {
        platformService.createTenantAdmin(id, adminDto);
        return Result.success();
    }

    @Operation(summary = "更新租户状态", description = "启用或禁用租户")
    @PutMapping("/tenant/{id}/status")
    public Result<Void> updateTenantStatus(
            @Parameter(description = "租户ID") @PathVariable(value = "id") String id,
            @Parameter(description = "状态: 1=启用, 0=禁用") @RequestParam(value = "status") Integer status) {
        platformService.updateTenantStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "模拟登录", description = "平台管理员模拟登录为租户管理员")
    @PostMapping("/tenant/{id}/impersonate")
    public Result<Map<String, Object>> impersonate(
            @Parameter(description = "租户ID") @PathVariable(value = "id") String id) {
        return Result.success(platformService.impersonateTenant(id));
    }

    @Operation(summary = "退出模拟登录", description = "从模拟登录状态恢复为平台管理员")
    @PostMapping("/impersonate/return")
    public Result<Void> returnFromImpersonate() {
        platformService.returnFromImpersonate();
        return Result.success();
    }

    @lombok.Data
    public static class TenantWithAdminRequest {
        @Valid
        private TenantDTO tenant;
        @Valid
        private UserDTO admin;
    }
}
