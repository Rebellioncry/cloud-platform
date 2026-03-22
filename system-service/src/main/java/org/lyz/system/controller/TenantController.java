package org.lyz.system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.system.dto.TenantDTO;
import org.lyz.system.entity.SysTenant;
import org.lyz.system.service.TenantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/list")
    public Result<List<SysTenant>> list() {
        return Result.success(tenantService.list());
    }

    @GetMapping("/{id}")
    public Result<TenantDTO> getById(@PathVariable Long id) {
        return Result.success(tenantService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody TenantDTO dto) {
        tenantService.create(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody TenantDTO dto) {
        tenantService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}
