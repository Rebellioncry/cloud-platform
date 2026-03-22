package org.lyz.resource.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.lyz.common.core.result.Result;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@Tag(name = "资源管理", description = "文件、图片等资源相关接口")
public class ResourceController {

    @Operation(summary = "测试接口", description = "测试资源服务是否正常运行")
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Resource Service is working!");
    }

    @Operation(summary = "获取资源详情", description = "根据ID获取资源详细信息")
    @GetMapping("/{id}")
    public Result<ResourceVO> getResource(
            @Parameter(description = "资源ID") @PathVariable Long id) {
        return Result.success(new ResourceVO(id, "文档.pdf", "document", 1024L, "admin", LocalDateTime.now()));
    }

    @Operation(summary = "资源列表", description = "分页获取资源列表")
    @GetMapping("/list")
    public Result<List<ResourceVO>> listResources() {
        return Result.success(Arrays.asList(
                new ResourceVO(1L, "文档.pdf", "document", 1024L, "admin", LocalDateTime.now()),
                new ResourceVO(2L, "图片.png", "image", 2048L, "user1", LocalDateTime.now())
        ));
    }

    @Operation(summary = "上传资源", description = "上传文件资源")
    @PostMapping("/upload")
    public Result<ResourceVO> upload(
            @Parameter(description = "资源名称") @RequestParam String name,
            @Parameter(description = "资源类型") @RequestParam String type) {
        return Result.success(new ResourceVO(3L, name, type, 0L, "currentUser", LocalDateTime.now()));
    }

    @Operation(summary = "删除资源", description = "根据ID删除资源")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(
            @Parameter(description = "资源ID") @PathVariable Long id) {
        return Result.success(null);
    }

    @Data
    @Tag(name = "资源信息", description = "资源详细信息")
    public static class ResourceVO {
        @Parameter(description = "资源ID")
        private Long id;
        
        @Parameter(description = "资源名称")
        private String name;
        
        @Parameter(description = "资源类型")
        private String type;
        
        @Parameter(description = "资源大小(字节)")
        private Long size;
        
        @Parameter(description = "上传用户")
        private String uploader;
        
        @Parameter(description = "上传时间")
        private LocalDateTime uploadTime;

        public ResourceVO() {}

        public ResourceVO(Long id, String name, String type, Long size, String uploader, LocalDateTime uploadTime) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.size = size;
            this.uploader = uploader;
            this.uploadTime = uploadTime;
        }
    }
}
