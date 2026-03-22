package org.lyz.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.UserDTO;
import org.lyz.common.core.entity.SysUser;
import org.lyz.system.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD、角色分配、密码重置接口")
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表", description = "获取所有用户列表")
    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        return Result.success(userService.list());
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<UserDTO> getById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "创建用户", description = "新增系统用户")
    @PostMapping
    public Result<Void> create(
            @Parameter(description = "用户信息") @Valid @RequestBody UserDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @Operation(summary = "更新用户", description = "修改用户信息")
    @PutMapping
    public Result<Void> update(
            @Parameter(description = "用户信息") @Valid @RequestBody UserDTO dto) {
        userService.update(dto);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重置密码", description = "重置用户密码")
    @PutMapping("/password")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @RequestParam Long id,
            @Parameter(description = "新密码") @RequestParam String password) {
        userService.resetPassword(id, password);
        return Result.success();
    }

    @Operation(summary = "分配角色", description = "给用户分配角色")
    @PutMapping("/assign-roles")
    public Result<Void> assignRoles(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色ID列表") @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return Result.success();
    }
}
