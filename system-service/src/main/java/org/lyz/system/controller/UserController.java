package org.lyz.system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.UserDTO;
import org.lyz.common.core.entity.SysUser;
import org.lyz.system.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody UserDTO dto) {
        userService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> resetPassword(@RequestParam Long id, @RequestParam String password) {
        userService.resetPassword(id, password);
        return Result.success();
    }

    @PutMapping("/assign-roles")
    public Result<Void> assignRoles(@RequestParam Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return Result.success();
    }
}
