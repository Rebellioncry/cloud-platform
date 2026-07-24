package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.FileStorageDTO;
import org.lyz.iot.entity.IotFileStorage;
import org.lyz.iot.service.FileStorageService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "文件存储")
@RestController
@RequestMapping("/file-storage")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "存储列表")
    @GetMapping("/list")
    public Result<PageResult<IotFileStorage>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name", required = false) String name) {
        return Result.success(fileStorageService.list(page, size, name));
    }

    @Operation(summary = "存储详情")
    @GetMapping("/{id}")
    public Result<IotFileStorage> getById(@PathVariable(value = "id") String id) {
        return Result.success(fileStorageService.getById(id));
    }

    @Operation(summary = "创建存储配置")
    @PostMapping
    public Result<IotFileStorage> create(@RequestBody FileStorageDTO dto) {
        return Result.success(fileStorageService.create(dto));
    }

    @Operation(summary = "更新存储配置")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable(value = "id") String id, @RequestBody FileStorageDTO dto) {
        fileStorageService.update(id, dto);
        return Result.success(null);
    }

    @Operation(summary = "删除存储配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable(value = "id") String id) {
        fileStorageService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "测试连接")
    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable(value = "id") String id) {
        return Result.success(fileStorageService.testConnection(id));
    }
}
