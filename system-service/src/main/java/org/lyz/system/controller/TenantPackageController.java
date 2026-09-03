package org.lyz.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.system.entity.SysTenantPackage;
import org.lyz.system.service.TenantPackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/platform/package")
@RequiredArgsConstructor
@Tag(name = "租户套餐管理", description = "平台管理员专用：套餐CRUD")
public class TenantPackageController {

    private final TenantPackageService packageService;

    @Operation(summary = "套餐列表", description = "分页获取所有套餐")
    @GetMapping
    public Result<PageResult<SysTenantPackage>> list(
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(packageService.list(page, size));
    }

    @Operation(summary = "全部套餐", description = "获取所有启用的套餐（用于下拉选择）")
    @GetMapping("/all")
    public Result<List<SysTenantPackage>> listAll() {
        return Result.success(packageService.listAll());
    }

    @Operation(summary = "套餐详情", description = "获取指定套餐的详细信息")
    @GetMapping("/{id}")
    public Result<SysTenantPackage> getById(
            @Parameter(description = "套餐ID") @PathVariable(value = "id") String id) {
        return Result.success(packageService.getById(id));
    }

    @Operation(summary = "创建套餐", description = "新增租户套餐")
    @PostMapping
    public Result<Void> create(@RequestBody SysTenantPackage pkg) {
        packageService.create(pkg);
        return Result.success();
    }

    @Operation(summary = "更新套餐", description = "修改套餐信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "套餐ID") @PathVariable(value = "id") String id,
            @RequestBody SysTenantPackage pkg) {
        pkg.setId(id);
        packageService.update(pkg);
        return Result.success();
    }

    @Operation(summary = "删除套餐", description = "根据ID删除套餐")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "套餐ID") @PathVariable(value = "id") String id) {
        packageService.delete(id);
        return Result.success();
    }
}
