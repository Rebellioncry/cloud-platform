package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.FirmwareDTO;
import org.lyz.iot.entity.IotFirmware;
import org.lyz.iot.service.FirmwareService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "固件管理")
@RestController
@RequestMapping("/firmware")
@RequiredArgsConstructor
public class FirmwareController {

    private final FirmwareService firmwareService;

    @Operation(summary = "固件列表")
    @GetMapping("/list")
    public Result<PageResult<IotFirmware>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam(value = "name", required = false) String name) {
        return Result.success(firmwareService.list(page, size, productId, name));
    }

    @Operation(summary = "固件详情")
    @GetMapping("/{id}")
    public Result<IotFirmware> getById(@PathVariable(value = "id") String id) {
        return Result.success(firmwareService.getById(id));
    }

    @Operation(summary = "上传固件")
    @PostMapping("/upload")
    public Result<IotFirmware> upload(
            @RequestPart(value = "file") MultipartFile file,
            @RequestParam(value = "productId") String productId,
            @RequestParam(value = "firmwareName") String firmwareName,
            @RequestParam(value = "firmwareVersion") String firmwareVersion,
            @RequestParam(value = "storageId") String storageId,
            @RequestParam(value = "description", required = false) String description) {
        FirmwareDTO dto = new FirmwareDTO();
        dto.setProductId(productId);
        dto.setFirmwareName(firmwareName);
        dto.setFirmwareVersion(firmwareVersion);
        dto.setStorageId(storageId);
        dto.setDescription(description);
        return Result.success(firmwareService.create(dto, file));
    }

    @Operation(summary = "更新固件")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable(value = "id") String id, @RequestBody FirmwareDTO dto) {
        firmwareService.update(id, dto);
        return Result.success(null);
    }

    @Operation(summary = "删除固件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable(value = "id") String id) {
        firmwareService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "获取下载URL")
    @GetMapping("/{id}/download-url")
    public Result<String> getDownloadUrl(@PathVariable(value = "id") String id) {
        return Result.success(firmwareService.getDownloadUrl(id));
    }
}
