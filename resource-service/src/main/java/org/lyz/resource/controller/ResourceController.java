package org.lyz.resource.controller;

import org.lyz.common.core.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resource")
public class ResourceController {

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Resource Service is working!");
    }

    @GetMapping("/demo")
    public Result<String> demo() {
        return Result.success("This is a protected resource");
    }
}
