package org.lyz.system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.MenuDTO;
import org.lyz.system.entity.SysMenu;
import org.lyz.system.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.list());
    }

    @GetMapping("/tree")
    public Result<List<MenuDTO>> tree() {
        return Result.success(menuService.getMenuTree());
    }

    @GetMapping("/{id}")
    public Result<MenuDTO> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody MenuDTO dto) {
        menuService.create(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody MenuDTO dto) {
        menuService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
