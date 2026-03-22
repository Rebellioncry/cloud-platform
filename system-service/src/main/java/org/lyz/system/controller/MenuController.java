package org.lyz.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.MenuDTO;
import org.lyz.system.entity.SysMenu;
import org.lyz.system.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单CRUD、菜单树接口")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "菜单列表", description = "获取所有菜单列表")
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.list());
    }

    @Operation(summary = "菜单树", description = "获取菜单树形结构")
    @GetMapping("/tree")
    public Result<List<MenuDTO>> tree() {
        return Result.success(menuService.getMenuTree());
    }

    @Operation(summary = "获取菜单详情", description = "根据ID获取菜单详细信息")
    @GetMapping("/{id}")
    public Result<MenuDTO> getById(
            @Parameter(description = "菜单ID") @PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @Operation(summary = "创建菜单", description = "新增菜单")
    @PostMapping
    public Result<Void> create(
            @Parameter(description = "菜单信息") @Valid @RequestBody MenuDTO dto) {
        menuService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新菜单", description = "修改菜单信息")
    @PutMapping
    public Result<Void> update(
            @Parameter(description = "菜单信息") @Valid @RequestBody MenuDTO dto) {
        menuService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除菜单", description = "根据ID删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "菜单ID") @PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
