package org.lyz.iot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lyz.common.core.result.PageResult;
import org.lyz.common.core.result.Result;
import org.lyz.iot.dto.ProductDTO;
import org.lyz.iot.dto.ProductTreeDTO;
import org.lyz.iot.entity.IotProduct;
import org.lyz.iot.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "产品管理")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "产品列表")
    @GetMapping("/list")
    public Result<PageResult<IotProduct>> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "size", defaultValue = "10") int size,
                                                @RequestParam(value = "name", required = false) String name) {
        return Result.success(productService.list(page, size, name));
    }

    @Operation(summary = "产品详情")
    @GetMapping("/{id}")
    public Result<IotProduct> getById(@PathVariable(value = "id") String id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "创建产品")
    @PostMapping
    public Result<IotProduct> create(@RequestBody ProductDTO dto) {
        return Result.success(productService.create(dto));
    }

    @Operation(summary = "更新产品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable(value = "id") String id, @RequestBody ProductDTO dto) {
        productService.update(id, dto);
        return Result.success(null);
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable(value = "id") String id) {
        productService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "产品树形结构")
    @GetMapping("/tree")
    public Result<List<ProductTreeDTO>> tree() {
        return Result.success(productService.getTree());
    }

    @Operation(summary = "获取物模型")
    @GetMapping("/{id}/thing-model")
    public Result<String> getThingModel(@PathVariable(value = "id") String id) {
        return Result.success(productService.getThingModel(id));
    }

    @Operation(summary = "更新物模型")
    @PutMapping("/{id}/thing-model")
    public Result<Void> updateThingModel(@PathVariable(value = "id") String id, @RequestBody String thingModel) {
        productService.updateThingModel(id, thingModel);
        return Result.success(null);
    }

    @Operation(summary = "发布物模型")
    @PostMapping("/{id}/thing-model/publish")
    public Result<Void> publishThingModel(@PathVariable(value = "id") String id) {
        productService.publishThingModel(id);
        return Result.success(null);
    }
}
