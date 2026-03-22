package org.lyz.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.RoleDTO;
import org.lyz.system.entity.SysRole;
import org.lyz.system.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色CRUD、菜单分配接口")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表", description = "获取所有角色列表")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.list());
    }

    @Operation(summary = "获取角色详情", description = "根据ID获取角色详细信息")
    @GetMapping("/{id}")
    public Result<RoleDTO> getById(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @Operation(summary = "创建角色", description = "新增系统角色")
    @PostMapping
    public Result<Void> create(
            @Parameter(description = "角色信息") @Valid @RequestBody RoleDTO dto) {
        roleService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新角色", description = "修改角色信息")
    @PutMapping
    public Result<Void> update(
            @Parameter(description = "角色信息") @Valid @RequestBody RoleDTO dto) {
        roleService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除角色", description = "根据ID删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "分配菜单", description = "给角色分配菜单权限")
    @PutMapping("/assign-menus")
    public Result<Void> assignMenus(
            @Parameter(description = "角色ID") @RequestParam Long roleId,
            @Parameter(description = "菜单ID列表") @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }
}
